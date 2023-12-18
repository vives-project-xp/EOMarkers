#pragma once

#include <WiFi.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <EEPROM.h>
#include <RGBWConverter.h>
#include <Adafruit_NeoPixel.h>

#include "config.h"


  extern Adafruit_NeoPixel sk6812;
  extern String friendlyName;
  extern WiFiClient espClient;
  extern PubSubClient client;
  extern String topic;
  extern RGBWConverter converter;

  // Declare callback function
  void callback(char* topic, byte* message, unsigned int length);

  void processRGBMessage(String message);

  void processNameMessage(String message);

  void visualize();

  // Declare sendAlive function
  void sendAlive();

  // Declare convertMac function
  String convertMac();

  // MQTT-configuratie
  void setup_MQTT();

  // MQTT-configuratie
  void loop_MQTT();

  // MQTT-herverbinding
  void reconnect();

  void setColor(int r, int g, int b, int w);

  void saveStringToEEPROM(int address, String data);

  void checkConnection();

