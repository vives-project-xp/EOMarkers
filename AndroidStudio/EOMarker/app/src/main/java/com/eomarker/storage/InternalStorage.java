package com.eomarker.storage;

import android.content.Context;
import android.widget.Toast;

import com.eomarker.device.Device;

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
    Context context;

    public InternalStorage(Context _context) {
        context = _context;
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

    public void updateDeviceName(String macAddress, String newName) {
        try {
            JSONArray jsonArray = readJsonArrayFromFile();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject deviceObject = jsonArray.getJSONObject(i);
                String deviceMacAddress = deviceObject.getString("macAddress");

                // Controleer of het macAdres overeenkomt met het gezochte apparaat
                if (deviceMacAddress.equals(macAddress)) {
                    // Wijzig de naam van het apparaat
                    deviceObject.put("name", newName);

                    // Sla de bijgewerkte array terug op
                    FileOutputStream fos = new FileOutputStream(storageFile);
                    fos.write(jsonArray.toString().getBytes());
                    fos.close();
                    return; // Stop de loop omdat het apparaat is gevonden en bijgewerkt
                }
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
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

    public void deleteData() {
        try{
            storageFile = new File(context.getFilesDir(), "devices.json");
            storageFile.delete();
            Toast.makeText(context, "Data deleted!", Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(context, "Unable to delete data...", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteDevice(Device device) {
        try {
            JSONArray jsonArray = readJsonArrayFromFile();
            JSONArray updatedArray = new JSONArray();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject deviceObject = jsonArray.getJSONObject(i);
                String deviceMacAddress = deviceObject.getString("macAddress");

                // Als het macAdres overeenkomt met het te verwijderen apparaat, sla het niet op in de bijgewerkte array
                if (!deviceMacAddress.equals(device.macAddress)) {
                    updatedArray.put(deviceObject);
                }
            }

            // Overschrijf het opslaan van de bijgewerkte array
            FileOutputStream fos = new FileOutputStream(storageFile);
            fos.write(updatedArray.toString().getBytes());
            fos.close();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

}