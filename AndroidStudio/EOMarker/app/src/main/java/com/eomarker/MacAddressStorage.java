package com.eomarker;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MacAddressStorage {
    private File storageFile;

    public MacAddressStorage(Context context) {
        storageFile = new File(context.getFilesDir(), "mac_addresses.txt");
        if (!storageFile.exists()) {
            try {
                storageFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveMacAddress(String macAddress) {
        try {
            FileOutputStream fos = new FileOutputStream(storageFile, true);
            String data = macAddress + "\n";
            fos.write(data.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<String> getMacAddresses(Context context) {
        Log.e("MQTT", "reading");
        List<String> macAddresses = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.openFileInput("mac_addresses.txt")));
            String line;
            while ((line = reader.readLine()) != null) {
                macAddresses.add(line);
            }
            reader.close();
            Log.e("MQTT", "readed");
        } catch (IOException e) {
            Log.e("MQTT", "unable to read macadresses");
            e.printStackTrace();
        }
        return macAddresses;
    }
}
