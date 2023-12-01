#include <Adafruit_NeoPixel.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <EEPROM.h>
#include "config.h"
#include <RGBWConverter.h>
#include <ArduinoJson.h>
#define WIFI_TIMEOUT_MS 20000

// Namespace voor configuratievariabelen
using namespace EOMarker;
// Definieer hardware-gerelateerde constanten
#define PIN 10            // De ESP32-pin GPIO16 verbonden met sk6812                                                           ################ ESP-C3 V1
#define NUM_PIXELS 24    // Het aantal LED's (pixels) op de sk6812 LED-strip
Adafruit_NeoPixel sk6812(NUM_PIXELS, PIN, NEO_GRBW + NEO_KHZ800);

// WiFi- en MQTT-clientinstellingen
WiFiClient espClient;
PubSubClient client(espClient);
String topic = Config::MQTT_BASE_TOPIC;
String friendlyName = "";


RGBWConverter converter(250, 250, 250, true);
// Pin voor sensorinput en kleurvariabelen
int sensorpin = 1; //                                                                                                            ################ ESP-C3 V1
int r = 0;
int g = 0;
int b = 0;
int w = 0;

bool prevVal = false;

void setup() {
  Serial.begin(115200);
  pinMode(sensorpin, INPUT);

  // Initialiseer sk6812 LED-strip
  sk6812.begin();

  // EEPROM-initialisatie en ophalen van opgeslagen kleurwaarden
  EEPROM.begin(512);
  r = EEPROM.read(0);
  g = EEPROM.read(1);
  b = EEPROM.read(2);
  w = EEPROM.read(3);
  friendlyName = readStringFromEEPROM(4);
  if(friendlyName.startsWith("name:")){
    friendlyName = friendlyName.substring(5); // Verwijder "name:" van de tekenreeks
    }else{
        friendlyName = "new Device";
  }
  if(r == 0 && g == 0 && b == 0){
    r = 255;
    g = 255;
    b = 255;
  }

  Serial.println("Lezen van EEPROM:");
  Serial.println("Rood: " + String(r));
  Serial.println("Groen: " + String(g));
  Serial.println("Blauw: " + String(b));
  Serial.println("Name: " + String(friendlyName));

  xTaskCreate(
    &checkSensor,  //function name
    "Keep Wifi Alive", //task name
    10240, //stack size increase above 1000 for connect to wifi
    NULL, //task parameters
    1, //priority
    NULL //task handle
  ); 

    // WiFi- en MQTT-configuratie
  setup_wifi();
  client.setServer(Config::MQTT_BROKER, Config::MQTT_PORT);
  client.setCallback(callback);
}

void loop() {
  client.loop();
}

// WiFi-configuratie
void setup_wifi() { 
  //Wifi setup
  xTaskCreatePinnedToCore(
    keepWifiAlive,  //function name
    "Keep Wifi Alive", //task name
    5000, //stack size increase above 1000 for connect to wifi
    NULL, //task parameters
    2, //priority
    NULL, //task handle
    CONFIG_ARDUINO_RUNNING_CORE //CPU Core 
  );
}

