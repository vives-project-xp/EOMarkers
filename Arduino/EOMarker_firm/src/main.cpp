#include <Arduino.h>
#include <EEPROM.h>

#include "wifi_helper.h"
#include "mqtt_helper.h"
#include "eeprom_reader.h"

  // Pin voor sensorinput en kleurvariabelen
  int sensorpin = 1; //                                                                                                            ################ ESP-C3 V1
  int r = 0;
  int g = 0;
  int b = 0;
  int w = 0;

  bool prevVal = false;
  String readStringFromEEPROM(int address);
  void checkSensor(void * parameters);
  void readColors();

  void setup() {
    Serial.begin(115200);
    pinMode(sensorpin, INPUT);

    // Initialiseer sk6812 LED-strip
    sk6812.begin();

    // EEPROM-initialisatie en ophalen van opgeslagen kleurwaarden
    EEPROM.begin(512);
    readColors(); 
    friendlyName = getFriendlyName();
    if(r == 0 && g == 0 && b == 0){
      r = 255;
      g = 255;
      b = 255;
    }

    xTaskCreate(
      &checkSensor,  //function name
      "Keep Wifi Alive", //task name
      10240, //stack size increase above 1000 for connect to wifi
      NULL, //task parameters
      1, //priority
      NULL //task handle
    );

    // WiFi- en MQTT- configuratie setup
    setup_wifi();
    setup_MQTT();
  }

  void loop() {
    loop_MQTT();
  }


  //---------------------------------CHECK SENSOR INPUT-----------------------------------------------------------------
 
  void checkSensor(void * parameters){
      for(;;){
    // Lees de sensorwaarde
    int val = digitalRead(sensorpin);
    // Vergelijk huidige sensorwaarde met vorige waarde
    
    if (val != prevVal) {
      prevVal = val;
      switch (val)
      {
        case 1:
           readColors();
          setColor(r, g, b, w);
          sendAlive();
        break;
        case 0:
          setColor(0, 0, 0, 0);
        break;
      }
    }
    }
  }

  //---------------------------------END CHECK SENSOR INPUT-------------------------------------------------------------

void readColors(){
    r = readStringFromEEPROM(0).toInt();
    g = readStringFromEEPROM(4).toInt();
    b = readStringFromEEPROM(8).toInt();
    w = readStringFromEEPROM(12).toInt();
}