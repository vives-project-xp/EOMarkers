//------------------------------------WIFI KEEP ALIVE CODE--------------------------------------------------------
using namespace EOMarker

void keepWifiAlive(void * parameters){ 
  for(;;){
    //if connected do nothing and quit
    if(WiFi.status() == WL_CONNECTED){
      Serial.print("WiFi still connected: "); Serial.println(WiFi.localIP().toString().c_str());
      digitalWrite(WIFI_STATUS_PIN, HIGH);
      vTaskDelay(10000 / portTICK_PERIOD_MS);
      continue;
    }
    //initiate connection
    Serial.println("Wifi Connecting");
    WiFi.mode(WIFI_STA);
    WiFi.begin(WIFI_NETWORK, WIFI_PASSWORD);
    unsigned long startAttemptTime = millis();

    //Indicate to the user that we are not currently connected but are trying to connect.
    while (WiFi.status() != WL_CONNECTED && millis() - startAttemptTime < WIFI_TIMEOUT_MS){
      digitalWrite(WIFI_STATUS_PIN, HIGH);
      Serial.print(".");
      vTaskDelay(500 / portTICK_PERIOD_MS);
      digitalWrite(WIFI_STATUS_PIN, LOW);
      vTaskDelay(500 / portTICK_PERIOD_MS);
      continue;
    }
    //Indicate to the user the outcome of our attempt to connect.
    if(WiFi.status() == WL_CONNECTED){
      Serial.print("[WIFI] Connected: "); Serial.println(WiFi.localIP().toString().c_str());
      digitalWrite(WIFI_STATUS_PIN, HIGH);
    } else {
      Serial.println("[WIFI] Failed to Connect before timeout");
      digitalWrite(WIFI_STATUS_PIN, LOW);
    }
  }
}  
//---------------------------------END OF WIFI KEEP ALIVE CODE--------------------------------------------------------
