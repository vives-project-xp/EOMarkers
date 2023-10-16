package com.eomarker;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerView;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;
import com.skydoves.colorpickerview.listeners.ColorListener;

public class MainActivity extends AppCompatActivity {

    private static final String BROKER_URL = "tcp://" + R.string.MQTT_BROKER;
    private static final String CLIENT_ID = "EOMarker";
    private MqttHandler mqttHandler;
    EditText red;
    EditText green;
    EditText blue;
    EditText white;
    EditText macAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mqttHandler = new MqttHandler();
        mqttHandler.connect(BROKER_URL,CLIENT_ID);
         red = findViewById(R.id.colorRed);
         green = findViewById(R.id.colorGreen);
         blue = findViewById(R.id.colorBlue);
         white = findViewById(R.id.colorWhite);
         macAddress = findViewById(R.id.MacAddress);
        ColorPickerView colorPickerView = findViewById(R.id.colorPickerView);
        colorPickerView.setColorListener(new ColorEnvelopeListener() {
            @Override
            public void onColorSelected(ColorEnvelope color, boolean fromUser) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    updateColor(String.valueOf(Color.red(color.getColor())),String.valueOf(Color.green(color.getColor())),String.valueOf(Color.blue(color.getColor())), "0");
                }
            }
        });

        Button btnPublish = findViewById(R.id.btnPublish);
        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateColor( red.getText().toString(), green.getText().toString(), blue.getText().toString(), white.getText().toString());
            }
        });
    }

    @Override
    protected void onDestroy() {
        mqttHandler.disconnect();
        super.onDestroy();

    }
    private void publishMessage(String topic, String message){
        Toast.makeText(this, "Publishing message: " + message, Toast.LENGTH_SHORT).show();
        mqttHandler.publish(topic,message);
    }
    private void subscribeToTopic(String topic){
        Toast.makeText(this, "Subscribing to topic "+ topic, Toast.LENGTH_SHORT).show();
        mqttHandler.subscribe(topic);
    }

    private void updateColor(String red, String green, String blue, String white){


        String color = "{r:" + red + ",g:" + green + ",b:" + blue +",w:" + white + "}";

        publishMessage("EOMarker/" + macAddress.getText() + "/color", color);
    }
}