//James Paton S1111175

package com.jamespaton.MPDCoursework;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.jamespaton.MPDCoursework.Directions.*;


import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;


//Maps Activity: Handles location gathering, place lookup, creating a waypoint, drawing that waypoint, and plots roadworks along that waypoint.
public class MapsActivity extends BaseActivity implements OnMapReadyCallback, TaskDirectionsCallbacks, GoogleMap.OnMyLocationButtonClickListener, ParserCallback
{

    private GoogleMap mMap;
    private AutocompleteSupportFragment autocompleteFragmentLocation, autocompleteFragmentDestination;
    private Marker markerLocation, markerDestination;
    private Polyline currentPolyline;
    private Location currentLocation;

    private List<Marker> markers = new ArrayList<>();
    private List<String> directionsRoadNames = new ArrayList<>();

    private Button buttonViewRoadworks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);


        //Initialize Places.
        if (!Places.isInitialized())
            Places.initialize(getApplicationContext(), getString(R.string.Google_API_Key));


        // Initialize the AutocompleteSupportFragment Location.
        autocompleteFragmentLocation = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragmentLocation);
        autocompleteFragmentLocation.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragmentLocation.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.e("MAP!", "Place: " + place.getName() + ", " + place.getId());
                MoveMap(place);
                if (markerLocation != null)
                    markerLocation.remove();
                markerLocation = mMap.addMarker(new MarkerOptions().position(new LatLng(place.getLatLng().latitude, place.getLatLng().longitude)).title("Location: " + place.getName()));

                DrawWaypoint(markerLocation, markerDestination, "driving");
            }

            @Override
            public void onError(Status status) {
                Log.e("MAP!", "An error occurred: " + status);
            }
        });

        // Initialize the AutocompleteSupportFragment Destination.
        autocompleteFragmentDestination = (AutocompleteSupportFragment) getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragmentDestination);
        autocompleteFragmentDestination.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

        autocompleteFragmentDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.e("Map", "Place: " + place.getName() + ", " + place.getId());
                MoveMap(place);
                if (markerDestination != null)
                    markerDestination.remove();
                markerDestination = mMap.addMarker(new MarkerOptions().position(new LatLng(place.getLatLng().latitude, place.getLatLng().longitude)).title("Destination: " + place.getName()));

                //Shoe view roadworks button.
                buttonViewRoadworks.setVisibility(View.VISIBLE);

                //Show location search bar.
                autocompleteFragmentLocation.getView().setVisibility(View.VISIBLE);

                //Draw waypoint.
                DrawWaypoint(markerLocation, markerDestination, "driving");
            }


            @Override
            public void onError(Status status) {
                Log.e("ERROR, MAP!", "An error occurred: " + status);
            }
        });


        //Get Directions button listener
        buttonViewRoadworks = findViewById(R.id.buttonViewRoadworks);
        buttonViewRoadworks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (directionsRoadNames.size() > 0)
                    onBackPressed();
                else
                    Toast.makeText(v.getContext(), "No route found.", Toast.LENGTH_SHORT).show();
            }
        });

        //Hide view roadworks button.
        buttonViewRoadworks.setVisibility(View.GONE);

        //Hide location search bar.
        autocompleteFragmentLocation.getView().setVisibility(View.GONE);

        UpdateCurrentLocation();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (_darkmode)
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_dark));

        //Check location permission.
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("Map, Location", "Location not accessible, requesting...");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        } else {
            Log.e("Map, Location", "Location information accessible.");
            EnableLocationInterface();
        }
    }

    private void MoveMap(LatLng latLng) {
        if (latLng != null) {
            Log.e("Map", "Moving map to " + latLng.toString());

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
        } else
            Log.e("ERROR, MAP!", "LatLng is null.");
    }
    private void MoveMap(Place place) {
        if (place != null)
            MoveMap(place.getLatLng());
        else
            Log.e("ERROR, MAP!", "Place is null.");
    }
    private void MoveMap(Location location) {
        if (location != null)
            MoveMap(new LatLng(location.getLatitude(), location.getLongitude()));
        else
            Log.e("ERROR, MAP!", "Location is null.");
    }

    @Override
    public boolean onMyLocationButtonClick() {
        MoveMap(currentLocation);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 123) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                Log.e("Map, Location", "Location permission granted.");
                EnableLocationInterface();
            } else {
                // User refused to grant permission. You can add AlertDialog here
                Log.e("Map, Location", "Failed to get location permission!");
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void EnableLocationInterface() {
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
    }

    @Override
    public void TaskDonePolyline(PolylineOptions polylineOptions) {
        Log.e("Directions", "Task done.");
        if (currentPolyline != null)
            currentPolyline.remove();
        currentPolyline = mMap.addPolyline(polylineOptions);
    }

    @Override
    public void TaskDoneRoads(List<String> roads) {
        Log.e("Directions", "Roads callback.");
        Log.e("Directions, Full string: ", roads.toString());

        directionsRoadNames.clear();
        List<String> roadNames = new ArrayList<>();

        for (int i = 0; i < roads.size(); i++) {
            int indexStart = -1;
            int indexEnd = -1;
            String searching = roads.get(i);
            String roadName = "";

            //Search common phrases to find road names used within the directions.
            String[] indicators = new String[] {" on <b>", " onto <b>", " toward <b>"," take the <b>"};
            roadName = FindRoadName(searching, indicators);

            if (roadName.length() > 0) {
                if (!directionsRoadNames.contains(roadName))
                    directionsRoadNames.add(roadName);
            }
        }
        Log.e("Directions", "Road Names: " + directionsRoadNames.toString());

        AddMarkers();
    }

    private void DrawWaypoint(Marker a, Marker b, String directionMode) {
        //If no location, get current location.
        if (markerLocation == null)
            UpdateCurrentLocation();

        Log.e("Directions", "Task starting...");
        Log.e("Directions", "From " + a.getTitle() + " to " + b.getTitle());

        String url = GetDirectionsURL(a.getPosition(), b.getPosition(), directionMode);
        new DirectionsFetchURL(MapsActivity.this).execute(url, directionMode);
    }

    private String GetDirectionsURL(LatLng origin, LatLng dest, String directionMode) {
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
        String mode = "mode=" + directionMode;
        String parameters = str_origin + "&" + str_dest + "&" + mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.Google_API_Key);
        return url;
    }

    private void UpdateCurrentLocation() {
        LocationServices
                .getFusedLocationProviderClient(this)
                .getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        //currentLocation = location;
                        //currentLocation is being replaced with the coordinates of GCU as AVDs default to Google's headquarters for their GPS location, which is an unrealistic location of someone using a Traffic Scotland app.
                        currentLocation = new Location("Glasgow Caledonian University");
                        currentLocation.setLatitude(55.86746635);
                        currentLocation.setLongitude(-4.25024539);

                        MoveMap(currentLocation);
                        if (currentLocation != null) {
                            if (markerLocation != null)
                                markerLocation.remove();
                            markerLocation = mMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())).title("Location: " + currentLocation.getProvider()));
                        }
                    }
                });
    }

    @Override
    public void onBackPressed() {
        Intent intent=new Intent();
        intent.putExtra("directionsRoadNames", (Serializable)directionsRoadNames);
        setResult(0,intent);
        finish();
    }

    private String FindRoadName(String sentence, String[] indicators) {
        String roadName = "";

        for (String indicator : indicators) {
            int indexStart = sentence.indexOf(indicator);

            if (indexStart > -1) {
                roadName = sentence.substring(indexStart + indicator.length());
                int indexEnd = roadName.indexOf("</b>");
                roadName = roadName.substring(0, indexEnd);

                return roadName;
            }
        }

        return "";
    }

    private void AddMarkers() {
        new Thread(new Parser.ParseURLTask(this, MainActivity.URL.PlanAJourney)).start();
    }

    @Override
    public void parserCallback(List<RSS> rssItems) {
        //Refine list to include only those along the waypoint.
        final List<RSS> rssItemsRefined = SearchRSS.SearchRoads(rssItems, directionsRoadNames);

        //Run on main thread.
        MapsActivity.this.runOnUiThread(new Runnable() {
            public void run() {
                //Clear marker
                for (Marker marker : markers)
                    marker.remove();
                markers.clear();

                //Place markers and add to list.
                for (RSS rss : rssItemsRefined) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.title(Html.fromHtml(rss.title).toString());
                    markerOptions.position(new LatLng(rss.lat, rss.lng));

                    Marker marker = mMap.addMarker(markerOptions);
                    markers.add(marker);
                }
            }
        });
    }
}
