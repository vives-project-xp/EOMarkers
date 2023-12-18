#include "eeprom_reader.h"

String readStringFromEEPROM(int address) {
    String data;
  
    for (int i = 0; i < 100; i++) {  // Maximaal aantal tekens om te lezen om oneindige lussen te voorkomen
     char readChar = EEPROM.read(address + i);
     if (readChar == '#') {
       break;  // Stoppen bij het bereiken van het nul-terminatiekarakter
     }  
      data += readChar;
    }
  
    //Serial.println("Data gelezen van EEPROM: " + data + " op adres: " + String(address) + "");
  
    return data;
  }

String getFriendlyName(){
  String friendlyName = readStringFromEEPROM(16);
    if(friendlyName.startsWith("name:")){
      friendlyName = friendlyName.substring(5); // Verwijder "name:" van de tekenreeks
      }else{
          friendlyName = "new Device";
    }

    return friendlyName;
}