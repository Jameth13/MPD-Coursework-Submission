//James Paton S1111175

package com.jamespaton.MPDCoursework;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private Editor editor;

    private Switch switchDarkMode;
    private Spinner spinnerFontSize;
    private ArrayAdapter spinnerFontSizeAdapter;
    private Spinner spinnerLanguage;
    private ArrayAdapter spinnerLanguageAdapter;

    private int fontSize = -1;
    private int language = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = prefs.edit();

        if (prefs.getBoolean("Dark Mode", false))
            setTheme(R.style.DarkTheme);
        else
            setTheme(R.style.LightTheme);

        fontSize = prefs.getInt("Font Size", 1);
        switch (fontSize) {
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

        language = prefs.getInt("Language", 0);
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        android.content.res.Configuration conf = resources.getConfiguration();

        switch (language) {
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

        //Set view
        setContentView(R.layout.activity_settings);

        //Switch
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchDarkMode.setChecked(prefs.getBoolean("Dark Mode", false));

        switchDarkMode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.e("DarkMode", Boolean.toString(isChecked));

                editor.putBoolean("Dark Mode", isChecked);
                editor.commit();

                if (isChecked)
                    getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                else
                    getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        //Spinner, Font Size
        spinnerFontSize = findViewById(R.id.spinnerFontSize);
        final String[] spinnerFontSizes = {getString(R.string.small), getString(R.string.medium), getString(R.string.large)};
        spinnerFontSizeAdapter = new ArrayAdapter<>(this, R.layout.spinner_normal, spinnerFontSizes);
        spinnerFontSize.setAdapter(spinnerFontSizeAdapter);

        if (fontSize == -1)
            spinnerFontSize.setSelection(0);
        else
            spinnerFontSize.setSelection(fontSize);

        spinnerFontSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                boolean spinnerChanged = false;

                switch (i) {
                    case 0:
                        if (fontSize == 0)
                            return;
                        spinnerChanged = true;
                        fontSize = 0;
                        //Note: COMPLEX_UNIT_PX used instead of COMPLEX_UNIT_SP as getDimension() already multiplies the value.
                        spinnerFontSizeAdapter.setDropDownViewResource(R.layout.spinner_small);
                        break;
                    case 1:
                        if (fontSize == 1)
                            return;
                        spinnerChanged = true;
                        fontSize = 1;
                        //Note: COMPLEX_UNIT_PX used instead of COMPLEX_UNIT_SP as getDimension() already multiplies the value.
                        spinnerFontSizeAdapter.setDropDownViewResource(R.layout.spinner_normal);
                        break;
                    case 2:
                        if (fontSize == 2)
                            return;
                        spinnerChanged = true;
                        fontSize = 2;
                        spinnerFontSizeAdapter.setDropDownViewResource(R.layout.spinner_large);
                        break;
                }

                if (spinnerChanged) {
                    editor.putInt("Font Size", fontSize);
                    editor.commit();

                    //Reload the activity and disable the transition animation.
                    Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        //Spinner, Language
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        final String[] spinnerLanguages = {"English", "Hrvatski", "Deutsche"};
        spinnerLanguageAdapter = new ArrayAdapter<>(this, R.layout.spinner_normal, spinnerLanguages);
        spinnerLanguage.setAdapter(spinnerLanguageAdapter);

        if (language == -1)
            spinnerLanguage.setSelection(0);
        else
            spinnerLanguage.setSelection(language);

        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                boolean spinnerChanged = false;

                //Change locale.
                Resources resources = getResources();
                DisplayMetrics displayMetrics = resources.getDisplayMetrics();
                android.content.res.Configuration conf = resources.getConfiguration();

                switch (i) {
                    case 0:
                        if (language == 0)
                            return;
                        spinnerChanged = true;
                        language = 0;
                        conf.setLocale(new Locale("en"));
                        resources.updateConfiguration(conf, displayMetrics);
                        break;
                    case 1:
                        if (language == 1)
                            return;
                        spinnerChanged = true;
                        language = 1;
                        conf.setLocale(new Locale("hr"));
                        resources.updateConfiguration(conf, displayMetrics);
                        break;
                    case 2:
                        if (language == 2)
                            return;
                        spinnerChanged = true;
                        language = 2;
                        conf.setLocale(new Locale("de"));
                        resources.updateConfiguration(conf, displayMetrics);
                        break;
                }

                if (spinnerChanged) {
                    editor.putInt("Language", language);
                    editor.commit();

                    //Reload the activity and disable the transition animation.
                    Intent intent = new Intent(SettingsActivity.this, SettingsActivity.class);
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                }
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        //Capture the back button press and close activity.
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
