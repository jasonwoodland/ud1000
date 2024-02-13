package com.example.ud1000

import android.content.Context
import android.widget.Toast
import com.ftdi.j2xx.D2xxManager
import com.ftdi.j2xx.FT_Device
import com.ftdi.j2xx.ft4222.FT_4222_Defines.FT4222_STATUS
import kotlin.experimental.or

// Metal detector constants
const val FTDI_DEVICE_DESCRIPTION = "Detector B"
const val SPICS_CS = 0x00 // TODO

// Declare parameters for 93C56
const val MemSize = 16 // Define data quantity you want to send out
const val SPIDATALENGTH: Byte = 11 // 3 digit command + 8 digit address
const val READ: Byte = 0xC0.toByte() // 110xxxxx
const val WRITE: Byte = 0xA0.toByte() // 101xxxxx
const val WREN: Byte = 0x98.toByte() // 10011xxx
const val ERAL: Byte = 0x90.toByte() // 10010xxx

// Declare for BAD command
const val AA_ECHO_CMD_1: Byte = 0xAA.toByte()
const val AB_ECHO_CMD_2: Byte = 0xAB.toByte()
const val BAD_COMMAND_RESPONSE: Byte = 0xFA.toByte()

// Declare for MPSSE command
const val MSB_RISING_EDGE_CLOCK_BYTE_OUT: Byte = 0x10.toByte()
const val MSB_FALLING_EDGE_CLOCK_BYTE_OUT: Byte = 0x11.toByte()
const val MSB_RISING_EDGE_CLOCK_BIT_OUT: Byte = 0x12.toByte()
const val MSB_FALLING_EDGE_CLOCK_BIT_OUT: Byte = 0x13.toByte()
const val MSB_RISING_EDGE_CLOCK_BYTE_IN: Byte = 0x20.toByte()
const val MSB_RISING_EDGE_CLOCK_BIT_IN: Byte = 0x22.toByte()
const val MSB_FALLING_EDGE_CLOCK_BYTE_IN: Byte = 0x24.toByte()
const val MSB_FALLING_EDGE_CLOCK_BIT_IN: Byte = 0x26.toByte()

interface FtdiMpsseControllerInterface {
//    /**
//     * Connect to FTDevice
//     */
//    fun connectDevice()

    /**
     * Initialize SPI and MPSSE
     */
    fun initializeDevice()

    /**
     * TODO: Flush receive buffer or MPSSE buffer?
     */
    fun flushBuffer()

    /**
     * Push to MPSSE buffer
     */
    fun bufferPush(bytes: ByteArray)

    /**
     * Send MPSSE buffer and set len=0
     */
    fun sendBuffer()

    /**
     * Receive from MPSSE/SPI buffer?
     */
    fun receiveData(len: Int): ByteArray

    /**
     * Construct MPSSE buffer, set CS, and send MPSSE buffer
     */
    fun spiWriteData(data: ByteArray)

    /**
     * Sets CS, sends MPSSE command, and receives data, returns uint32
     *
     * TODO: reg is only used in dataBytes array which is unused
     */
    fun spiReadData(reg: Int): UInt

    /**
     * Set CS
     *
     * Sends MPSSE GPIO command to set CS
     */
    fun setCs(cs: Int, state: Int)

}

class MetalDetectorController(private val context: Context) {
    private var ftdiController: MetalDetectorFtdiController

    init {
        ftdiController = MetalDetectorFtdiController(context)
    }
}

class MetalDetectorFtdiController(private val context: Context) {
    private var d2xxManager: D2xxManager = D2xxManager.getInstance(context)
    private var ftDevice: FT_Device
    private var ftdiMpsseController: FtdiMpsseController

    init {
        ftDevice = getDevice()
        Toast.makeText(context, "Found device ${ftDevice.deviceInfo.id}", Toast.LENGTH_SHORT).show()
        ftdiMpsseController = FtdiMpsseController(ftDevice)
        ftdiMpsseController.initializeDevice()
        Toast.makeText(context, "Initialized successfully", Toast.LENGTH_SHORT).show()
    }

