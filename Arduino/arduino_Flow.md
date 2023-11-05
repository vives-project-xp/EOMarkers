# code flow

```
+----------------------------------------+
|  Global Variable Declarations  
|    Include Libraries     
|    Define Constants     
|    Create NeoPixel Object  
+----------------------------------------+
        |
        v
+----------------------------------------+
|          setup() Function            
|    Initialize Serial                
|    Set Sensor Pin as Input         
|    Initialize NeoPixel LED Strip   
|    Initialize EEPROM and Read Color
|    Create checkSensor Task         
|    Configure WiFi and MQTT         
+----------------------------------------+
        |
        v
+----------------------------------------+
|         loop() Function              
|      MQTT Client Loop                
+----------------------------------------+
        |
        v
+----------------------------------------+
|        setup_wifi() Function      
|    Create keepWifiAlive Task       
+----------------------------------------+
        |
        v
+----------------------------------------+
|       callback() Function            
|   Handle MQTT Messages              
|   Parse and Convert Colors         
|   Write to EEPROM                   
|   Set LED Color and Show           
+----------------------------------------+
        |
        v
+----------------------------------------+
|      reconnect() Function         
|   MQTT Reconnection Logic          
+----------------------------------------+
        |
        v
+----------------------------------------+
|      setColor() Function          
|   Set LED Color                    
+----------------------------------------+
        |
        v
+----------------------------------------+
|      sendAlive() Function         
|   Publish "alive" Message         
+----------------------------------------+
        |
        v
+----------------------------------------+
|      convertMac() Function       
|   Convert MAC Address              
+----------------------------------------+
        |
        v
+----------------------------------------+
|    keepWifiAlive() Function     
|   Ensure Continuous WiFi          
|   Connect and Reconnect          
+----------------------------------------+
        |
        v
+----------------------------------------+
|    checkSensor() Function         
|   Continuous Sensor Monitoring   
|   Update LED and Publish         
+----------------------------------------+
```