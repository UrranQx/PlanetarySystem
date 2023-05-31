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

import java.io.FileInputStream;

public class MainActivity extends AppCompatActivity {

    private static final double delayScaler = 1.2;
    private static int yearOrSo = 365;
    private static double coeff = 1.6602739726027398; // yearOrSo(606)/365 -> intDay * coeff = day
    private static final double zoomScaler = 1.1;
    private static final int BACKGROUND_COLOR = Color.argb(255, 0, 0, 0);
    private KeplerRunner krunner;
    private SharedPreferences sharedPreferences;
    private int timeJump;
    Toolbar toolbar;
    LinearLayout LL1;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        // Load the JSON string from the settings file in internal storage
        String filename = "settings.json";
        FileInputStream inputStream;
        String json = "";
        try {
            inputStream = openFileInput(filename);
            int c;
            while ((c = inputStream.read()) != -1) {
                json += Character.toString((char) c);
            }
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Convert the JSON string to a SettingsData object
        Gson gson = new Gson();
        SettingsData settingsData = gson.fromJson(json, SettingsData.class);

        // Apply the settings to the app
        //applySettings(settingsData);

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

        krunner = new KeplerRunner(this);
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
            Log.i("SHARED PREFS","Time Jump = "+ timeJump + "* " + coeff);
            krunner.timeJump((int) (timeJump*coeff));
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
