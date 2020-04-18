//James Paton S1111175

package com.jamespaton.MPDCoursework;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

public class RSSItemActivity extends BaseActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private RSS rss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rssitem);


        //Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);


        Intent intent = getIntent();
        rss = (RSS) intent.getSerializableExtra("rssItem");

        TextView textViewRSSTitle = findViewById(R.id.textViewRSSTitle);
        TextView textViewRSSDate = findViewById(R.id.textViewRSSDate);
        TextView textViewRESDescription = findViewById(R.id.textViewRSSDiscription);

        textViewRSSTitle.setText(Html.fromHtml(rss.title));
        textViewRESDescription.setText(Html.fromHtml(rss.description));
        if (rss.timeAgo > 0)
            textViewRSSDate.setText(rss.pubDate + "\n" + rss.timeAgoString);
        else
            textViewRSSDate.setText(rss.pubDate);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (_darkmode)
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_dark));

        MoveMap(new LatLng(rss.lat, rss.lng));
    }

    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menusettings, menu);
        getMenuInflater().inflate(R.menu.menufavourite, menu);
        return true;
    }

    private void MoveMap(LatLng latLng) {
        Log.e("RRS Map", "Moving map to " + latLng.toString());

        mMap.addMarker(new MarkerOptions().position(latLng).title("Marker"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12));
    }
}
