#include <Adafruit_NeoPixel.h>
#include <WiFi.h>
#include <PubSubClient.h> 
#include <EEPROM.h>
#include "config.h"
using namespace EOMarker;
#define PIN 10  // The ESP32 pin GPIO16 connected to sk6812
#define NUM_PIXELS 24   // The number of LEDs (pixels) on sk6812 LED strip
Adafruit_NeoPixel sk6812(NUM_PIXELS, PIN, NEO_GRBW + NEO_KHZ800);
WiFiClient espClient;
PubSubClient client(espClient);

 String topic = Config::MQTT_BASE_TOPIC;

#define RGB_COUNT     1
#define RGB_PIN       8
#define RGB_CHANNEL   0

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
     setup_wifi();
    client.setServer(Config::MQTT_BROKER, Config::MQTT_PORT);
    client.setCallback(callback);
  }

bool prevVal = false;

void loop() {
  if (!client.connected()) {
    reconnect();
  }
  client.loop();
  int val = digitalRead(sensorpin);
  if(val != prevVal){
    prevVal = val;
    if(val == 1){
      setColor(r,g,b,w);
      sendAlive();
    }else{
      setColor(0,0,0,0);
    }
  }
      sk6812.show();  
}
void setup_wifi() {
  delay(10);
  // We start by connecting to a WiFi network
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(Config::WIFI_SSID);
  WiFi.begin(Config::WIFI_SSID, Config::WIFI_PASS);
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
<<<<<<< HEAD

=======
  /*DynamicJsonDocument doc(1024);
  deserializeJson(doc, message);
    r = doc["r"];
    g = doc["g"];
    b = doc["b"];
    w = doc["w"];
    */
>>>>>>> db4a30fa31592e55e7ccc16d2d71a9d9116053a6
    char* ptr = strtok((char*)message, ",");  // delimiter
    char *colors[3]; // an array of pointers to the pieces of the above array after strtok()
    byte index = 0;
     while (ptr != NULL)
     {
        colors[index] = ptr;
        index++;
        ptr = strtok(NULL, ",");
     }
     r = atoi(colors[0]);
     g = atoi(colors[1]);
     b = atoi(colors[2]);
    Serial.println(r);
    Serial.println(g);
    Serial.println(b);

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
    Serial.print("Connecting: ");
    Serial.print(WiFi.macAddress().c_str());
    Serial.print(" : ");
    Serial.print(Config::MQTT_PASS);
    if (client.connect(WiFi.macAddress().c_str(), WiFi.macAddress().c_str(), Config::MQTT_PASS)) {
      Serial.println(" connected");
      // Subscribe
      client.subscribe((topic + '/' + WiFi.macAddress() + "/rgb").c_str());
      sendAlive();
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

void sendAlive(){
        client.publish((topic + "/alive").c_str() , WiFi.macAddress().c_str());
}
