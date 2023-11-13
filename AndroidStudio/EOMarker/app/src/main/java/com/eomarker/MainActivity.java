package com.eomarker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static String BROKER_URL = "";
    private static String CLIENT_ID = "";
    private static String CLIENT_PASS = "";
    private MqttHandler mqttHandler;

    String macAddress = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mqttHandler = new MqttHandler();

        BROKER_URL = "tcp://" + KeyValueStorage.loadData(getApplicationContext(), "BROKER_URL", getResources().getString(R.string.MQTT_BROKER));
        CLIENT_ID = KeyValueStorage.loadData(getApplicationContext(), "BROKER_USERNAME", getResources().getString(R.string.MQTT_BROKER));
        CLIENT_PASS = KeyValueStorage.loadData(getApplicationContext(), "BROKER_PASSWORD", getResources().getString(R.string.MQTT_BROKER));

        ConnectMQTT();
        ColorPickerView colorPickerView = findViewById(R.id.colorPickerView);
        colorPickerView.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope color, boolean fromUser) {
                Spinner spinnerDevices = findViewById(R.id.spinner_devices);
                try {
                    macAddress = spinnerDevices.getSelectedItem().toString();
                }catch (Exception e){

                }
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    updateColor(String.valueOf(Color.red(color.getColor())),String.valueOf(Color.green(color.getColor())),String.valueOf(Color.blue(color.getColor())), "0");
                }
            }
        });

        loadDevices();
    }

    public void loadDevices() {
        Spinner spinnerDevices = findViewById(R.id.spinner_devices);
        InternalStorage internalStorage = new InternalStorage(this);
        List<Device> devices = internalStorage.getDevices();
        ArrayAdapter<Device> adapter = new ArrayAdapter<Device>(this, android.R.layout.simple_spinner_dropdown_item, devices);
        spinnerDevices.setAdapter(adapter);
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


    private void updateColor(String red, String green, String blue, String white){
        try{
            if(mqttHandler.connected()) {
                getSupportActionBar().setTitle("EOMarker (connected)");
            }else{
                getSupportActionBar().setTitle("EOMarker (disconnected)");
            }}catch (Exception e){

        }
        String color = red + "," + green + "," + blue;
        try {
            String mac = macAddress.toString().replace(":", "");
            publishMessage("PM/EOMarkers/" + mac + "/rgb", color);
        }catch (Exception e){

        }
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
        if(id == R.id.menu_reload){
            Toast.makeText(this, "Reloading...", Toast.LENGTH_SHORT).show();
            ConnectMQTT();
            loadDevices();
        }
        if(id == R.id.menu_devices){
            Intent devicesIntent = new Intent(MainActivity.this, DevicesActivity.class);
            startActivity(devicesIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void ConnectMQTT(){
        try {
            mqttHandler.connect(BROKER_URL, CLIENT_ID, CLIENT_PASS, this);
        }catch (Exception e){
            Log.e("MQTT", e.toString());
        }
    }
}