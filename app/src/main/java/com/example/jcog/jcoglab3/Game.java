package com.example.jcog.jcoglab3;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.location.LocationListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;

/**
 * Created by Chris Moorad on 10/19/17.
 */

public class Game extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    //Variable declarations
    public String user1;
    public String pass1;
    public Bitmap image;

    private GoogleMap mMap;
    private boolean permCheck = false;
    private LocationManager mgr;
    private LatLng loc;
    private SupportMapFragment mapFragment;
    private static final int MY_PERMISSIONS_REQUEST = 301;

    private String req;
    private Handler dl;
    private Activity ctx;
    RequestQueue queue;

    public Game() {
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_fragment);

        requestPermissions();

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

    //Updates current location
    @Override
    public void onLocationChanged(Location location) {

        Log.d("LOCATION", "CHANGED: " + location.getLatitude() + " " + location.getLongitude());

        loc = new LatLng(location.getLatitude(), location.getLongitude());

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
            if (l != null) {
                mMap.moveCamera( CameraUpdateFactory.newLatLngZoom(new LatLng(l.getLatitude(), l.getLongitude()) , 16.0f) );
                Log.d("HERES THE LOCATION", l.toString());
            }
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

    //Get catlist - request
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

    //support methods for post - error caught or draw the cat markers
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
                }
                else {
                    Log.d("GOT CATLIST", res);
                    drawCats(res);
                }

            }
        });
    }

    //draws the cat markers
    private void drawCats(String jsonString) {

        //THROWING JSON EXCEPTION - actually an exception for setting onclick listeners

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

                /*
                mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                          @Override
                          public boolean onMarkerClick(Marker marker) {
                              String namesky = marker.getTitle();
                              Log.d("MARKER WAS CLICKED", namesky);

                              //onMarkerListener(getCurrentFocus(), marker);
                              return false;
                          }
                      });
                      */
            }

        } catch(JSONException je) {
            Log.d("Error", "JSON CREATION ERROR");
        }

    }


    //onClickListener for Cat markers
    private void onMarkerListener(View view, Marker marker) {


        /*
        for (int i = 0; i < catlist.length(); i++) {

            try {

                //error being thrown here
                JSONObject cat = catlist.getJSONObject(i);

                if (cat.get("name").toString().equals(marker.getTitle())) {

                    image = grabBitMap(cat.get("picUrl").toString());
                    ImageView icon = view.findViewById(R.id.catIcon);
                    icon.setImageBitmap(image);


                    //to calculate dist, call method from google API using loc AND parsed values

                    TextView name = view.findViewById(R.id.catname);
                    TextView dist = view.findViewById(R.id.catdist);



                }

            }

            catch(JSONException je) {
                Log.d("Error", "JSON CREATION ERROR");
            }



        }
        */


    }


    //support method to get image from url
    protected Bitmap grabBitMap(String url) {
        Bitmap Icon = null;
        try {
            InputStream in = new java.net.URL(url).openStream();
            Icon = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        return Icon;
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






    //double check permissions even though checked in main activity (trying to troubleshoot)
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void requestPermissions(){
        // Here, thisActivity is the current activity
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.INTERNET)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET},
                    MY_PERMISSIONS_REQUEST);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                        grantResults[2] == PackageManager.PERMISSION_GRANTED)) {
                    // permissions not obtained
                    Toast.makeText(this,"failed request permission!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }













}
