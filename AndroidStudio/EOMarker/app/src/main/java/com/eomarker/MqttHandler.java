package com.eomarker;

import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttHandler {

    private MqttClient client = null;
    public boolean isConnected = false;

    public void connect(String brokerUrl, String clientId, String clientPass) {
        if(brokerUrl == null || brokerUrl.isEmpty()){
            return;
        }
        try {
            // Initialize the MQTT client
            client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

            // Set up the connection options
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setUserName(clientId);
            connectOptions.setPassword(clientPass.toCharArray());
            connectOptions.setCleanSession(true);

            // Connect to the broker
            client.connect(connectOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean connected(){
        try{
            isConnected = client.isConnected();
        }catch (Exception e){

        }
        return isConnected;
    }

    public void disconnect() {
        if(isConnected) {
            try {
                client.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void publish(String topic, String message) {
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            client.publish(topic, mqttMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic) {
        try {
            client.subscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
