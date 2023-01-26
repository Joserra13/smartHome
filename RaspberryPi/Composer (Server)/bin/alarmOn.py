# -*- coding: utf-8 -*
import serial
import time

# Abra el puerto serie
ser = serial.Serial('/dev/rfcomm1', 9600)
if ser.isOpen == False:
    ser.open()                # Abra el puerto serie
    
ser.write(b"H")