// MQTT-berichtverwerking
void callback(char* topic, byte* message, unsigned int length) {
  String topicString = String(topic);
      // Convert byte array to String
    String messageString;
    for (int i = 0; i < length; i++) {
        messageString += (char)message[i];
    }
  Serial.print("Bericht ontvangen op onderwerp: ");
  Serial.print(topic);
  Serial.print(". Bericht: ");

  // Toon het ontvangen bericht
  for (int i = 0; i < length; i++) {
    Serial.print((char)message[i]);
  }
  Serial.println();
  Serial.println(topic);
  if(topicString.indexOf("rgb") > 0){
    char copy[256];  // Maak een kopie van het bericht om strtok veilig te gebruiken
    strncpy(copy, (char*)message, length);
    copy[length] = '\0';  // Zorg ervoor dat de kopie een null-terminator heeft
    
    // Parse het ontvangen bericht om kleurwaarden te verkrijgen
    char* ptr = strtok(copy, ",");
    char *colors[3]; // Een array van pointers naar de delen van het bovenstaande array na strtok()
    colors[0] = 0;
    colors[1] = 0;
    colors[2] = 0;
  
    byte index = 0;
  
    // Splits het bericht op in RGB-kleurwaarden
    while (ptr != NULL) {
      colors[index] = ptr;
      index++;
      ptr = strtok(NULL, ",");
    }
  
    // Converteer de kleurwaarden naar integer
    r = atoi(colors[0]);
    g = atoi(colors[1]);
    b = atoi(colors[2]);
  
    //rgb to rgbw
    auto c = converter.RGBToRGBW(r, g, b);
    r = c.r;
    g = c.g;
    b = c.b;
    w = c.w;
  
    Serial.println(r);
    Serial.println(g);
    Serial.println(b);
    Serial.println(w);
  
    // Schrijf kleurwaarden naar EEPROM
    EEPROM.write(0, r);
    EEPROM.write(1, g);
    EEPROM.write(2, b);
    EEPROM.write(3, w);
    EEPROM.commit();
  
    // Stel de LED-kleur in
    setColor(r, g, b, w);
  }
  if(topicString.indexOf("name") > 0){
    friendlyName = messageString;
    Serial.println(friendlyName);
      saveStringToEEPROM(4, "name:" + friendlyName);
    Serial.println(friendlyName);
    EEPROM.commit();
  }

   if(topicString.indexOf("visualize") > 0){
    visualize();
   }
}

void visualize(){
  for(int i = 0; i < 10; i++){
    setColor(255,255,255,255);
    delay(500);
    setColor(0,0,0,0);
    delay(500);
  }
}


// MQTT-herverbinding
void reconnect() {
  // Blijf proberen totdat we opnieuw verbonden zijn
  while (!client.connected()) {
    Serial.print("Poging tot MQTT-verbinding...");
    Serial.print("Verbinding maken: ");
    Serial.print(WiFi.macAddress().c_str());
    Serial.print(" : ");
    Serial.print(Config::MQTT_PASS);

    // Probeer opnieuw verbinding te maken met MQTT-broker
    if (client.connect(WiFi.macAddress().c_str(), WiFi.macAddress().c_str(), Config::MQTT_PASS)) {
      Serial.println(" verbonden");
      // Abonneer op het juiste onderwerp
      client.subscribe((topic + '/' + convertMac() + "/rgb").c_str());
      client.subscribe((topic + '/' + convertMac() + "/name").c_str());
      client.subscribe((topic + '/' + convertMac() + "/visualize").c_str());
      sendAlive();
    } else {
      Serial.print("Mislukt, rc=");
      Serial.print(client.state());
      Serial.println(" probeer opnieuw over 5 seconden");
      delay(5000); // Wacht 5 seconden voordat u opnieuw probeert
    }
  }
}

// Stel de LED-kleur in
void setColor(int r, int g, int b, int w) {
  for (int pixel = 0; pixel < NUM_PIXELS; pixel++) {
    sk6812.setPixelColor(pixel, sk6812.Color(r, g, b, w));
  }
}

// Stuur "alive" bericht naar MQTT-broker
void sendAlive() {
  String currentMac = WiFi.macAddress();

  // Controleer of het MAC-adres het juiste formaat heeft (12 karakters, hexadecimaal)
  if (currentMac.length() == 17) {
    StaticJsonDocument<200> device;
    device["macAddress"] = currentMac.c_str();
    device["name"] = friendlyName;
    
    char out[128];
    serializeJson(device, out);
    client.publish((topic + "/alive").c_str(), out);
  } else {
    Serial.println("Ongeldig MAC-adres formaat. Bericht niet verzonden.");
  }
}
// Hulpmethode om MAC-adres te converteren naar een leesbaar formaat
String convertMac() {
  String macAddress = WiFi.macAddress();
  String cleanedMac = "";

  // Loop door elk teken van het MAC-adres
  for (int i = 0; i < macAddress.length(); i++) {
    char c = macAddress.charAt(i);

    // Voeg tekens toe aan de schoongemaakte string als ze geen ":" zijn
    if (c != ':') {
      cleanedMac += c;
    }
  }

  return cleanedMac;
}


