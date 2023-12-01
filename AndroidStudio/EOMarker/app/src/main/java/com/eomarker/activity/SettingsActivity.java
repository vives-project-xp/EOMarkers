package com.eomarker.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.eomarker.R;
import com.eomarker.storage.InternalStorage;
import com.eomarker.storage.KeyValueStorage;

public class SettingsActivity extends AppCompatActivity {

    EditText brokerURL;
    EditText userName;
    EditText userPassword;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        context = this;
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

        ImageButton removeData = findViewById(R.id.btn_removeData);
        removeData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alert = new AlertDialog.Builder(context)
                .setTitle("Remove Data")
                        .setMessage("Are you sure you want to delete all data?")
                        .setCancelable(true)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                InternalStorage internalStorage = new InternalStorage(context);
                                internalStorage.deleteData();

                                brokerURL.setText("");
                                userName.setText( "");
                                userPassword.setText("");
                            }
                        }).show();

            }
        });
        Button SaveData = findViewById(R.id.btn_saveData);
        SaveData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveSettings();
                onPause();
            }
        });
    }

    @Override
    protected void onPause() {
        saveSettings();
        super.onPause();
        finish();
    }

    private void saveSettings(){
        KeyValueStorage.saveData(getApplicationContext(), "BROKER_URL", brokerURL.getText().toString());
        KeyValueStorage.saveData(getApplicationContext(), "BROKER_USERNAME", userName.getText().toString());
        KeyValueStorage.saveData(getApplicationContext(), "BROKER_PASSWORD", userPassword.getText().toString());
        Toast.makeText(getApplicationContext(), "Saved sucessfully", Toast.LENGTH_SHORT).show();
    }
}