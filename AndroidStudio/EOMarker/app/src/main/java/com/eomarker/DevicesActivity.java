package com.eomarker;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

public class DevicesActivity extends AppCompatActivity implements DeviceDiscoveryListener {
    private List<Device> devices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        // calling the action bar
        ActionBar actionBar = getSupportActionBar();

        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);

        MqttHandler mqttHandler = MainActivity.getMqttHandler();
        mqttHandler.setDeviceDiscoveryListener(this);
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadDevices();
                pullToRefresh.setRefreshing(false);
            }
        });
        loadDevices();
        ImageButton visualizeAll = findViewById(R.id.btn_visualizeAll);
        visualizeAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadDevices();
                Toast.makeText(getApplicationContext(), "Visualizing all devices!", Toast.LENGTH_SHORT).show();
                for (Device device: devices) {
                try {
                    MqttHandler mqttHandler = MainActivity.getMqttHandler();
                    mqttHandler.publish("PM/EOMarkers/" + device.macAddress.replace(":", "") + "/visualize", "true");
                }catch (Exception e){}
            }

            }
        });
    }

    private void loadDevices(
    ) {
        // Create an ArrayList of device objects
        InternalStorage internalStorage = new InternalStorage(this);
        devices = internalStorage.getDevices();
        DeviceAdapter deviceAdapter = new DeviceAdapter(this, devices);

        ListView listView = findViewById(R.id.listview_devices);
        listView.setAdapter(deviceAdapter);
    }

    @Override
    public void onDeviceDiscovered() {
        loadDevices();
    }
}