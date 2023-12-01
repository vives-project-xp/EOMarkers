package com.eomarker.mqtt;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.eomarker.activity.MainActivity;
import com.eomarker.device.Device;
import com.eomarker.device.DeviceDiscoveryListener;
import com.eomarker.storage.InternalStorage;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONObject;

import java.util.List;

public class MqttHandler {

    private MqttClient client = null;
    public boolean isConnected = false;
    private Context context;
    MainActivity main;
    private DeviceDiscoveryListener deviceDiscoveryListener;

    public void connect(String brokerUrl, String clientId, String clientPass, Context _context, MainActivity _main) {
        context = _context;
        main = _main;
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
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.i("MQTT", "connection lost, reconnecting... " + cause);
                    //main.ConnectMQTT();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    InternalStorage internalStorage = new InternalStorage(context);
                    JSONObject json = new JSONObject(new String(message.getPayload()));
                    Device device = new Device(json.getString("macAddress"), json.getString("name"));
                    List<Device> devices = internalStorage.getDevices();
                    boolean newDevice = true;
                    for (Device d: devices) {
                        if(d.macAddress.equals(device.macAddress)){
                            if(d.name.equals(device.name)) {
                                newDevice = false;
                            }else{
                                internalStorage.updateDeviceName(device.macAddress, d.name);
                            }
                        }
                    }
                    if(newDevice) {
                        try {
                            internalStorage.saveDevice(device);
                            main.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    Toast.makeText(context, "New device discovered!", Toast.LENGTH_SHORT).show();
                                    onNewDeviceDiscovered();
                                }
                            });
                        }catch (Exception e){}
                    }

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.i("MQTT", "msg delivered");
                }
            });
            subscribeToTopic();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDeviceDiscoveryListener(DeviceDiscoveryListener listener) {
        this.deviceDiscoveryListener = listener;
    }

    private void onNewDeviceDiscovered() {
        if (deviceDiscoveryListener != null) {
            deviceDiscoveryListener.onDeviceDiscovered();
        }
    }

    public void subscribeToTopic() {
        String topic = "PM/EOMarkers/alive";
        subscribe(topic, 0, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Log.e("MQTT", "subscribed succeed");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.e("MQTT", "subscribed failed");
            }
        });

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
            if (client != null && client.isConnected()) {
                MqttMessage mqttMessage = new MqttMessage(message.getBytes());
                client.publish(topic, mqttMessage);
            } else {
                Log.e("MQTT", "Client not connected or null");
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    public void subscribe(String topic, int i, Object o, IMqttActionListener iMqttActionListener) {
        if(client.isConnected()){
            Log.e("mqtt", "connected, starting sub");
        }else{
            Log.e("MQTT", "Not connected");
        }
        try {
            client.subscribe(topic);
            Log.e("MQTT", "Subscribing to topic: " + topic);
        } catch (MqttException e) {
            Log.e("MQTT", "Unable to subscribe to topic: " + topic);
            e.printStackTrace();
        }
    }

}
