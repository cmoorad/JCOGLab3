package com.example.jcog.jcoglab3;

import android.content.Intent;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;
import android.location.LocationListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by ucla201 on 10/19/17.
 */

public class Game extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    public String user1;

    private GoogleMap mMap;
    private boolean permCheck = false;
    private LocationManager mgr;
    private LatLng loc;

    public Game() {
    }

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_fragment);

        Intent i = getIntent();
        user1 = i.getStringExtra("user");


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        permCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

        if (!permCheck) {
            Toast.makeText(this, "GPS permission FAILED", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "GPS permission OK", Toast.LENGTH_LONG).show();

            mgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 5f, this);
        }


        Button successbutton = findViewById(R.id.successButton);
        successbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(Game.this, Success.class);
                myIntent.putExtra("user", user1);
                startActivity(myIntent);
            }
        });

    }

    //Updates map zoom based on current location
    @Override
    public void onLocationChanged(Location location) {

        Log.d("LOCATION", "CHANGED: " + location.getLatitude() + " " + location.getLongitude());

        LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(newPoint));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(16f));

    }


    //SETS UP THE MAP INITIALLY, WITH APPROPRIATE ZOOM
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        // Set default zoom marker
        Double x = Double.parseDouble(getString(R.string.theGreen_x));
        Double y = Double.parseDouble(getString(R.string.theGreen_y));
        LatLng hanover = new LatLng(x, y);

        Location l = null; // remains null if Location is disabled in the phone
        try {
            //if the permissions are there, continue
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            l = mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(l.getLatitude(), l.getLongitude()) , 16.0f) );
        }
        catch(SecurityException e){
            Log.d("PERM", "Security Exception getting last known location. Using Hanover.");
        }

        //if phone location is enabled, set current location to the phone location
        if (l != null)  loc = new LatLng(l.getLatitude(), l.getLongitude());
        else loc = hanover;
        Log.d("Coords", loc.latitude + " " + loc.longitude);

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.addMarker(new MarkerOptions().position(loc).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(16f)); // buildings-level


        /*
        //currently does nothing...might not need at all in fact...
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng p0) {


                Log.d("Map", p0.toString());
                if (p0 != null) {
                    mMap.addMarker(new MarkerOptions().position(p0).title(p0.toString()));
                }

            }
        });
        */
    }

    // Put the marker at given location and zoom into the location
    private void updateWithNewLocation(Location location) {
        if (location != null) {
            LatLng l = fromLocationToLatLng(location);
            loc = l;

            /*
            drawMarker(l, false);
            moveToCurrentLocation(l);
            */
        }
    }

    //support method for above
    public static LatLng fromLocationToLatLng(Location location){
        return new LatLng(location.getLatitude(), location.getLongitude());
    }

    /*
    // Remove old marker and place new marker.
    private void drawMarker(LatLng l, boolean serverLoc){
        if(serverLoc) {
            if (server != null)
                server.remove();
            server = map.addMarker(new MarkerOptions().position(l).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        } else{
            if (own != null)
                own.remove();
            own = map.addMarker(new MarkerOptions().position(l).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }


    private void moveToCurrentLocation(LatLng currentLocation)
    {
        LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
        if(!bounds.contains(currentLocation) || zoomedOut ){
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
            // Zoom in, animating the camera.
            map.animateCamera(CameraUpdateFactory.zoomIn());
            // Zoom out to zoom level 10, animating with a duration of 1 second.
            map.animateCamera(CameraUpdateFactory.zoomTo(15), 1000, null);
            zoomedOut = false;
        }
    }
    */













    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
