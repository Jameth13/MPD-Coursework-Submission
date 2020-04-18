//James Paton S1111175

package com.jamespaton.MPDCoursework.Directions;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Code adapted from Vysh01's android-maps-directions repo
 * Ref: https://github.com/Vysh01/android-maps-directions
 **/

public class DirectionsPointsParser extends AsyncTask<String, Integer, Directions> {
    TaskDirectionsCallbacks taskCallback;
    String directionMode = "driving";

    public DirectionsPointsParser(Context mContext, String directionMode) {
        this.taskCallback = (TaskDirectionsCallbacks) mContext;
        this.directionMode = directionMode;
    }

    // Parsing the data in non-ui thread
    @Override
    protected Directions doInBackground(String... jsonData) {

        JSONObject jObject;
        Directions directions = new Directions();

        try {
            jObject = new JSONObject(jsonData[0]);
            DirectionsDataParser parser = new DirectionsDataParser();

            // Starts parsing data
            directions = parser.parse(jObject);

        } catch (Exception e) {
            Log.e("ERROR, POINTS!", e.toString());
        }
        return directions;
    }

    // Executes in UI thread, after the parsing process
    @Override
    protected void onPostExecute(Directions directions) {
        ArrayList<LatLng> points;
        PolylineOptions lineOptions = null;
        Log.e("Points", "Length of results: " + directions.routes.size());
        // Traversing through all the routes
        for (int i = 0; i < directions.routes.size(); i++) {
            Log.e("Points", "Result " + i + ": " + directions.routes.get(i).size());
            points = new ArrayList<>();
            lineOptions = new PolylineOptions();
            // Fetching i-th route
            List<HashMap<String, String>> path = directions.routes.get(i);
            // Fetching all the points in i-th route
            for (int j = 0; j < path.size(); j++) {
                HashMap<String, String> point = path.get(j);
                //Log.e("POINTS", point.toString());

                double lat = Double.parseDouble(point.get("lat"));
                double lng = Double.parseDouble(point.get("lng"));
                LatLng position = new LatLng(lat, lng);
                points.add(position);
            }
            // Adding all the points in the route to LineOptions
            lineOptions.addAll(points);
            if (directionMode.equalsIgnoreCase("walking")) {
                lineOptions.width(10);
                lineOptions.color(Color.MAGENTA);
            } else {
                lineOptions.width(20);
                lineOptions.color(Color.BLUE);
            }
        }

        // Drawing polyline in the Google Map for the i-th route
        if (lineOptions != null) {
            taskCallback.TaskDonePolyline(lineOptions);
            taskCallback.TaskDoneRoads(directions.roads);

        } else {
            Log.e("Points", "No route found.");
        }
    }
}