//------------------------------------WIFI KEEP ALIVE CODE--------------------------------------------------------
void keepWifiAlive(void * parameters){ 
  for(;;){
    //if connected do nothing and quit
    if(WiFi.status() == WL_CONNECTED){
      Serial.print("WiFi still connected: "); Serial.println(WiFi.localIP().toString().c_str());
      vTaskDelay(10000 / portTICK_PERIOD_MS);
      continue;
    }
    //initiate connection

       StaticJsonDocument<1024> jsonLogins;
    DeserializationError error = deserializeJson(jsonLogins, Config::LOGINS);
    
    if (error) {
      Serial.print("Fout tijdens het analyseren van JSON: ");
      Serial.println(error.c_str());
      return;
    }
    
    Serial.println("Wifi Connecting");
    Serial.println(Config::WIFI_SSID);
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
    WiFi.begin(Config::WIFI_SSID, jsonLogins[String(WiFi.macAddress())].as<const char*>());
    unsigned long startAttemptTime = millis();

    //Indicate to the user that we are not currently connected but are trying to connect.
    while (WiFi.status() != WL_CONNECTED && millis() - startAttemptTime < WIFI_TIMEOUT_MS){
      Serial.print(".");
      vTaskDelay(500 / portTICK_PERIOD_MS);
      vTaskDelay(500 / portTICK_PERIOD_MS);
      continue;
    }
    //Indicate to the user the outcome of our attempt to connect.
    if(WiFi.status() == WL_CONNECTED){
      Serial.print("[WIFI] Connected: "); Serial.println(WiFi.localIP().toString().c_str());
      reconnect();
    } else {
      Serial.println("[WIFI] Failed to Connect before timeout");
    }
  }
}  
//---------------------------------END OF WIFI KEEP ALIVE CODE--------------------------------------------------------

//---------------------------------CHECK SENSOR INPUT-----------------------------------------------------------------

void checkSensor(void * parameters){
    for(;;){
  // Lees de sensorwaarde
  int val = digitalRead(sensorpin);
  // Vergelijk huidige sensorwaarde met vorige waarde
  if (val != prevVal) {
    prevVal = val;
    if (val == 1) {
      // Zet LED-kleur en stuur "alive" bericht als sensor is geactiveerd
      setColor(r, g, b, w);
      sendAlive();
    } else {
      // Zet LED-kleur uit als sensor niet is geactiveerd
      setColor(0, 0, 0, 0);
    }
  }
  // Toon de LED-kleuren op de sk6812 LED-strip
  sk6812.show();
    }
}

//---------------------------------END CHECK SENSOR INPUT-------------------------------------------------------------

void saveStringToEEPROM(int address, String data) {
  int length = data.length();

  for (int i = 0; i < length; i++) {
    EEPROM.write(address + i, data[i]);
  }

  EEPROM.write(address + length, '\0'); // Null-terminator om het einde van de string aan te geven
  EEPROM.commit(); // Belangrijk: om de wijzigingen op te slaan
}

String readStringFromEEPROM(int address) {
  String data;

  for (int i = 0; i < 100; i++) {  // Maximaal aantal tekens om te lezen om oneindige lussen te voorkomen
    char readChar = EEPROM.read(address + i);
    if (readChar == '\0') {
      break;  // Stoppen bij het bereiken van het nul-terminatiekarakter
    }

    // Print de hexadecimale waarde van de byte
    Serial.print("Byte ");
    Serial.print(i);
    Serial.print(": 0x");
    Serial.println(readChar, HEX);

    data += readChar;
  }

  Serial.println("Data gelezen van EEPROM: " + data);

  return data;
}
