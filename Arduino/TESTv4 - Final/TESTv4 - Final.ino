/*
 * +---------------------------------------------------+
 *              MASTER THESIS ARDUINO PART V1
 * +---------------------------------------------------+
 * Library MFRC522: 
 * DHT Sensor Library: https://github.com/adafruit/DHT-sensor-library
 * Adafruit Unified Sensor Lib: https://github.com/adafruit/Adafruit_Sensor
 */

/* +---------------+
 * |   LIBRARIES   |
 * +---------------+
 */

//RFID
#include <SPI.h>
#include <MFRC522.h>

//SERVO MOTOR
#include <Servo.h>

//DHT
#include <Adafruit_Sensor.h>
#include <DHT.h>
#include <DHT_U.h>

//OLED
#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>

/* +-----------------+
 * |   DEFINITIONS   |
 * +-----------------+
 */

//RFID
#define RST_PIN 5  //MEGA
#define SS_PIN 53  //MEGA

//GAS SENSOR
#define ANALOG_PIN 0
#define co2Zero 105

//DHT
#define DHTPIN 31      // Digital pin connected to the DHT sensor
#define DHTTYPE DHT11  // DHT 11

//OLED
#define SCREEN_WIDTH 128  // OLED display width, in pixels
#define SCREEN_HEIGHT 32  // OLED display height, in pixels

/* +-------------+
 * |   OBJECTS   |
 * +-------------+
 */

//RFID
MFRC522 mfrc522(SS_PIN, RST_PIN);  // Create MFRC522 instance
MFRC522::MIFARE_Key key;

byte joserraUID[4];
byte guest[4];

//SERVO MOTOR
Servo myservo;

//DHT
DHT_Unified dht(DHTPIN, DHTTYPE);

//OLED
// Declaration for an SSD1306 display connected to I2C (SDA, SCL pins)
Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, -1);

/* +--------------------+
 * |   SENSORS PINOUT   |
 * +--------------------+
 */

//LED Signals
int lGreen = 23;   // Alarm ON
int lRed = 25;     // Alarm OFF
int lYellow = 22;  // Outside light

//PIR Sensor
int pirPin = 43;      // Input for HC-S501
int nDetections = 0;  //Count the number of times movement was detected during the alarm is connected
int oneTime = 0;      //Only add one each time movement was detected

//Buzzer
int buzzer = 26;  //the pin of the active buzzer

//LDR
int ldr = A1;  // Photoresistor at Arduino analog pin A0

/* +--------------------+
 * |   SENSORS VALUES   |
 * +--------------------+
 */

//PIR
int pirValue;  // Place to store read PIR Value

//StateAlarm
bool stateAlarm = false;

//GAS SENSOR
uint16_t gasVal;
int co2ppm = 0;
bool gasLeak = false;

//SERVO MOTOR
bool windowIsOpen = false;
bool wantWindowOpen = false;
bool auxWindow = false;

//DHT
float tempDHT = 0.0;
float humDHT = 0.0;

//LDR
int lightVal;
bool isNight;


/* +-------------------+
 * |   AUX FUNCTIONS   |
 * +-------------------+
 */

void rfidConfig() {

  SPI.begin();         // Init SPI bus
  mfrc522.PCD_Init();  // Init MFRC522 card

  // Prepare key - all keys are set to FFFFFFFFFFFFh at chip delivery from the factory.
  for (byte i = 0; i < 6; i++) {
    key.keyByte[i] = 0xFF;
  }

  //Set my personal key ID
  joserraUID[0] = 0x19;
  joserraUID[1] = 0xEF;
  joserraUID[2] = 0x2A;
  joserraUID[3] = 0xB9;

  //Set the guest key ID
  guest[0] = 0x69;
  guest[1] = 0xD1;
  guest[2] = 0xF6;
  guest[3] = 0x97;
}

