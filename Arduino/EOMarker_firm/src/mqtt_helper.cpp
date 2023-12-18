#include "mqtt_helper.h"
#include "eeprom_reader.h"

using namespace EOMarker_Config;

  Adafruit_NeoPixel sk6812(NUM_PIXELS, PIN, NEO_GRBW + NEO_KHZ800);
  String friendlyName;
  WiFiClient espClient;
  PubSubClient client(espClient);
  String topic = MQTT_BASE_TOPIC;
  RGBWConverter converter(250, 250, 250, true);
  

  // MQTT-configuratie
  void setup_MQTT() {
    //MQTT setup
    client.setServer(MQTT_BROKER, MQTT_PORT);
    client.setCallback(callback);
  }

  // MQTT-configuratie
  void loop_MQTT() {
    client.loop();
  }

  // MQTT-herverbinding
  void reconnect() {
    // Blijf proberen totdat we opnieuw verbonden zijn
    while (!client.connected()) {
      Serial.println("Poging tot MQTT-verbinding...");
      //Serial.println(WiFi.macAddress().c_str() + String(" : ") + MQTT_PASS);
      
      // Probeer opnieuw verbinding te maken met MQTT-broker
      if (client.connect(WiFi.macAddress().c_str(), MQTT_LOGIN, MQTT_PASS)) {
        Serial.println(" verbonden");
        client.setKeepAlive(60);
        // Abonneer op het juiste onderwerp
        client.subscribe((topic + '/' + convertMac() + "/rgb").c_str());
        client.subscribe((topic + '/' + convertMac()  + "/name").c_str());
        client.subscribe((topic + '/'+ convertMac() + "/visualize").c_str());
        sendAlive();
      } else {
        Serial.print("Mislukt, rc=");
        Serial.print(client.state());
        Serial.println(" probeer opnieuw over 5 seconden");
        switch (client.state())
        {
        case -4:
        Serial.println("MQTT_CONNECTION_TIMEOUT - the server didn't respond within the keepalive time");
        break;

    case -3:
        Serial.println("MQTT_CONNECTION_LOST - the network connection was broken");
        break;

    case -2:
        Serial.println("MQTT_CONNECT_FAILED - the network connection failed");
        break;

    case -1:
        Serial.println("MQTT_DISCONNECTED - the client is disconnected cleanly");
        break;

    case 0:
        Serial.println("MQTT_CONNECTED - the client is connected");
        break;

    case 1:
        Serial.println("MQTT_CONNECT_BAD_PROTOCOL - the server doesn't support the requested version of MQTT");
        break;

    case 2:
        Serial.println("MQTT_CONNECT_BAD_CLIENT_ID - the server rejected the client identifier");
        break;

    case 3:
        Serial.println("MQTT_CONNECT_UNAVAILABLE - the server was unable to accept the connection");
        break;

    case 4:
        Serial.println("MQTT_CONNECT_BAD_CREDENTIALS - the username/password were rejected");
        break;

    case 5:
        Serial.println("MQTT_CONNECT_UNAUTHORIZED - the client was not authorized to connect");
        break;

    default:
        Serial.println("Unknown MQTT Status Code");
        break;
        }
        delay(5000); // Wacht 5 seconden voordat u opnieuw probeert
      }
    }
  }

  void checkConnection() {
    if (!client.connected()) {
      Serial.println("MQTT-verbinding verloren. Opnieuw verbinden...");
      reconnect();
    }else{
      Serial.println("MQTT-verbinding nog steeds actief");
    }
  }

  // MQTT-berichtverwerking
  void callback(char* topic, byte* message, unsigned int length) {
    // Convert topic and message to strings
    String topicString = String(topic);
    String messageString(reinterpret_cast<char*>(message), length);

    // Print received topic and message to Serial monitor
    Serial.print("Received message on topic: ");
    Serial.println(topic);
    Serial.print("Message: ");
    Serial.println(messageString);

    // Process RGB Message
    if (topicString.indexOf("rgb") > 0) {
      processRGBMessage(messageString);
    }

    // Process Name Message
    if (topicString.indexOf("name") > 0) {
      processNameMessage(messageString);
    }

    // Process Visualize Message
    if (topicString.indexOf("visualize") > 0) {
      visualize();
    }
  }

  // Function to process RGB messages
  void processRGBMessage(String message) {
    char copy[256];
    strncpy(copy, message.c_str(), sizeof(copy));
    copy[sizeof(copy) - 1] = '\0';

    char* ptr = strtok(copy, ",");
    char* colors[3] = {0};

    byte index = 0;

    // Split the message into RGB color values
    while (ptr != NULL && index < 3) {
      colors[index++] = ptr;
      ptr = strtok(NULL, ",");
    }

    // Convert color values to integers
    int r = atoi(colors[0]);
    int g = atoi(colors[1]);
    int b = atoi(colors[2]);

    // Convert RGB to RGBW
    auto c = converter.RGBToRGBW(r, g, b);
    r = c.r;
    g = c.g;
    b = c.b;
    int w = c.w;

/*
    // Print RGBW values to Serial monitor
    Serial.println("RGBW Values:");
    Serial.println(r);
    Serial.println(g);
    Serial.println(b);
    Serial.println(w);

*/

    // Write color values to EEPROM
    saveStringToEEPROM(0, String(r));
    saveStringToEEPROM(4, String(g));
    saveStringToEEPROM(8, String(b));
    saveStringToEEPROM(12, String(w));

    // Set the LED color
    setColor(r, g, b, w);
    delay(1000);
    setColor(0,0,0,0);
  }

  // Function to process Name messages
  void processNameMessage(String message) {
    friendlyName = message;
    Serial.println("New Friendly Name: " + friendlyName);

    // Save the friendlyName to EEPROM
    saveStringToEEPROM(16, "name:" + friendlyName);
  }

  // Function to visualize
  void visualize() {
    for(int i = 0; i < 10; i++){
    setColor(255,255,255,255);
    delay(500);
    setColor(0,0,0,0);
    delay(500);
    }
  }


  // Stuur "alive" bericht naar MQTT-broker
  void sendAlive() {
    if(!client.connected()){
      Serial.println("Client is not connected");
      return;
    }
    String currentMac = WiFi.macAddress();

    // Controleer of het MAC-adres het juiste formaat heeft (12 karakters, hexadecimaal)
    if (currentMac.length() == 17) {
      StaticJsonDocument<200> device;
      device["macAddress"] = currentMac.c_str();
      device["name"] = getFriendlyName();
      char out[128];
      serializeJson(device, out);
      if (!client.publish((topic + "/alive").c_str(), out)) {
        Serial.print("Failed to publish alive message. Error: ");
        Serial.println(client.getWriteError());      
        }else{
          //Serial.println("Alive message published");
        };
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

  // Stel de LED-kleur in
  void setColor(int r, int g, int b, int w) {
    for (int pixel = 0; pixel < NUM_PIXELS; pixel++) {
      sk6812.setPixelColor(pixel, sk6812.Color(r, g, b, w));
    }
    sk6812.show();
  }

  void saveStringToEEPROM(int address, String data) {
  int length = data.length();

  //Serial.println("Data opslaan in EEPROM: " + data + " op adres: " + String(address) + "");

  for (int i = 0; i < length; i++) {
    EEPROM.write(address + i, data[i]);
  }

  EEPROM.write(address + length, '#'); // Null-terminator om het einde van de string aan te geven
  EEPROM.commit(); // Belangrijk: om de wijzigingen op te slaan
  }

