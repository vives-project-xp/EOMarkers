package com.eomarker.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.eomarker.R;
import com.eomarker.device.Device;
import com.eomarker.device.DeviceDiscoveryListener;
import com.eomarker.device.SpinnerDeviceAdapter;
import com.eomarker.mqtt.MqttHandler;
import com.eomarker.storage.InternalStorage;
import com.eomarker.storage.KeyValueStorage;
import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

import java.util.List;

public class MainActivity extends AppCompatActivity implements DeviceDiscoveryListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static String BROKER_URL = "";
    private static String CLIENT_ID = "";
    private static String CLIENT_PASS = "";
    private static MqttHandler mqttHandler;
    private boolean firstStart = true;

    String macAddress = "";
    private List<Device> devices;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mqttHandler = new MqttHandler();
        mqttHandler.setDeviceDiscoveryListener(this);


        initializeBrokerInfo();
        ConnectMQTT();
        updateStatus();
        setupColorPicker();
        setupPullToRefresh();
        loadDevices();
    }

    private void initializeBrokerInfo() {
        BROKER_URL = "tcp://" + KeyValueStorage.loadData(getApplicationContext(), "BROKER_URL", getResources().getString(R.string.MQTT_BROKER));
        CLIENT_ID = KeyValueStorage.loadData(getApplicationContext(), "BROKER_USERNAME", getResources().getString(R.string.MQTT_BROKER));
        CLIENT_PASS = KeyValueStorage.loadData(getApplicationContext(), "BROKER_PASSWORD", getResources().getString(R.string.MQTT_BROKER));
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupColorPicker() {
        SwipeRefreshLayout swipeRefreshLayout = findViewById(R.id.pullToRefresh);
        ColorPickerView colorPickerView = findViewById(R.id.colorPickerView);
        colorPickerView.setOnTouchListener((v, event) -> {
            int action = event.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    // Start veegbeweging
                    swipeRefreshLayout.setEnabled(false);
                    break;
                case MotionEvent.ACTION_UP:
                    // Eind veegbeweging
                    swipeRefreshLayout.setEnabled(true);
                    break;
            }
            return false;
        });
        colorPickerView.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope color, boolean fromUser) {
                if (!firstStart) {
                    CheckBox ColorAll = findViewById(R.id.CBColorAll);
                    if(ColorAll.isChecked()){
                        handleColorSelectionAll(color);
                    }else {
                        handleColorSelection(color);
                    }
                }
                firstStart = false;
            }
        });
    }

    private void setupPullToRefresh() {
        SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(() -> {
            loadDevices();
            pullToRefresh.setRefreshing(false);
        });
    }

    private void handleColorSelection(ColorEnvelope color) {
        Spinner spinnerDevices = findViewById(R.id.spinner_devices);
        try {
            Device device = (Device) spinnerDevices.getSelectedItem();
            macAddress = device.macAddress;
        } catch (Exception e) {
            Log.e(TAG, "Error getting selected device", e);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            updateColor(String.valueOf(Color.red(color.getColor())), String.valueOf(Color.green(color.getColor())), String.valueOf(Color.blue(color.getColor())), "0");
        }
    }

    private void handleColorSelectionAll(ColorEnvelope color) {
        loadDevices();
        for(Device device : devices){
            macAddress = device.macAddress;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                updateColor(String.valueOf(Color.red(color.getColor())), String.valueOf(Color.green(color.getColor())), String.valueOf(Color.blue(color.getColor())), "0");
            }
        }
    }

    public void loadDevices() {
        Spinner spinnerDevices = findViewById(R.id.spinner_devices);
        InternalStorage internalStorage = new InternalStorage(this);
        devices = internalStorage.getDevices();

        SpinnerDeviceAdapter adapter = new SpinnerDeviceAdapter(this, R.layout.spinner_device_item, R.id.spinner_device_macAddress, devices);
        spinnerDevices.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        mqttHandler.disconnect();
        super.onDestroy();
    }

    private void publishMessage(String topic, String message) {
        if (mqttHandler.connected()) {
            mqttHandler.publish(topic, message);
        }else {
            ConnectMQTT();
            publishMessage(topic, message);
        }
    }

    private void updateColor(String red, String green, String blue, String white) {
        updateStatus();
        String color = red + "," + green + "," + blue;
        try {
            String mac = macAddress.replace(":", "");
            publishMessage("PM/EOMarkers/" + mac + "/rgb", color);
        } catch (Exception e) {
            Log.e(TAG, "Error updating color", e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_settings) {
            Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(settingsIntent);
        }
        if (id == R.id.menu_reload) {
            Toast.makeText(this, "Reloading...", Toast.LENGTH_SHORT).show();
            ConnectMQTT();
            loadDevices();
        }
        if (id == R.id.menu_devices) {
            Intent devicesIntent = new Intent(MainActivity.this, DevicesActivity.class);
            startActivity(devicesIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    public void ConnectMQTT() {
        try {
            mqttHandler.disconnect();
            initializeBrokerInfo();
            mqttHandler.connect(BROKER_URL, CLIENT_ID, CLIENT_PASS, this, this);
            updateStatus();
        } catch (Exception e) {
            Log.e(TAG, "MQTT connection error", e);
        }
    }

    public void updateStatus() {
        try {
            if (mqttHandler.connected()) {
                getSupportActionBar().setTitle("EOMarker (connected)");
            } else {
                getSupportActionBar().setTitle("EOMarker (disconnected)");
            }
        } catch (Exception e) {
            getSupportActionBar().setTitle("EOMarker (no broker connection)");
            //Log.e(TAG, "Error updating status", e);
        }
    }

    public static MqttHandler getMqttHandler() {
        return mqttHandler;
    }

    @Override
    public void onDeviceDiscovered() {
        loadDevices();
    }
}