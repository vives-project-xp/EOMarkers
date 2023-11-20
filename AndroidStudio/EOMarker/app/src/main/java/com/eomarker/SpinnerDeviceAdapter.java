package com.eomarker;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class SpinnerDeviceAdapter extends ArrayAdapter<Device> {
    private final LayoutInflater inflater;
    private final List<Device> devices;
    private Context context;
    private  InternalStorage internalStorage;

    public SpinnerDeviceAdapter(Activity context, int resouceId, int textviewId, List<Device> devices){
        super(context,resouceId,textviewId, devices);
        inflater = context.getLayoutInflater();
        this.devices = devices;
        this.context = context;
        internalStorage = new InternalStorage(context);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        try {
            if (view == null) {
                view = inflater.inflate(R.layout.spinner_device_item, null, true);
            }
        }catch (Exception e){
            Log.e("spinner", e.toString());
        }

        Device device = devices.get(position);

        TextView macAddressTextView = view.findViewById(R.id.spinner_device_macAddress);
        TextView nameTextView = view.findViewById(R.id.spinner_device_name);

        macAddressTextView.setText(device.macAddress);
        nameTextView.setText(device.name);

        return view;
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
        if(view == null){
            view = inflater.inflate(R.layout.spinner_device_item,parent, false);
        }
        Device device = getItem(position);
        TextView macAddressTextView = view.findViewById(R.id.spinner_device_macAddress);
        TextView nameTextView = view.findViewById(R.id.spinner_device_name);

        macAddressTextView.setText(device.macAddress);
        nameTextView.setText(device.name);
        return view;
    }
}
