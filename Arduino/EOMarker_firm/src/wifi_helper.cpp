#include "wifi_helper.h"
#include "mqtt_helper.h"

// WiFi-EOMarker_Configuratie
  void setup_wifi() {
    //Wifi setup
    xTaskCreatePinnedToCore(
      keepWifiAlive,               //function name
      "Keep Wifi Alive",           //task name
      5000,                        //stack size increase above 1000 for connect to wifi
      NULL,                        //task parameters
      2,                           //priority
      NULL,                        //task handle
      CONFIG_ARDUINO_RUNNING_CORE  //CPU Core
    );
  }

  //------------------------------------WIFI KEEP ALIVE CODE--------------------------------------------------------
  void keepWifiAlive(void *parameters) {
    for (;;) {
      //if connected do nothing and quit
      if (WiFi.status() == WL_CONNECTED) {
        Serial.print("WiFi still connected: ");
        Serial.println(WiFi.localIP().toString().c_str());
        vTaskDelay(10000 / portTICK_PERIOD_MS);
        checkConnection();
        continue;
      }
      //initiate connection

      StaticJsonDocument<1024> jsonLogins;
      DeserializationError error = deserializeJson(jsonLogins, EOMarker_Config::WIFI_PASS);
    
      if (error) {
        Serial.print("Fout tijdens het analyseren van JSON: ");
        Serial.println(error.c_str());
        return;
      }

      Serial.println("Wifi Connecting");
      Serial.println(EOMarker_Config::WIFI_SSID);
      Serial.println(String(WiFi.macAddress()));

      // Controleer of de sleutel bestaat voordat je de waarde probeert af te drukken
      if (jsonLogins.containsKey(String(WiFi.macAddress()))) {
        Serial.print("Value for ");
        Serial.print(WiFi.macAddress());
        Serial.print(": ");
        Serial.println(jsonLogins[String(WiFi.macAddress())].as<const char*>());
      } else {
        Serial.println("Key not found in JSON document");
        return;
      }

      WiFi.mode(WIFI_STA);
      Serial.println("Wifi Connecting");

      WiFi.begin(EOMarker_Config::WIFI_SSID, jsonLogins[String(WiFi.macAddress())].as<const char*>());
      //WiFi.begin(EOMarker_Config::WIFI_SSID, EOMarker_Config::WIFI_PASS);
      unsigned long startAttemptTime = millis();

      //Indicate to the user that we are not currently connected but are trying to connect.
      while (WiFi.status() != WL_CONNECTED && millis() - startAttemptTime < WIFI_TIMEOUT_MS) {
        Serial.print(".");
        vTaskDelay(500 / portTICK_PERIOD_MS);
        vTaskDelay(500 / portTICK_PERIOD_MS);
        continue;
      }
      //Indicate to the user the outcome of our attempt to connect.
      if (WiFi.status() == WL_CONNECTED) {
        Serial.print("[WIFI] Connected: ");
        Serial.println(WiFi.localIP().toString().c_str());

        Serial.println("trying Connecting to MQTT");
        reconnect();

      } else {
        Serial.println("[WIFI] Failed to Connect before timeout");
      }
    }
  }
//---------------------------------END OF WIFI KEEP ALIVE CODE--------------------------------------------------------
