# Android App

## How to import the app into my PC?

**Note: You have to change the IP address in the source code twice for the IP of your Raspberry Pi or where you have installed the WebSocket server and the MQTT broker**

Go tho the [file](./myIoTApp2/app/src/main/java/dte/masteriot/mdp/myiotapp2/MainActivity.java) in the app and search for this lines.

MQTT Broker IP:
![MQTTBrokerIP](../Docs/img/mqttBrokerIP.jpg)

WebSocket Server IP
![WebSocketServerIP](../Docs/img/WebSocketServerIP.jpg)

First unzip the file with the [app](./myIoTApp2.zip). Then start Android Studio and click on "Open an existing Android Studio project". Finally, navigate to the root directory of the project and click OK.

Once you have the project inside Android Studio, go check this [post](https://realm.io/building-android-app/) in order to build the project into the Android emulator.

## How does the app look

![My Image](../Docs/img/androidApp0.jpg)