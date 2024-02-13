ClockDivisor = 5
byte ADbusVal = 0;
byte ADbusDir = 0;
NumBytesToSend = 0;
FTDI.FT_STATUS ftStatus = FTDI.FT_STATUS.FT_OK;

/***** Initial device configuration *****/
ftStatus = FTDI.FT_STATUS.FT_OK;
ftStatus |= myFtdiDevice.SetTimeouts(5000, 5000);
ftStatus |= myFtdiDevice.SetLatency(16);
ftStatus |=
myFtdiDevice.SetFlowControl(FTDI.FT_FLOW_CONTROL.FT_FLOW_RTS_CTS, 0x00, 0x00);
ftStatus |= myFtdiDevice.SetBitMode(0x00, 0x00);
ftStatus |= myFtdiDevice.SetBitMode(0x00, 0x02); // MPSSE mode
if (ftStatus != FTDI.FT_STATUS.FT_OK) {
  return 1; // error();
}

/***** Flush the buffer *****/
SPI_Status = FlushBuffer();

/***** Synchronize the MPSSE interface by sending bad command 0xAA *****/
NumBytesToSend = 0;
MPSSEbuffer[NumBytesToSend++] = 0xAA;
SPI_Status = Send_Data(NumBytesToSend);
if (SPI_Status != 0) return 1; // error();
NumBytesToRead = 2;
SPI_Status = Receive_Data(2);
if (SPI_Status != 0) return 1; //error();

/***** Synchronize the MPSSE interface by sending bad command 0xAB *****/
NumBytesToSend = 0;
MPSSEbuffer[NumBytesToSend++] = 0xAB;
SPI_Status = Send_Data(NumBytesToSend);
if (SPI_Status != 0) return 1; // error();

NumBytesToRead = 2;
SPI_Status = Receive_Data(2);

if (SPI_Status != 0) return 1; //error();
NumBytesToSend = 0;

MPSSEbuffer[NumBytesToSend++] = 0x8A; // Disable clock divide by 5 for 60Mhz master clock
MPSSEbuffer[NumBytesToSend++] = 0x97; // Turn off adaptive clocking
MPSSEbuffer[NumBytesToSend++] = 0x8D; // Disable 3 phase data clock, used by I2C to allow data on both clock edges
				      // The SK clock frequency can be worked out by below algorithm with divide by 5 set as off
				      // SK frequency = 60MHz /((1 + [(1 +0xValueH*256) OR 0xValueL])*2)
MPSSEbuffer[NumBytesToSend++] = 0x86; //Command to set clock divisor
MPSSEbuffer[NumBytesToSend++] = (byte)(ClockDivisor & 0x00FF);
//Set 0xValueL of clock divisor
MPSSEbuffer[NumBytesToSend++] = (byte)((ClockDivisor >> 8) & 0x00FF);
//Set 0xValueH of clock divisor
MPSSEbuffer[NumBytesToSend++] = 0x85; // loopback off

#if (FT232H)
MPSSEbuffer[NumBytesToSend++] = 0x9E; //Enable the FT232H's drive-zero mode with the following enable mask...
MPSSEbuffer[NumBytesToSend++] = 0x00; // 0x07; // ... Low byte (ADx) enables - bits 0, 1 and 2 and ...
MPSSEbuffer[NumBytesToSend++] = 0x00; //...High byte (ACx) enables - all off
ADbusVal = (byte)(0x00 | I2C_Data_SDAhi_SCLhi | (GPIO_Low_Dat & 0xF8)); // SDA and SCL both output high (open drain)
ADbusDir = (byte)(0x00 | I2C_Dir_SDAout_SCLout | (GPIO_Low_Dir & 0xF8));
#else
ADbusVal = (byte)(0x00 | I2C_Data_SDAlo_SCLlo | (GPIO_Low_Dat & 0xF8)); // SDA and SCL set low but as input to mimic open drain
ADbusDir = (byte)(0x00 | I2C_Dir_SDAin_SCLin | (GPIO_Low_Dir & 0xF8)); //
#endif
MPSSEbuffer[NumBytesToSend++] = 0x80; //GPIO command for ADBUS(low byte). ACBUS(high byte) = 0x82
MPSSEbuffer[NumBytesToSend++] = 0x38; // set pin
MPSSEbuffer[NumBytesToSend++] = 0xFB; // set direction of bus. All pins out except data in
SPI_Status = Send_Data(NumBytesToSend);

// Write SPI
public void SPI_WriteData(byte[] data, int size)
{
  NumBytesToSend = 0;
  int sizeLen = size - 1;
  byte lenLow = (byte)(sizeLen & 0xff);
  byte lenHigh = (byte)((sizeLen >> 8) & 0xff);

  //set CS low
  SPI_SetCS(0, SPICS_CS);

  //write bytes
  MPSSEbuffer[NumBytesToSend++] = MSB_FALLING_EDGE_CLOCK_BYTE_OUT;
  MPSSEbuffer[NumBytesToSend++] = lenLow; // length low byte
  MPSSEbuffer[NumBytesToSend++] = lenHigh; // length high byte. Bytes + 1 (0x0000 will send 1 byte)
  //MPSSEbuffer[NumBytesToSend++] = dataBytes[3];

  // byte data
  for(int i = 0; i < size; i++)
  {
    MPSSEbuffer[NumBytesToSend++] = data[i];
  }

  //set CS high
  SPI_SetCS(1, SPICS_CS);

  //send to MPSSE
  SPI_Status = Send_Data(NumBytesToSend);

  //Console.WriteLine(SPI_Status);
  NumBytesToSend = 0;
}

// Read SPI
public uint SPI_ReadData(int reg)
{
  uint rtn = 0;
  NumBytesToSend = 0;
  byte[] dataBytes = new byte[4];
  dataBytes[0] = (byte)(reg & 0xff);
  dataBytes[1] = 0;
  dataBytes[2] = 0;
  dataBytes[3] = 0;

  //set CS low
  SPI_SetCS(0, SPICS_CS);

  //read data
  MPSSEbuffer[NumBytesToSend++] = MSB_FALLING_EDGE_CLOCK_BYTE_IN;
  MPSSEbuffer[NumBytesToSend++] = 0x03; // length low byte
  MPSSEbuffer[NumBytesToSend++] = 0x00; // length high byte. Bytes + 1 (0x0000 will send 1 byte)

  //set CS high
  SPI_SetCS(1, SPICS_CS);

  //send to MPSSE
  SPI_Status = Send_Data(NumBytesToSend);
  SPI_Status = Receive_Data(4);
  rtn = 0;
  rtn |= (uint)(InputBuffer2[0] << 24);
  rtn |= (uint)(InputBuffer2[1] << 16);
  rtn |= (uint)(InputBuffer2[2] << 8);
  rtn |= (uint)(InputBuffer2[3] & 0xff);
  NumBytesToSend = 0;
  return rtn;
}

// Set SPI CS
private void SPI_SetCS(int csVal, byte csNum)
{
  byte csCont = 0x38; // set CS/GPOI0/GPIO1 high
  if (csVal == 0) {
    csCont = csNum;
  }

  //set 5 times to hold for 1uS
  for (int i = 0; i < 5; i++)
  {
    MPSSEbuffer[NumBytesToSend++] = 0x80; //GPIO command for ADBUS(low byte). ACBUS(high byte) = 0x82
    MPSSEbuffer[NumBytesToSend++] = csCont; // set cs pin
    MPSSEbuffer[NumBytesToSend++] = 0xFB; // set direction of bus. All pins out except data in
  }
}