void ioConfig() {

  pinMode(pirPin, INPUT);  //Movement sensor INPUT

  pinMode(buzzer, OUTPUT);  //initialize the buzzer pin as an output

  pinMode(lGreen, OUTPUT);
  pinMode(lRed, OUTPUT);
  pinMode(lYellow, OUTPUT);

  digitalWrite(lRed, HIGH);

  myservo.attach(9);
  myservo.write(35);

  dht.begin();

  if (!display.begin(SSD1306_SWITCHCAPVCC, 0x3C)) {
    Serial.println(F("SSD1306 allocation failed"));
    for (;;)
      ;
  }
  delay(2000);
  display.clearDisplay();

  display.setTextSize(1);
  display.setTextColor(WHITE);
  display.setCursor(0, 0);
  // Display static text
  display.println("SmartHome IoT System");
  display.display();
}

void rfidRead() {
  int matches = 0;

  // Look for new cards, and select one if present
  if (!mfrc522.PICC_IsNewCardPresent() || !mfrc522.PICC_ReadCardSerial()) {
    delay(50);
    return;
  }

  // Now a card is selected. The UID and SAK is in mfrc522.uid.

  // Now, print the UID of the Card
  //Serial.print(F("Card UID:"));
  for (byte i = 0; i < mfrc522.uid.size; i++) {
    //Serial.print(mfrc522.uid.uidByte[i] < 0x10 ? " 0" : " ");
    //Serial.print(mfrc522.uid.uidByte[i], HEX);

    if (mfrc522.uid.uidByte[i] == joserraUID[i] || mfrc522.uid.uidByte[i] == guest[i]) {
      matches++;
    }
  }

  //Serial.println();

  if (matches == 4) {
    //Serial.println("Bienvenido");
    //Serial.println();

    //Change the state of the Alarm
    stateAlarm = !stateAlarm;
    nDetections = 0;
  }

  delay(1500);
}

void readGasValue() {

  int co2now[10];   //int array for co2 readings
  int co2raw = 0;   //int for raw value of co2
  int co2comp = 0;  //int for compensated co2
  co2ppm = 0;       //int for calculated ppm
  int zzz = 0;      //int for averaging
  int grafX = 0;    //int for x value of graph

  for (int x = 0; x < 10; x++) {  //samplpe co2 10x over 2 seconds
    gasVal = analogRead(ANALOG_PIN);
    co2now[x] = gasVal;
    //delay(200);
  }

  for (int x = 0; x < 10; x++) {  //add samples together
    zzz = zzz + co2now[x];
  }
  co2raw = zzz / 10;                          //divide samples by 10
  co2comp = co2raw - co2Zero;                 //get compensated value
  co2ppm = map(co2comp, 0, 1023, 400, 5000);  //map value for atmospheric levels

  if (co2ppm >= 1000) {

    if (!wantWindowOpen) {
      //The window will be closed after
      auxWindow = true;
    }

    //Open Window if closed
    myservo.write(140);
    windowIsOpen = true;
    gasLeak = true;

  } else if (co2ppm <= 500) {

    //Gas leak fixed
    gasLeak = false;

    //Close Window if do not want to be opened
    if (!wantWindowOpen) {

      myservo.write(35);
      windowIsOpen = false;
    }
  }
}

void valuesToOLED() {
  display.clearDisplay();

  display.setCursor(0, 0);
  // Display static text
  display.println("SmartHome IoT System");

  display.setCursor(0, 10);
  // Display temperature
  display.println("Temp:");

  display.setCursor(30, 10);
  display.println(tempDHT);
  display.setCursor(70, 10);
  display.println("C");

  display.setCursor(0, 20);
  // Display temperature
  display.println("Hum:");

  display.setCursor(30, 20);
  display.println(humDHT);
  display.setCursor(70, 20);
  display.println("%");

  if (gasLeak) {

    display.setCursor(85, 10);
    // Display temperature
    display.println("| Gas");

    display.setCursor(85, 20);
    // Display temperature
    display.println("| Leak!");
  }

  display.display();
}

void readTempHum() {
  // Get temperature event and print its value.
  sensors_event_t event;
  dht.temperature().getEvent(&event);

  if (isnan(event.temperature)) {
    //Serial.println(F("Error reading temperature!"));
  } else {

    tempDHT = event.temperature;
  }
  // Get humidity event and print its value.
  dht.humidity().getEvent(&event);
  if (isnan(event.relative_humidity)) {
    //Serial.println(F("Error reading humidity!"));
  } else {

    humDHT = event.relative_humidity;
  }

  valuesToOLED();
}

