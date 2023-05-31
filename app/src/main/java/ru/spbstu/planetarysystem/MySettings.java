package ru.spbstu.planetarysystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.google.gson.Gson;

public class MySettings extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_settings);
        Button buttonApplyTimeJump = findViewById(R.id.button_apply_time_jump);
        buttonApplyTimeJump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText textView = findViewById(R.id.etxt_time_jump);
                int timeJump = Integer.parseInt(textView.getText().toString());
                Log.i("SHARED PREFS", " +" + timeJump);
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                // Get the editor object to make changes to SharedPreferences
                SharedPreferences.Editor editor = sharedPreferences.edit();
                // Put the integer value in SharedPreferences
                editor.putInt("timeJump", timeJump);
                // Commit the changes
                editor.apply();
                Toast.makeText(getApplicationContext(), "Applied", Toast.LENGTH_SHORT).show();
            }
        });
        Button buttonReset = findViewById(R.id.button_reset);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the SharedPreference instance
                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

                // Get the editor to edit the SharedPreference
                SharedPreferences.Editor editor = sharedPreferences.edit();

                // Remove the SharedPreference
                editor.remove("timeJump");
                // Commit the changes
                editor.apply();
                Toast.makeText(getApplicationContext(), "All your data removed", Toast.LENGTH_SHORT).show();
                Log.w("RESET BUTTON", "Now your data is" + sharedPreferences.getAll());
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    private void saveSettings() {
        SettingsData settingsData = new SettingsData();
        settingsData.setSetting1("value1");
        settingsData.setSetting2(123);
        // Convert the settings data to JSON format
        String settingsJson = new Gson().toJson(settingsData);
        try {
            FileOutputStream fos = openFileOutput("settings.json", Context.MODE_PRIVATE);
            fos.write(settingsJson.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}