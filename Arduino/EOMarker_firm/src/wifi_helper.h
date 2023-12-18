#pragma once

#include <WiFi.h>
#include <ArduinoJson.h>

#include "mqtt_helper.h"
#include "config.h"

#define WIFI_TIMEOUT_MS 20000

// Function prototype
void keepWifiAlive(void *parameters);

// WiFi-configuratie
void setup_wifi();