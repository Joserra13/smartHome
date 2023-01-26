# Arduino Code

## This how the sensors are currently connected to the Arduino
![My Image](../Docs/img/ArduinoConnections.jpg)

## Installing

1. Connect the sensors to the Arduino MEGA as it is in the picture above. In case of doubt, you can always check in the source code where is each pin from each sensor connected.

2. Load the [source code](/Arduino/TESTv4%20-%20Final/TESTv4%20-%20Final.ino) into the board, after installing the Arduino IDE, following the steps explained in this [post](https://support.arduino.cc/hc/en-us/articles/4733418441116-Upload-a-sketch-in-Arduino-IDE)

**Note: In order to load succesfully the source code to the Arduino, the pin coming from the TX pin at the Bluetooth module HC-06 MUST be disconnected from the pin RX at the Arduino board to prevent a collision in the serial port**

**Obviously, after the code was uploaded to the Arduino, you MUST connect the wire again :)**