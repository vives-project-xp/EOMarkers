#pragma once
#include <Arduino.h>

#define PIN 10
#define NUM_PIXELS 24

namespace EOMarker_Config {
  
   static const char * WIFI_SSID = "YOUR_SSID";
   static const String WIFI_PASS = "{'MAC1':'PASS1','MAC2':'PASS2'}";
   static const char * MQTT_BROKER = "YOUR_BROKER.URL";
   static const char * MQTT_PASS = "YOUR_SECRET_PASSWORD";
   static const char * MQTT_LOGIN = "YOUR_LOGIN";
   static const unsigned int MQTT_PORT = 1883;
   static const char * MQTT_BASE_TOPIC = "PM/EOMarkers";

};
