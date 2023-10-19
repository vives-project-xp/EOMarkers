#include <Adafruit_NeoPixel.h>
#include <WiFi.h>
#include <PubSubClient.h>
#include <EEPROM>
#include "config.h"

// Namespace voor configuratievariabelen
using namespace EOMarker;

// Definieer hardware-gerelateerde constanten
#define PIN 10            // De ESP32-pin GPIO16 verbonden met sk6812
#define NUM_PIXELS 24     // Het aantal LED's (pixels) op de sk6812 LED-strip
Adafruit_NeoPixel sk6812(NUM_PIXELS, PIN, NEO_GRBW + NEO_KHZ800);

// WiFi- en MQTT-clientinstellingen
WiFiClient espClient;
PubSubClient client(espClient);
String topic = Config::MQTT_BASE_TOPIC;

// RGB-pin en kanaal voor sensorinvoer
#define RGB_COUNT 1
#define RGB_PIN 8
#define RGB_CHANNEL 0

// Pin voor sensorinput en kleurvariabelen
int sensorpin = 1;
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
  EEPROM.begin(4);
  r = EEPROM.read(0);
  g = EEPROM.read(1);
  b = EEPROM.read(2);
  w = EEPROM.read(3);

  Serial.println("Lezen van EEPROM:");
  Serial.println("Rood: " + String(r));
  Serial.println("Groen: " + String(g));
  Serial.println("Blauw: " + String(b));

  // WiFi- en MQTT-configuratie
  setup_wifi();
  client.setServer(Config::MQTT_BROKER, Config::MQTT_PORT);
  client.setCallback(callback);
}

void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop();

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

// WiFi-configuratie
void setup_wifi() {
  delay(10);
  Serial.println();
  Serial.print("Verbinden met ");
  Serial.println(Config::WIFI_SSID);
  WiFi.begin(Config::WIFI_SSID, Config::WIFI_PASS);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("");
  Serial.println("WiFi verbonden");
  Serial.println("IP-adres: ");
  Serial.println(WiFi.localIP());
}

// MQTT-berichtverwerking
void callback(char* topic, byte* message, unsigned int length) {
  Serial.print("Bericht ontvangen op onderwerp: ");
  Serial.print(topic);
  Serial.print(". Bericht: ");

  // Toon het ontvangen bericht
  for (int i = 0; i < length; i++) {
    Serial.print((char)message[i]);
  }
  Serial.println();

  // Parse het ontvangen bericht om kleurwaarden te verkrijgen
  char* ptr = strtok((char*)message, ",");  // Delimiter
  char *colors[3]; // Een array van pointers naar de delen van het bovenstaande array na strtok()
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

  // Schrijf kleurwaarden naar EEPROM
  EEPROM.write(0, r);
  EEPROM.write(1, g);
  EEPROM.write(2, b);
  EEPROM.write(3, w);
  EEPROM.commit();

  // Stel de LED-kleur in
  setColor(r, g, b, w);
  sk6812.show(); // Toon de nieuwe kleuren op de LED-strip
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
    sk6812.setPixelColor(pixel, sk6812.Color(r, g, b, 0));
  }
}

// Stuur "alive" bericht naar MQTT-broker
void sendAlive() {
  client.publish((topic + "/alive").c_str(), WiFi.macAddress().c_str());
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
