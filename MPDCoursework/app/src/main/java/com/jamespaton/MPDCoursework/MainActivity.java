//James Paton S1111175

package com.jamespaton.MPDCoursework;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.content.Intent;

import java.io.Serializable;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends BaseActivity implements RecyclerAdapter.OnItemListener, ParserCallback {

    //Black Box Testing
    private boolean enableTesting = false;

    //UI
    private Button buttonUpdate;
    private EditText editTextSearch;
    private RecyclerView recyclerView;
    private Button buttonPlanJourney;

    //URL enums
    public enum URL {
        Incidents,
        Roadworks,
        PlannedRoadworks,
        PlanAJourney,
        TestData
    }

    //RSS URLs
    private URL spinnerSelection = URL.Incidents;

    //Recycler view
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    //RSS Item lists
    private List<RSS> rssItemsComplete = new ArrayList<>();
    private List<RSS> rssItemsRefined = new ArrayList<>();

    //Date picker
    private DatePickerWithClearButton datePicker;
    private EditText editTextDate;

    //Plan a Journey
    List<String> directionsRoadNames = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getApplicationContext().getSharedPreferences("MyPref", 0);
        _fontSize = prefs.getInt("Font Size", 1);
        _language = prefs.getInt("Language", 0);

        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);


        Intent intent = getIntent();
        directionsRoadNames = (ArrayList<String>) intent.getSerializableExtra("directionsRoadNames");
        if (directionsRoadNames == null)
            Log.e("Directions Return", "NULL!");
        else
            Log.e("Directions Return", "Not null.");

        //Get all UI
        buttonUpdate = findViewById(R.id.buttonUpdate);
        editTextSearch = findViewById(R.id.editTextSearch);
        recyclerView = findViewById(R.id.recycleView);
        editTextDate = findViewById(R.id.editTextDate);
        buttonPlanJourney = findViewById(R.id.buttonMap);


        //Spinner RSS Feed Selection
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter adapter;
        if (enableTesting) {
            String[] spinnerRSSFeeds = {getString(R.string.current_incidents), getString(R.string.roadworks), getString(R.string.planned_roadworks), getString(R.string.plan_a_journey), "TEST DATA"};
            adapter = new ArrayAdapter<>(this, R.layout.spinner_normal, spinnerRSSFeeds);
        } else {
            String[] spinnerRSSFeeds = {getString(R.string.current_incidents), getString(R.string.roadworks), getString(R.string.planned_roadworks), getString(R.string.plan_a_journey)};
            adapter = new ArrayAdapter<>(this, R.layout.spinner_normal, spinnerRSSFeeds);
        }
        adapter.setDropDownViewResource(R.layout.spinner_normal);
        spinner.setAdapter(adapter);

        spinnerSelection = URL.values()[spinner.getSelectedItemPosition()];

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                //Note: This function is also called when app launches.

                //Enable date for feeds with date information.
                if (URL.values()[i] == URL.PlannedRoadworks || URL.values()[i] == URL.Roadworks || URL.values()[i] == URL.PlanAJourney || URL.values()[i] == URL.TestData)
                    editTextDate.setEnabled(true);
                else
                    editTextDate.setEnabled(false);

                if (URL.values()[i] == URL.PlanAJourney) {
                    //Show Plan a Journey button
                    buttonPlanJourney.setVisibility(view.VISIBLE);
                } else
                    //Hide Plan a Journey button.
                    buttonPlanJourney.setVisibility(view.GONE);

                spinnerSelection = URL.values()[i];
                ParseURL();
                CloseKeyboard();
            }

            public void onNothingSelected(AdapterView<?> adapterView) { }
        });


        //Update button listener
        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParseURL();
                CloseKeyboard();
            }
        });


        //Plan a Journey button listener
        buttonPlanJourney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadMaps();
            }
        });


        //Add search text field listener
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            //Perform search if search text has changed.
            @Override
            public void afterTextChanged(Editable s) {
                Log.e("Checkpoint", "Text Search changed.");
                SearchAndUpdateRecycler();
            }
        });


        //Setup recycler view
        rssItemsRefined.add(new RSS()); //Do I need this?
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new RecyclerAdapter(this, rssItemsRefined, this);
        recyclerView.setAdapter(mAdapter);


        //Add Date Picker listener and its button listeners to date text field.
        editTextDate.setInputType(InputType.TYPE_NULL);
        editTextDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CloseKeyboard();

                //Load date picker on currently entered date, if invalid, load today's date.
                Calendar calendar = Calendar.getInstance();
                SimpleDateFormat sdfSearch = new SimpleDateFormat("dd/MM/yy", Locale.ENGLISH);
                Date date = sdfSearch.parse(editTextDate.getText().toString(), new ParsePosition(0));

                //Prevent crash from invalid date entry.
                if (date != null)
                    calendar.setTime(date);

                datePicker = new DatePickerWithClearButton(MainActivity.this, new DatePickerWithClearButton.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            editTextDate.setText(String.format("%02d" , dayOfMonth) + "/" + String.format("%02d" , monthOfYear + 1) + "/" + String.format("%02d" , year % 100));
                        }
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)); //Open at today's date.

                datePicker.show();

                //Confirm
                datePicker.getButton(DatePickerWithClearButton.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                int day = datePicker.getDatePicker().getDayOfMonth();
                                int month = datePicker.getDatePicker().getMonth();
                                int year = datePicker.getDatePicker().getYear();
                                editTextDate.setText(String.format("%02d" , day) + "/" + String.format("%02d" , month + 1) + "/" + String.format("%02d" , year % 100));
                                datePicker.dismiss();
                                SearchAndUpdateRecycler();
                            }
                        });
                //Clear
                datePicker.getButton(DatePickerWithClearButton.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                editTextDate.setText("");
                                datePicker.dismiss();
                                SearchAndUpdateRecycler();

                            }
                        });
                //Cancel
                datePicker.getButton(DatePickerWithClearButton.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                datePicker.dismiss();
                            }
                        });
            }
        });


        //Add date text entry listener
        editTextDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            //Perform search if date has changed.
            @Override
            public void afterTextChanged(Editable s) {
                Log.e("Checkpoint", "Date Search changed.");

                if (spinnerSelection != URL.PlannedRoadworks && spinnerSelection != URL.Roadworks && spinnerSelection != URL.TestData)
                    return;

                SearchAndUpdateRecycler();
            }
        });
    }



    /**Searching*/
    private void SearchAndUpdateRecycler() {
        Search();
        UpdateRecycler();
    }


    private void Search() {
        Log.e("Checkpoint", "Search, start");
        rssItemsRefined.clear();

        if (spinnerSelection == URL.PlanAJourney) {
            if (directionsRoadNames != null)
                rssItemsRefined.addAll(SearchRSS.SearchRoads(rssItemsComplete, directionsRoadNames));
            return;
        }

        //Get date text, if current spinner selection supports date query.
        String dateText = "";
        if (spinnerSelection == MainActivity.URL.PlannedRoadworks || spinnerSelection == MainActivity.URL.Roadworks || spinnerSelection == MainActivity.URL.PlanAJourney || spinnerSelection == URL.TestData)
            dateText = editTextDate.getText().toString();

        rssItemsRefined.addAll(SearchRSS.SearchQuery(rssItemsComplete, editTextSearch.getText().toString(), dateText));
    }


    //Updates recycler, used when information in recycler changes.
    private void UpdateRecycler() {
        //No items to display, add no items entry.
        if (rssItemsRefined.size() <= 0) {
            Log.e("Recycler", "No items found.");
            if (IsNetworkConnected())
                if (spinnerSelection == URL.PlanAJourney) {
                    if (directionsRoadNames != null && directionsRoadNames.size() > 0) {
                        RSS rssNoJourney = new RSS("No items found on journey.");
                        rssNoJourney.clickable = false;
                        rssItemsRefined.add(rssNoJourney);
                    } else {
                        RSS rssNoJourney = new RSS("No journey set.");
                        rssNoJourney.clickable = false;
                        rssItemsRefined.add(rssNoJourney);
                    }
                }
                else {
                    RSS rssNoItem = new RSS("No items found.");
                    rssNoItem.clickable = false;
                    rssItemsRefined.add(rssNoItem);
                }
            else {
                RSS rssNoItem = new RSS("Check network connection.");
                rssNoItem.clickable = false;
                rssItemsRefined.add(rssNoItem);
            }
        }

        //Reset view to top of recycler
        layoutManager.scrollToPosition(0);
        //Update content of recycler
        mAdapter.notifyDataSetChanged();

        Log.e("Recycler", "Updated, items: " + rssItemsRefined.size());
    }



    /**Overrides*/
    //Parser Callback
    @Override
    public void parserCallback(final List<RSS> rssItemsTemp) {
        //Run on main thread.
        MainActivity.this.runOnUiThread(new Runnable()
        {
            public void run() {

                //Update lists back on main thread.
                //It's probably a bad idea to access class variables from different threads...
                rssItemsRefined.clear();
                rssItemsRefined.addAll(rssItemsTemp);
                rssItemsComplete.clear();
                rssItemsComplete.addAll(rssItemsTemp);

                //Perform search and update recycler
                SearchAndUpdateRecycler();

                //Enable update button once parsing has completed.
                buttonUpdate.setText(getResources().getString(R.string.Update));
                buttonUpdate.setEnabled(true);
            }
        });
    }


    //Recycler Item Click Callback
    @Override
    //Display the RSS Item in RSS Item Activity when item is clicked
    public void onItemClick(int position) {
        //Check if RSS is clickable, if so, launch RSS Item Activity
        RSS rss = rssItemsRefined.get(position);
        if (rss.clickable) {
            Intent intent = new Intent(this, RSSItemActivity.class);
            //Pass information on to the new activity.
            intent.putExtra("rssItem", rssItemsRefined.get(position));
            startActivity(intent);
        }
    }


    //Map Callback
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==0 && data != null) {
            //Get road names from map journey planner and update recycler.
            directionsRoadNames = (ArrayList<String>) data.getExtras().getSerializable("directionsRoadNames");
            SearchAndUpdateRecycler();
        }
    }


    //Activity onResume
    @Override
    protected void onResume() {
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("MyPref", 0);
        int fontSize = prefs.getInt("Font Size", 1);
        int language = prefs.getInt("Language", 0);

        //If font size has changed, reload the activity.
        if (fontSize != _fontSize || language != _language) {
            _fontSize = fontSize;
            _language = language;

            //Reload the activity and disable the transition animation.
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }
        super.onResume();
    }



    /**Private Methods*/
    //Load Maps
    private void LoadMaps() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivityForResult(intent, 0);
    }


    //Parse RSS Feed
    private void ParseURL() {
        //Disable update button while parsing.
        buttonUpdate.setText(getResources().getString(R.string.Updating));
        buttonUpdate.setEnabled(false);
        //Network access must run on a separate thread.
        new Thread(new Parser.ParseURLTask(this, spinnerSelection)).start();
    }


    //Close software keyboard
    private void CloseKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }


    //Check network connection
    private boolean IsNetworkConnected() {
        ConnectivityManager connMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connMan.getActiveNetworkInfo() != null && connMan.getActiveNetworkInfo().isConnected();
    }
}
