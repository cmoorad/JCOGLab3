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

    @Override
    public void onLocationChanged(Location location) {

        Log.d("LOCATION", "CHANGED: " + location.getLatitude() + " " + location.getLongitude());
        Toast.makeText(this, "LOC: " + location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_LONG).show();

        LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(newPoint));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17f));


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        // Add a marker and move the camera to it
        Double x = Double.parseDouble(getString(R.string.theGreen_x));
        Double y = Double.parseDouble(getString(R.string.theGreen_y));
        LatLng hanover = new LatLng(x, y);

        Location l = null; // remains null if Location is disabled in the phone
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            l = mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
        catch(SecurityException e){
            Log.d("PERM", "Security Exception getting last known location. Using Hanover.");
        }

        if (l != null)  loc = new LatLng(l.getLatitude(), l.getLongitude());
        else loc = hanover;

        Log.d("Coords", loc.latitude + " " + loc.longitude);

        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.addMarker(new MarkerOptions().position(hanover).title("Marker in Hanover"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(hanover));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(17f)); // buildings-level

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng p0) {

                Log.d("Map", p0.toString());
                if (p0 != null) {
                    mMap.addMarker(new MarkerOptions().position(p0).title(p0.toString()));
                }
            }
        });

    }

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
