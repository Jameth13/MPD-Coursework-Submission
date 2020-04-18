package com.jamespaton.MPDCoursework;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity {

    protected int _fontSize;
    protected int _language;
    protected boolean _darkmode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("Checkpoint", "onCreate");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("MyPref", 0);

        if (prefs.getBoolean("Dark Mode", false)) {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            setTheme(R.style.DarkTheme);
            _darkmode = true;
        }
        else {
            getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            setTheme(R.style.LightTheme);
            _darkmode = false;
        }

        switch (prefs.getInt("Font Size", 1)){
            case 0:
                setTheme(R.style.FontSmall);
                break;
            case 1:
                setTheme(R.style.FontMedium);
                break;
            case 2:
                setTheme(R.style.FontLarge);
                break;
        }

        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        android.content.res.Configuration conf = resources.getConfiguration();

        switch (prefs.getInt("Language", 0)) {
            case 0:
                conf.setLocale(new Locale("en"));
                resources.updateConfiguration(conf, displayMetrics);
                break;
            case 1:
                conf.setLocale(new Locale("hr"));
                resources.updateConfiguration(conf, displayMetrics);
                break;
            case 2:
                conf.setLocale(new Locale("de"));
                resources.updateConfiguration(conf, displayMetrics);
                break;
        }

        //Signature
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frameLayoutSignature, new SignatureFragment())
                .commit();

        setContentView(R.layout.activity_base);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menusettings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Log.e("Action Bar", "Settings pressed");
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            //Default case is the back button.
            default:
                finish();
                return true;
        }
    }
}
