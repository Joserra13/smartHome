# -*- coding: utf-8 -*
import serial
import time

# print("Program started!")

def main():
    ser.write(b"D")
    while True:
        # Obtener el carácter de búfer de recepción
        count = ser.readline()
        if count != 0:
            # Leer contenido y mostrar
            print (count)
            break;
        # Limpiar el búfer de recepción
        ser.flushInput()
        # Retraso necesario del software
        time.sleep(0.1)

# Abra el puerto serie
ser = serial.Serial('/dev/rfcomm1', 9600)
if ser.isOpen == False:
    ser.open()                # Abra el puerto serie
main()
