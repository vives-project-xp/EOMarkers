#include <Adafruit_NeoPixel.h>
#include <WiFi.h>
#include <PubSubClient.h> 
#include "Freenove_WS2812_Lib_for_ESP32.h"
#include <ArduinoJson.h>
#include <EEPROM.h>

#define PIN 10  // The ESP32 pin GPIO16 connected to sk6812
#define NUM_PIXELS 24   // The number of LEDs (pixels) on sk6812 LED strip

Adafruit_NeoPixel sk6812(NUM_PIXELS, PIN, NEO_GRBW + NEO_KHZ800);

const char* ssid = "SSID";
const char* password = "PASSWORD";
const char* mqtt_server = "BROKER";

const String topic = "EOMarker";

WiFiClient espClient;
PubSubClient client(espClient);

#define RGB_COUNT     1
#define RGB_PIN       8
#define RGB_CHANNEL   0
Freenove_ESP32_WS2812 strip = Freenove_ESP32_WS2812(RGB_COUNT, RGB_PIN, RGB_CHANNEL, TYPE_GRB);

int sensorpin = 1;
int r = 0;
int g = 0;
int b = 0;
int w = 0;


void setup() {
  Serial.begin(115200);
  pinMode(sensorpin, INPUT);
   sk6812.begin();  // initialize sk6812 strip object (REQUIRED)
     EEPROM.begin(4);
   r = EEPROM.read(0);
   g = EEPROM.read(1);
   b = EEPROM.read(2);
   w = EEPROM.read(3);

   Serial.print("reading from eeprom");
   Serial.println(r);
    strip.setLedColorData(0,255,0,0);
    strip.show();
     setup_wifi();
    client.setServer(mqtt_server, 1883);
    client.setCallback(callback);
  }

void loop() {
  /*if (Serial.available() > 0) {
  String rgb = Serial.readString();
  Serial.println(rgb);
  for(int pixel = 0; pixel < NUM_PIXELS ; pixel++){
    sk6812.setPixelColor(pixel, sk6812.Color(getValue(rgb, ';', 0).toInt(), getValue(rgb, ';', 1).toInt(), getValue(rgb, ';', 2).toInt(),getValue(rgb, ';', 3).toInt()));  // it only takes effect if pixels.show() is called
  }
        sk6812.show();   
        // update to the sk6812 Led Strip
  }*/

  if (!client.connected()) {
    strip.setLedColorData(255,255,0,0);
    strip.show();
    reconnect();
  }
    strip.setLedColorData(0,255,0,0);
    strip.show();
  client.loop();
  int val = digitalRead(sensorpin);
  if(val == 1){
    setColor(r,g,b,w);
  }else{
    setColor(0,0,0,0);
  }
      sk6812.show();  
}

void setup_wifi() {
  delay(10);
  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(ssid);

  WiFi.begin(ssid, password);

  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }

  Serial.println("");
  Serial.println("WiFi connected");
  Serial.println("IP address: ");
  Serial.println(WiFi.localIP());
}

void callback(char* topic, byte* message, unsigned int length) {
  Serial.print("Message arrived on topic: ");
  Serial.print(topic);
  Serial.print(". Message: ");
  
  for (int i = 0; i < length; i++) {
    Serial.print((char)message[i]);
  }
  Serial.println();
  DynamicJsonDocument doc(1024);
  deserializeJson(doc, message);
  r = doc["r"];
  g = doc["g"];
  b = doc["b"];
  w = doc["w"];
  EEPROM.write(0, r);
  EEPROM.write(1, g);
  EEPROM.write(2,b);
  EEPROM.write(3, w);

  EEPROM.commit();
  setColor(r,g,b,w);
  
        sk6812.show();   
        // update to the sk6812 Led Strip
}

void reconnect() {
  // Loop until we're reconnected
  while (!client.connected()) {
    Serial.print("Attempting MQTT connection...");
    // Attempt to connect
    if (client.connect("ESP8266Client")) {
      Serial.println("connected");
      // Subscribe
      client.subscribe((topic + '/' + WiFi.macAddress() + "/color").c_str());
      client.publish((topic + '/'  + WiFi.macAddress() + "/alive").c_str() , "true");
    } else {
      Serial.print("failed, rc=");
      Serial.print(client.state());
      Serial.println(" try again in 5 seconds");
      // Wait 5 seconds before retrying
      delay(5000);
    }
  }
}

void setColor(int r, int g, int b , int w){
    for(int pixel = 0; pixel < NUM_PIXELS ; pixel++){
      sk6812.setPixelColor(pixel, sk6812.Color(r,g,b,w));  // it only takes effect if pixels.show() is called
}
}
