package ru.spbstu.planetarysystem;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
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

        // Your code to get the settings data here

        // Convert the settings data to JSON format
        JSONObject settingsJson = new JSONObject();
        try {
            settingsJson.put("setting1", setting1Value);
            settingsJson.put("setting2", setting2Value);
            // Add more settings as needed
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON object", e);
        }

        // Save the JSON data to internal storage
        saveJsonToFile(this, "settings.json", settingsJson.toString());
    }

    private void saveJsonToFile(Context context, String fileName, String json) {
        FileOutputStream outputStream;
        try {
            outputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE);
            outputStream.write(json.getBytes(StandardCharsets.UTF_8));
            outputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "Error saving JSON file", e);
        }
    }
}