    private fun getDevice(): FT_Device {
        val devCount = d2xxManager.createDeviceInfoList(context)
        if (devCount == 0) {
            throw Exception("No FTDI devices found")
        }

        val nodeList = arrayOfNulls<D2xxManager.FtDeviceInfoListNode>(devCount)
        d2xxManager.getDeviceInfoList(devCount, nodeList)

        val node = nodeList.find { it?.description == FTDI_DEVICE_DESCRIPTION }
            ?: throw Exception("FTDI device not found with description: $FTDI_DEVICE_DESCRIPTION")

        return d2xxManager.openByDescription(context, node.description)
            ?: throw Exception("Failed to open FTDI device")
    }
}

// MpsseSpiController
class FtdiMpsseController(private var ftDevice: FT_Device) : FtdiMpsseControllerInterface {
    private var adBusVal = 0
    private var adBusDir = 0
    private var mpsseBuf = ByteArray(1024)
    private var mpsseBufLen = 0
    private var ftStatus = FT4222_STATUS.FT4222_OK

    override fun initializeDevice() {
        // TODO: ftDevice.setTimeouts(5000, 5000)?
        ftDevice.latencyTimer = 16
        if (!ftDevice.setFlowControl(
                D2xxManager.FT_FLOW_RTS_CTS,
                0x00.toByte(),
                0x00.toByte()
            )
        ) {
            throw Exception("FTDI device failed to set flow control")
        }
        if (!ftDevice.setBitMode(0x00, D2xxManager.FT_BITMODE_RESET)) {
            throw Exception("FTDI device failed to set bit mode FT_BITMODE_RESET")
        }
        if (!ftDevice.setBitMode(0x00, D2xxManager.FT_BITMODE_MPSSE)) {
            throw Exception("FTDI device failed to set bit mode FT_BITMODE_MPSSE")
        }
    }

    override fun flushBuffer() {
        // TODO: purge might be the wrong function to use here
//        ftDevice.purge(D2xxManager.FT_PURGE_RX or D2xxManager.FT_PURGE_TX)
    }

    override fun bufferPush(bytes: ByteArray) {
        mpsseBuf += bytes
        mpsseBufLen += bytes.size
    }

    override fun sendBuffer() {
        ftDevice.write(mpsseBuf, mpsseBufLen)
        mpsseBufLen = 0
    }

    override fun receiveData(len: Int): ByteArray {
        val buf = ByteArray(len)
        ftDevice.read(buf, len)
        return buf
    }

    override fun spiWriteData(data: ByteArray) {
        val sizeLen = data.size - 1
        val lenLow = (sizeLen and 0xff).toByte()
        val lenHigh = (sizeLen shr 8 and 0xff).toByte()
        setCs(SPICS_CS, 0)
        bufferPush(byteArrayOf(
            MSB_FALLING_EDGE_CLOCK_BYTE_OUT,
            lenLow,
            lenHigh,
        ))
        bufferPush(data)
        setCs(SPICS_CS, 1)
        sendBuffer()
    }

    override fun spiReadData(reg: Int): UInt {
        // TODO: review unused `dataBytes`
        //  uint rtn = 0;
        //  NumBytesToSend = 0;
        //  byte[] dataBytes = new byte[4];
        //  dataBytes[0] = (byte)(reg & 0xff);
        //  dataBytes[1] = 0;
        //  dataBytes[2] = 0;
        //  dataBytes[3] = 0;

        setCs(SPICS_CS, 0)
        bufferPush(byteArrayOf(
            MSB_FALLING_EDGE_CLOCK_BYTE_IN,
            0x03,
            0x00,
        ))
        sendBuffer()

        val data = receiveData(4)
        var value = 0
        value = value or (data[0].toInt() shl 24)
        value = value or (data[1].toInt() shl 16)
        value = value or (data[2].toInt() shl 8)
        value = value or (data[3].toInt() and 0xff)
        return value.toUInt()
    }

    override fun setCs(cs: Int, state: Int) {
        var csCont = 0x80
        if (state == 0) {
            csCont = state
        }

        for (i in 0..5) {
            bufferPush(byteArrayOf(
                0x80.toByte(),
                csCont.toByte(),
                0xfb.toByte(),
            ))
        }
    }
}