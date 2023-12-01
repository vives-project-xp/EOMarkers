package com.eomarker.device;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.eomarker.R;
import com.eomarker.activity.MainActivity;
import com.eomarker.mqtt.MqttHandler;
import com.eomarker.storage.InternalStorage;

import java.util.List;

public class DeviceAdapter extends ArrayAdapter<Device>{
    private final LayoutInflater inflater;
    private final List<Device> devices;
    private Context context;
    private InternalStorage internalStorage;

    public DeviceAdapter(Context context, List<Device> devices) {
        super(context, R.layout.device_item, devices);
        this.inflater = LayoutInflater.from(context);
        this.devices = devices;
        this.context = context;
        internalStorage = new InternalStorage(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.device_item, parent, false);
        }

        Device device = devices.get(position);

        TextView macAddressTextView = view.findViewById(R.id.device_macAddress);
        TextView nameTextView = view.findViewById(R.id.device_name);

        macAddressTextView.setText(device.macAddress);
        nameTextView.setText(device.name);

        ImageButton edit =view.findViewById(R.id.device_edit);
        edit.setTag(device);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Device device = (Device) view.getTag();
                editName(device);
            }
        });

        ImageButton delete = view.findViewById(R.id.device_delete);
        delete.setTag(device);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Device device = (Device) view.getTag();
                deleteDevice(device);
            }
        });

        ImageButton visualize = view.findViewById(R.id.device_visualize);
        visualize.setTag(device);
        visualize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Device device = (Device) view.getTag();
                visualizeDevice(device);
            }
        });

        return view;
    }

    private void editName(Device device){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Change friendly name");
        EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(device.name);
        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.leftMargin = 50;
        params.rightMargin = 50;
        input.setLayoutParams(params);
        container.addView(input);
        builder.setView(container);

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String name = input.getText().toString();
                Log.e("device" , device.macAddress);
                try {
                    MqttHandler mqttHandler = MainActivity.getMqttHandler();
                    mqttHandler.publish("PM/EOMarkers/" + device.macAddress.replace(":","") + "/name", name);

                } catch (Exception e) {
                    Log.e("MQTT", "error:" +  e.toString());
                }
                internalStorage.updateDeviceName(device.macAddress, name);
                device.name = name;
                devices.remove(device);
                devices.add(device);
                notifyDataSetChanged();
                Toast.makeText(context, "Updated device name!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    private void deleteDevice(Device device){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to delete this device? " + device.macAddress);
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    InternalStorage internalStorage = new InternalStorage(context);
                    internalStorage.deleteDevice(device);
                    devices.remove(device);
                    Toast.makeText(context, "Device deleted!", Toast.LENGTH_SHORT).show();
                    notifyDataSetChanged();
                }catch (Exception e){
                    Toast.makeText(context, "Unable to delete device!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();

    }

    private void visualizeDevice(Device device){
        try {
            MqttHandler mqttHandler = MainActivity.getMqttHandler();
            mqttHandler.publish("PM/EOMarkers/" + device.macAddress.replace(":", "") + "/visualize", "true");
            Toast.makeText(context, "Visualizing device!", Toast.LENGTH_SHORT).show();
        }catch (Exception e){}
    }
}
