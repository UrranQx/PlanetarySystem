package ru.spbstu.planetarysystem;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.FileOutputStream;
import java.io.IOException;

import com.google.gson.Gson;

public class MySettings extends AppCompatActivity {
    private static final String TAG = "SettingsActivity";
    private String celestialBodyName;
    private double eccentricity;
    private double semimajorAxis;
    private double period;
    private float orbitRotation;
    private double initialAngeInRad;
    private double direction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_settings);
        Button buttonApplyTimeJump = findViewById(R.id.button_apply_time_jump);
        buttonApplyTimeJump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText textView = findViewById(R.id.etxt_time_jump);
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
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
                // WE've resetted timeJump variable
                // But what about orbit constructs?
            }
        });
        Button saveNewBody = findViewById(R.id.button_save_body);
        saveNewBody.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // First we need to check whether a body have a Not Empty Name
                // 0=<Ecc<1 if not -> ecc = 0
                // AU <10^5 Just joking
                // direction = 1 | -1 if not -> direction = 1;
                EditText etxtCelestialBodyName = findViewById(R.id.etxt_celestial_body_name);
                EditText etxtEccentricity = findViewById(R.id.etxt_eccentricity);
                EditText etxtSemimajorAxis = findViewById(R.id.etxt_semimajor_axis);
                EditText etxtOrbitRotation = findViewById(R.id.etxt_orient_deg);
                EditText etxtInitialAngleRad = findViewById(R.id.etxt_theta_0);
                EditText etxtDirection = findViewById(R.id.etxt_retro_fac);
                celestialBodyName = etxtCelestialBodyName.getText().toString();
                eccentricity = Double.parseDouble(etxtEccentricity.getText().toString());
                semimajorAxis = Double.parseDouble(etxtSemimajorAxis.getText().toString());
                orbitRotation = Float.parseFloat(etxtOrbitRotation.getText().toString());
                initialAngeInRad = Double.parseDouble(etxtInitialAngleRad.getText().toString());
                direction = Double.parseDouble(etxtDirection.getText().toString());
                if (celestialBodyName.trim().length() == 0) {
                    Toast.makeText(
                                    getApplicationContext(),
                                    "Celestial Body Name is empty",
                                    Toast.LENGTH_SHORT)
                            .show();
                }
                if (eccentricity < 0 || eccentricity >= 1) eccentricity = 0;
                if (semimajorAxis > Math.pow(10, 5)) semimajorAxis = 10 + semimajorAxis % 10;
                direction = direction >= 0d ? 1d : -1d;
                period = Math.pow(semimajorAxis, 3f / 2f);
                saveSettings();
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    private void saveSettings() {
        CelestialBody celestialBody = new CelestialBody(
                celestialBodyName,
                eccentricity,
                semimajorAxis,
                period,
                initialAngeInRad,
                orbitRotation,
                direction

        );
        // Convert the settings data to JSON format
        String settingsJson = new Gson().toJson(celestialBody);
        try {
            FileOutputStream fos = openFileOutput("settings.json", Context.MODE_PRIVATE);
            fos.write(settingsJson.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}