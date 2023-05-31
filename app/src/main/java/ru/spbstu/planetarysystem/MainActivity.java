package ru.spbstu.planetarysystem;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final double delayScaler = 1.2;
    private static int yearOrSo = 365;
    private static double coeff = 1.6602739726027398; // yearOrSo(606)/365 -> intDay * coeff = day
    private static final double zoomScaler = 1.1;
    private static final int BACKGROUND_COLOR = Color.argb(255, 0, 0, 0);
    private static List<CelestialBody> celestialBodiesDefault = Arrays.asList(
            new CelestialBody("Mercury", 0.387, 0.206, 0.241, 5.1, 0f, 1.0),
            new CelestialBody("Venus", 0.723, 0.007, 0.615, 1.4, 0.0f, 1.0),
            new CelestialBody("Earth", 1.0, 0.017, 1.0, 1.2, 0f, 1.0),
            new CelestialBody("Mars", 1.524, 0.093, 1.881, 1.6, 100f, 1.0),
            new CelestialBody("Jupiter", 5.203, 0.048, 11.86, 1.2, 0f, 1.0),
            new CelestialBody("Saturn", 9.54, 0.056, 29.46, 4.4, 0f, 1.0),
            new CelestialBody("Uranus", 19.18, 0.047, 84.01, 1.2, 0f, 1.0),
            new CelestialBody("Neptune", 30.06, 0.009, 164.8, 2.0, 0f, 1.0),
            new CelestialBody("Pluto", 39.53, 0.248, 248.5, 5.6, 200f, 1.0),
            new CelestialBody("2008 VB4", 2.35, 0.617, 3.61, 3.1, 70f, 1.0),
            new CelestialBody("2009 FG", 1.97, 0.529, 2.76, 3.1, -45f, 1.0),
            new CelestialBody("Halley", 17.83, 0.967, 75.32, 3.1, 115f, -1.0)
    );
    private static LinkedList<CelestialBody> newBodies = new LinkedList<>();
    private KeplerRunner krunner;
    private SharedPreferences sharedPreferences;
    private int timeJump;
    Toolbar toolbar;
    LinearLayout LL1;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        newBodies.addAll(celestialBodiesDefault);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        // Get the path to the internal storage directory
        // Apply the settings to the app
        //applySettings(settingsData);
        File file = new File(getFilesDir(), "settings.json");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileInputStream fis = openFileInput("settings.json");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            isr.close();
            fis.close();
            String settingsJson = sb.toString();
            CelestialBody settingsData = new Gson().fromJson(settingsJson, CelestialBody.class);
            Log.e("ERROR", settingsData.toString() + " || " + newBodies);
            newBodies.add(settingsData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Get the int value
        timeJump = sharedPreferences.getInt("timeJump", yearOrSo);

        /* In the following we lay out the screen entirely in code (activity_main.xml
         * isn't used).  We wish to lay out a stage for planetary motion using LinearLayout.
         * The instance krunner of KeplerRunner is added to a LinearLayout LL1 using addView.
         * Then we use setContent to set the content view to LL1. The formatting of the layouts
         * is controlled using the LinearLayout.LayoutParams lp.
         * */

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 100);

        LL1 = new LinearLayout(this);
        LL1.setOrientation(LinearLayout.VERTICAL);

        // Create top Toolbar using code rather than xml.  Note that this assumes that in styles.xml
        // a no action bar theme is set: <style name="AppTheme" parent=".NoActionBar">

        // Instantiate a Toolbar from its constructor and add properties to it
        toolbar = new Toolbar(this);
        toolbar.setBackgroundColor(getResources().getColor(R.color.md_theme_light_primary, null));

        toolbar.setNavigationIcon(R.drawable.ic_app);
        toolbar.setTitle("");

        // Attach the toolbar to the view
        LL1.addView(toolbar);

        // Set the toolbar as the ActionBar for this window
        setSupportActionBar(toolbar);

        // Instantiate the class MotionRunner to define the entry screen display and add it
        // to the view.

        krunner = new KeplerRunner(this, newBodies);
        krunner.setLayoutParams(lp);
        krunner.setBackgroundColor(BACKGROUND_COLOR);
        LL1.addView(krunner);

        // Set the view as the display

        setContentView(LL1);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the toolbar menu
        toolbar.inflateMenu(R.menu.main);

        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop animation loop if going into background
        krunner.stopLooper();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Resume animation loop
        krunner.startLooper();
        Log.d("ANIM", "onResume");

    }

    @Override
    public void onRestart() {
        super.onRestart();
        Log.d("ANIM", "onRestart");
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        // Get the int value
        timeJump = sharedPreferences.getInt("timeJump", yearOrSo);
    }

    // Process action bar menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("ANIM", "OptionItemsSelected");
        // Handle item selection
        int id = item.getItemId();

        if (id == R.id.change_direction) {
            krunner.changeDirection();
            return true;
        }
        // Run slower
        else if (id == R.id.speed_decrease) {
            krunner.setDelay(delayScaler);
            //krunner.addNsteps((int) (100 * delayScaler));
            return true;
            // Run faster
        } else if (id == R.id.speed_increase) {
            long test = krunner.setDelay(1 / delayScaler);
            //krunner.subNsteps((int) (100 * delayScaler)); // if (krunner.getNsteps() <= 36) {
            // Method setDelay() returns -2 if new delay would be < 1
            if (test == -2) {
                Toast.makeText(this, "Maximum speed. Can't increase",
                        Toast.LENGTH_SHORT).show();
            }
            return true;
            // zoom Out
        } else if (id == R.id.zoom_out) {
            krunner.setZoom(1 / zoomScaler);
        } else if (id == R.id.zoom_in) {
            krunner.setZoom(zoomScaler);
            return true;
            //toggle labels
        } else if (id == R.id.toggle_labels) {
            krunner.showLabels = !krunner.showLabels;
        } else if (id == R.id.timeJump) {
            Log.i("SHARED PREFS", "Time Jump = " + timeJump + "* " + coeff);
            krunner.timeJump((int) (timeJump * coeff));
        } else if (id == R.id.action_settings) {
            //
            Intent i = new Intent(this, MySettings.class);
            this.startActivity(i);
            return true;
        } else return super.onOptionsItemSelected(item);
        Log.w("MAIN ACTIVITY", "All if-elses are missed");
        return true;
    }
}
