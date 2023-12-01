package com.eomarker.storage;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

public class KeyValueStorage {
    private static final String FILENAME = "data.dat";

    public static void saveData(Context context, String key, String value) {
        HashMap<String, String> data = loadData(context);
        data.put(key, value);
        saveData(context, data);
    }

    public static String loadData(Context context, String key, String defaultValue) {
        HashMap<String, String> data = loadData(context);

        if (data.containsKey(key)) {
            return data.get(key);
        }

        return defaultValue;
    }

    private static void saveData(Context context, HashMap<String, String> data) {
        try {
            File file = new File(context.getFilesDir(), FILENAME);
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(data);
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static HashMap<String, String> loadData(Context context) {
        HashMap<String, String> data = new HashMap<>();

        try {
            File file = new File(context.getFilesDir(), FILENAME);
            if (file.exists()) {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                data = (HashMap<String, String>) ois.readObject();
                ois.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }
}
