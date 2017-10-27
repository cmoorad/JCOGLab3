package com.example.jcog.jcoglab3;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;

import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.widget.EditText;
import android.widget.Toast;
import android.location.LocationListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by ucla201 on 10/19/17.
 */

public class Game extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    public String user1;
    public String pass1;

    private GoogleMap mMap;
    private boolean permCheck = false;
    private LocationManager mgr;
    private LatLng loc;
    private Marker own;
    private Marker server;
    private SupportMapFragment mapFragment;

    private String req;
    private Handler dl;
    private Activity ctx;
    RequestQueue queue;

    public Game() {
    }

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_fragment);

        Intent i = getIntent();
        user1 = i.getStringExtra("user");
        pass1 = i.getStringExtra("pass");

        dl = new Handler();
        ctx = this;
        queue = Volley.newRequestQueue(this);

        checkPermissions();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
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
                myIntent.putExtra("pass", pass1);
                startActivity(myIntent);
            }
        });


        getCats(mapFragment.getView());

    }

    //Updates map zoom based on current location
    @Override
    public void onLocationChanged(Location location) {

        Log.d("LOCATION", "CHANGED: " + location.getLatitude() + " " + location.getLongitude());

        LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());

        loc = newPoint;

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(newPoint));
        //mMap.moveCamera(CameraUpdateFactory.zoomTo(16f));

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
        mMap.addMarker(new MarkerOptions().position(loc).title("Current Location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(loc));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(16f)); // buildings-level



        //getCats(mapFragment.this);



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

    //Check to ensure permissions
    private void checkPermissions() {
        if(Build.VERSION.SDK_INT < 23)
            return;

        if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.INTERNET}, 0);
        }
    }


    //Get requests
    public void getCats(View v){

        req = buildHTML();

        new Thread(new Runnable() {

            @Override
            public void run() {
                String url =  "http://cs65.cs.dartmouth.edu/catlist.pl?" + req;

                Log.d("URL", url);
                StringRequest req = new StringRequest (Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String res) {
                                postResultsToUI(res);
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("THE ERROR", error.toString());
                        postResultsToUI("Error" + error.toString());
                    }
                });


                // Add the request to the RequestQueue.
                queue.add(req);
            }
        }).start();
    }

    // builds string for retrieving cat list
    private String buildHTML(){

        String s = "name=" + user1 + "&password=" + pass1 + "&mode=easy";

        return s;
    }

    //support methods for post
    private void postResultsToUI(final String res){
        dl.post(new Runnable() {
            @Override
            public void run() {
                if(res.contains("error")) {
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(ctx);
                    dlgAlert.setTitle("Error Message...");
                    dlgAlert.setPositiveButton("OK", null);
                    dlgAlert.setCancelable(true);
                    dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dlgAlert.setMessage("Could not connect to server");
                    dlgAlert.create().show();
                    return;
                }
                else {
                    Log.d("GOT CATLIST", "Succeeded");
                    drawCats(res);
                }

            }
        });
    }


    private void drawCats(String jsonString) {

        try {
            JSONArray catlist = new JSONArray(jsonString);
            Log.d("CATLISTARRAY", catlist.toString());



            for (int i = 0; i < catlist.length(); i++) {


                JSONObject cat = catlist.getJSONObject(i);

                String name = cat.get("name").toString();


                Double catLat = Double.parseDouble(cat.get("lat").toString());
                Double catLng = Double.parseDouble(cat.get("lng").toString());
                LatLng pos = new LatLng(catLat, catLng);

                mMap.addMarker(new MarkerOptions().position(pos).title(name).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));


            }



        } catch(JSONException je) {
            Log.d("Error", "JSON CREATION ERROR");
        }




    }




    // Remove old marker and place new marker.
    private void drawMarker(LatLng l, boolean serverLoc){
        if(serverLoc) {
            if (server != null)
                server.remove();
            server = mMap.addMarker(new MarkerOptions().position(l).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        } else{
            if (own != null)
                own.remove();
            own = mMap.addMarker(new MarkerOptions().position(l).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }

/*
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
