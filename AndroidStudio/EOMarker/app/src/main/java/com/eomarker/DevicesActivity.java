package com.eomarker;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.List;

public class DevicesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devices);
        // calling the action bar
        ActionBar actionBar = getSupportActionBar();

        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);

        loadDevices();

    }

    private void loadDevices(
    ) {
        // Create an ArrayList of device objects
        InternalStorage internalStorage = new InternalStorage(this);
        List<Device> devices = internalStorage.getDevices();

        ArrayAdapter<Device> deviceAdapter = new ArrayAdapter<Device>(this, R.layout.device_item, devices);

        // Get a reference to the ListView, and attach the adapter to the listView.
        ListView listView = (ListView) findViewById(R.id.listview_devices);
        listView.setAdapter(deviceAdapter);
    }
}