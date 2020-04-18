//James Paton S1111175

package com.jamespaton.MPDCoursework.Directions;

import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public interface TaskDirectionsCallbacks {
    void TaskDonePolyline(PolylineOptions polylineOptions);
    void TaskDoneRoads(List<String> roads);
}
