package com.eomarker;

import androidx.activity.OnBackPressedDispatcher;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.ActionMode;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    EditText brokerURL;
    EditText userName;
    EditText userPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        // calling the action bar
        ActionBar actionBar = getSupportActionBar();

        // showing the back button in action bar
        actionBar.setDisplayHomeAsUpEnabled(true);

        brokerURL = findViewById(R.id.brokerURL);
        userName = findViewById(R.id.userName);
        userPassword = findViewById(R.id.userPassword);

        brokerURL.setText(KeyValueStorage.loadData(getApplicationContext(), "BROKER_URL", ""));
        userName.setText(KeyValueStorage.loadData(getApplicationContext(), "BROKER_USERNAME", ""));
        userPassword.setText(KeyValueStorage.loadData(getApplicationContext(), "BROKER_PASSWORD", ""));

    }


    @Override
    protected void onPause() {
        saveSettings();
        super.onPause();
    }

    private void saveSettings(){
        KeyValueStorage.saveData(getApplicationContext(), "BROKER_URL", brokerURL.getText().toString());
        KeyValueStorage.saveData(getApplicationContext(), "BROKER_USERNAME", userName.getText().toString());
        KeyValueStorage.saveData(getApplicationContext(), "BROKER_PASSWORD", userPassword.getText().toString());
        Toast.makeText(getApplicationContext(), "Saved sucessfully", Toast.LENGTH_SHORT).show();
    }
}