void readLightVal() {

  lightVal = analogRead(ldr);
  if (lightVal < 800) {
    isNight = true;
  } else if (lightVal >= 800) {
    isNight = false;
  }
}

void sendValues() {

  /*
   * STRUCTURE
   * +------------+-------+-------------+-------+----------+-------+--------------+-------+------+-------+-----+-------+---------+
   * | stateAlarm | SPACE | nDetections | SPACE | gasValue | SPACE | windowIsOpen | SPACE | temp | SPACE | hum | SPACE | isNight |
   * +------------+-------+-------------+-------+----------+-------+--------------+-------+------+-------+-----+-------+---------+
   *
   */

  //Serial.print("State:");
  //Serial.print(" ");
  Serial.print(stateAlarm);
  //Serial.println();

  //Serial.print("nDetections:");
  Serial.print(" ");
  Serial.print(nDetections);
  //Serial.println();

  //Serial.print("gasValue:");
  Serial.print(" ");
  Serial.print(co2ppm);
  //Serial.println();

  //Serial.print("windowIsOpen:");
  Serial.print(" ");
  Serial.print(windowIsOpen);
  //Serial.println();

  //Serial.print("tempDHT:");
  Serial.print(" ");
  Serial.print(tempDHT);
  //Serial.println();

  //Serial.print("humDHT:");
  Serial.print(" ");
  Serial.print(humDHT);
  //Serial.println();

  //Serial.print("isNight:");
  Serial.print(" ");
  Serial.print(isNight);
  Serial.println();
}

void raspCommands() {
  //if (Serial.available())
  //{
  char c = Serial.read();  //Store the command in a variable
  if (c == 'H') {
    //Turn ON the alarm
    stateAlarm = true;
    nDetections = 0;

  } else if (c == 'L') {
    //Turn OFF the alarm
    stateAlarm = false;
    nDetections = 0;

  } else if (c == 'W') {
    //Change the state of the window
    if (!auxWindow) {
      wantWindowOpen = true;
      windowIsOpen = true;
      myservo.write(140);
    }
    auxWindow = false;

  } else if (c == 'M') {
    //Change the state of the window
    wantWindowOpen = false;
    windowIsOpen = false;
    myservo.write(35);

  } else if (c == 'D') {
    //Send the values to the serial
    sendValues();
  }
  //}
}

/* +--------------------------+
 * |   START OF THE PROGRAM   |
 * +--------------------------+
 */

void setup() {

  Serial.begin(9600);  // Initialize serial communications with the PC
  while (!Serial)
    ;  // Do nothing if no serial port is opened (added for Arduinos based on ATMEGA32U4)

  rfidConfig();
  ioConfig();
}

void loop() {

  rfidRead();
  raspCommands();

  //Read lightIntensity
  readLightVal();

  if (stateAlarm) {
    //ALARM ON

    digitalWrite(lGreen, HIGH);
    digitalWrite(lRed, LOW);

    //Read pirValue
    pirValue = digitalRead(pirPin);

    while (pirValue == 1) {
      if (oneTime == 0) {
        nDetections++;
        oneTime++;
      }

      digitalWrite(lYellow, HIGH);
      digitalWrite(buzzer, HIGH);
      delay(2);  //wait for 2ms
      digitalWrite(buzzer, LOW);
      delay(2);  //wait for 2ms
      pirValue = digitalRead(pirPin);
    }
    oneTime = 0;
    digitalWrite(lYellow, LOW);
  } else {

    //ALARM OFF

    digitalWrite(lGreen, LOW);
    digitalWrite(lRed, HIGH);

    //Read pirValue
    pirValue = digitalRead(pirPin);

    while (pirValue == 1) {

      if (isNight) {
        digitalWrite(lYellow, HIGH);
      }

      pirValue = digitalRead(pirPin);
    }
    digitalWrite(lYellow, LOW);
  }

  //Read gasValue
  readGasValue();

  //read TempHum
  readTempHum();
}
