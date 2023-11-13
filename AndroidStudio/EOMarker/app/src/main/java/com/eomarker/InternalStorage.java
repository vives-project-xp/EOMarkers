package com.eomarker;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InternalStorage {
    private File storageFile;

    public InternalStorage(Context context) {
        storageFile = new File(context.getFilesDir(), "devices.json");
        if (!storageFile.exists()) {
            try {
                storageFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveDevice(Device device) {
        try {
            JSONArray jsonArray = readJsonArrayFromFile();
            JSONObject deviceObject = new JSONObject();
            deviceObject.put("macAddress", device.macAddress);
            deviceObject.put("name", device.name);
            jsonArray.put(deviceObject);

            FileOutputStream fos = new FileOutputStream(storageFile);
            fos.write(jsonArray.toString().getBytes());
            fos.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    public List<Device> getDevices() {
        List<Device> devices = new ArrayList<>();
        try {
            JSONArray jsonArray = readJsonArrayFromFile();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject deviceObject = jsonArray.getJSONObject(i);
                String macAddress = deviceObject.getString("macAddress");
                String name = deviceObject.getString("name");
                devices.add(new Device(macAddress, name));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return devices;
    }


    private JSONArray readJsonArrayFromFile() {
        JSONArray jsonArray = new JSONArray();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(storageFile));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            reader.close();

            jsonArray = new JSONArray(stringBuilder.toString());
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }
}
