//package com.example.ud1000
//
//import android.content.Context
//import com.ftdi.j2xx.D2xxManager
//import com.ftdi.j2xx.FT_Device
//
////const val FTDI_DEVICE_DESCRIPTION = "Detector B"
//const val BAUD_RATE = 2.4e7.toInt()
//
//const val SPICS_CS = 0x00.toByte() // TODO
//class FtdiMetalDetectorController(private val context: Context) {
//    private var d2xxManager: D2xxManager = D2xxManager.getInstance(context)
//    private var ftDevice: FT_Device? = null
//    private var mpsseBuffer = ByteArray(2014)
//    private var mpsseBufferLen = 0
//
//    fun connectDevice() {
//        val devCount = d2xxManager.createDeviceInfoList(this.context)
//        if (devCount == 0) {
//            throw Exception("FTDI device not found")
//        }
//
//        val devList = arrayOfNulls<D2xxManager.FtDeviceInfoListNode>(devCount)
//        d2xxManager.getDeviceInfoList(devCount, devList)
//
//        val dev = devList.find { it?.description == FTDI_DEVICE_DESCRIPTION }
//            ?: throw Exception("FTDI device not found with description: $FTDI_DEVICE_DESCRIPTION")
//
//        ftDevice = d2xxManager.openByDescription(this.context, dev.description)
//            ?: throw Exception("FTDI device not open")
//    }
//
//    fun initializeSPI() {
//        val ftDevice = ftDevice ?: throw Exception("FTDI device not open")
//        if (!ftDevice.setBaudRate(BAUD_RATE)) {
//            throw Exception("FTDI device failed to set baud rate")
//        }
//        if (!ftDevice.setLatencyTimer(16)) {
//            throw Exception("FTDI device failed to set latency timer")
//        }
//        if (!ftDevice.setFlowControl(
//                D2xxManager.FT_FLOW_RTS_CTS,
//                0x00.toByte(),
//                0x00.toByte()
//            )
//        ) {
//            throw Exception("FTDI device failed to set flow control")
//        }
//        if (!ftDevice.setBitMode(0x00, D2xxManager.FT_BITMODE_RESET)) {
//            throw Exception("FTDI device failed to set bit mode FT_BITMODE_RESET")
//        }
//        if (!ftDevice.setBitMode(0x00, D2xxManager.FT_BITMODE_MPSSE)) {
//            throw Exception("FTDI device failed to set bit mode FT_BITMODE_MPSSE")
//        }
//
//        //syncMpsse()
//    }
//
//    fun mpsseSync() {
//        spiWrite(byteArrayOf(0xaa.toByte()))
//        var status = spiRead(1)
//    }
//
//    fun mpssePush(byte: Byte) {
//        mpsseBuffer[mpsseBufferLen++] = byte
//    }
//
//    fun mpssePush(data: ByteArray) {
//        data.forEach { mpssePush(it) }
//    }
//
//    fun mpsseFlush() {
//        if (mpsseBufferLen == 0) {
//            return
//        }
//        spiWrite(mpsseBuffer.sliceArray(0 until mpsseBufferLen))
//        mpsseBufferLen = 0
//    }
//
//    fun txEnableOff() {
//        val data = byteArrayOf(0x13.toByte(), 0x00.toByte())
//        spiWrite()
//    }
//
//    fun spiWrite(data: ByteArray) {
//        ftDevice?.write(data)
//    }
//
//    fun spiRead(size: Int) {
//        val dataRead = ByteArray(size)
//        ftDevice?.read(dataRead)
//        return dataRead
//    }
//
//    fun writeRegister(reg: Int, data: Int) {
//        val writedata = byteArrayOf(
//            (reg and 0xff).toByte(),
//            (data shr 16 and 0xff).toByte(),
//            (data shr 8 and 0xff).toByte(),
//        )
//    }
//
//    fun initialize() {
//        // add usb device
//        val devCount = d2xxManager.createDeviceInfoList(this.context)
//        val devList = arrayOfNulls<D2xxManager.FtDeviceInfoListNode>(devCount)
//        d2xxManager.getDeviceInfoList(devCount, devList)
//        // check we have device with description
//        val dev = devList.firstOrNull { it?.description == FTDI_DEVICE_DESCRIPTION }
//        if (dev == null) {
//            throw Exception("FTDI device not found with description: $FTDI_DEVICE_DESCRIPTION")
//        }
//        // open usb device
//        val ftDevice = d2xxManager.openByDescription(this.context, FTDI_DEVICE_DESCRIPTION)
//        // set baud rate
//        ftDevice.setBaudRate(9600)
//        // set data bit
//        ftDevice.setDataCharacteristics(
//            D2xxManager.FT_DATA_BITS_8,
//            D2xxManager.FT_STOP_BITS_1,
//            D2xxManager.FT_PARITY_NONE
//        )
//        // set flow control
//        ftDevice.setFlowControl(
//            D2xxManager.FT_FLOW_NONE,
//            0x00.toByte(),
//            0x00.toByte()
//        )
//        // set read timeout
//        ftDevice.setReadTimeout(5000)
//        // set write timeout
//        ftDevice.setWriteTimeout(5000)
//        // set latency timer
//        ftDevice.latencyTimer = 16
//        // set bit mode
//        ftDevice.setBitMode(0x00.toByte(), D2xxManager.FT_BITMODE_RESET)
//        ftDevice.setBitMode(0x00.toByte(), D2xxManager.FT_BITMODE_ASYNC_BITBANG)
//        // set event character
//        ftDevice.setEventCharacter(0x0D.toByte(), 0x00.toByte())
//        // set error character
//        ftDevice.setErrorCharacter(0x11.toByte(), 0x00.toByte())
//        // set flow control
//        ftDevice.setFlowControl(
//            D2xxManager.FT_FLOW_RTS_CTS,
//            0x11.toByte(),
//            0x13.toByte()
//        )
//
//    }
//}
//
////interface MetalDetectorController {
////
////}
////
//class SPIController (private val ftDevice: FT_Device) {
//    private var mpsseBuffer = ByteArray(1024)
//    private var mpsseBufferLen = 0
//
//    fun initializeDevice() {
//        ftDevice.run {
//            latencyTimer = 16.toByte()
//            setFlowControl(D2xxManager.FT_FLOW_RTS_CTS, 0x00.toByte(), 0x00.toByte())
//            setBitMode(0x00.toByte(), D2xxManager.FT_BITMODE_RESET)
//            setBitMode(0x00.toByte(), D2xxManager.FT_BITMODE_MPSSE)
//        }
//    }
//
//    fun flushBuffer() {
//        // TODO: flush or send the buffer here? refer to pdf
//        ftDevice.purge(D2xxManager.FT_PURGE_RX or D2xxManager.FT_PURGE_TX)
//    }
//
//    fun sendMpsseBuffer() {
//        ftDevice.write(mpsseBuffer, mpsseBufferLen)
//        mpsseBufferLen = 0
//    }
//
//    fun sendCommand(command: ByteArray) {
//        val bytesWritten = ftDevice.write(command, command.size)
//        if (bytesWritten != command.size) {
//            throw Exception("Failed to write all bytes to FTDI device")
//        }
//        // TODO: impl
//    }
//
//    fun receiveData(size: Int): ByteArray {
//        val data = ByteArray(size)
//        val bytesRead = ftDevice.read(data, size)
//        if (bytesRead != size) {
//            throw Exception("Failed to read all bytes from FTDI device")
//        }
//        // TODO: impl
//    }
//
//    fun writeSpi(data: ByteArray) {
//        val sizeLen = data.size - 1
//        val lenLow = (sizeLen and 0xff).toByte()
//        var lenHigh = (sizeLen shr 8 and 0xff).toByte()
//        setCs(SPICS_CS, 0)
//        bytes = byteArrayOf(
//            MSB_FALLING_EDGE_CLOCK_BYTE_OUT,
//            lenLow,
//            lenHigh,
//        ) + data
//        mpssePush(bytes)
//        setCs(SPICS_CS, 1)
//        sendMpsseBuffer()
//    }
//
//    fun setCs(cs: byte, value: int) {
//        csCont = 0x38.toByte() // set CS/GPIO0/GPIO1 high
//        if (value == 0) {
//            csCont = cs
//        }
//        // hold for 1us
//        // TODO: measure/use timer
//        bytes = byteArrayOf(
//            0x80.toByte(), // GPIO command for ADBUS(low byte). ACBUS(high byte) = 0x82
//            csCont,        // CS pin
//            0xfb.toByte(), // Bus direction (all pins out except data in)
//        )
//        for (i in 0..5) {
//            // TODO: use DI to access parent method
//            mpssePush(bytes)
//        }
//    }
//
//}