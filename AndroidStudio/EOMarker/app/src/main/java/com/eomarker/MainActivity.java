package com.eomarker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.skydoves.colorpickerview.listeners.ColorListener;

public class MainActivity extends AppCompatActivity {

    private static String BROKER_URL = "";
    private static String CLIENT_ID = "";
    private static String CLIENT_PASS = "";
    private MqttHandler mqttHandler;
    


    EditText red;
    EditText green;
    EditText blue;
    EditText white;
    EditText macAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mqttHandler = new MqttHandler();

        BROKER_URL = "tcp://" + KeyValueStorage.loadData(getApplicationContext(), "BROKER_URL", getResources().getString(R.string.MQTT_BROKER));
        CLIENT_ID = KeyValueStorage.loadData(getApplicationContext(), "BROKER_USERNAME", getResources().getString(R.string.MQTT_BROKER));
        CLIENT_PASS = KeyValueStorage.loadData(getApplicationContext(), "BROKER_PASSWORD", getResources().getString(R.string.MQTT_BROKER));

        ConnectMQTT();
        macAddress = findViewById(R.id.MacAddress);
        ColorPickerView colorPickerView = findViewById(R.id.colorPickerView);
        colorPickerView.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope color, boolean fromUser) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    updateColor(String.valueOf(Color.red(color.getColor())),String.valueOf(Color.green(color.getColor())),String.valueOf(Color.blue(color.getColor())), "0");
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        mqttHandler.disconnect();
        super.onDestroy();

    }
    private void publishMessage(String topic, String message){
        if(mqttHandler.connected()) {
            mqttHandler.publish(topic, message);
        }
    }
    private void subscribeToTopic(String topic){
        mqttHandler.subscribe(topic);
    }

    private void updateColor(String red, String green, String blue, String white){
        mqttHandler.connect(BROKER_URL,CLIENT_ID, CLIENT_PASS);

        try{
            if(mqttHandler.connected()) {
                getSupportActionBar().setTitle("EOMarker (connected)");
            }else{
                getSupportActionBar().setTitle("EOMarker (disconnected)");
            }}catch (Exception e){

        }
        String color = red + "," + green + "," + blue;
        String mac = macAddress.getText().toString().replace(":","");
        publishMessage("PM/EOMarkers/" + mac  + "/rgb", color);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if(id == R.id.menu_settings){
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void ConnectMQTT(){
        try {
            mqttHandler.connect(BROKER_URL, CLIENT_ID, CLIENT_PASS);
        }catch (Exception e){
            Log.e("MQTT", e.toString());
        }
    }
}