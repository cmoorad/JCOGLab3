package com.example.jcog.jcoglab3;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

/**
 * Created by ucla201 on 10/19/17.
 */

public class Success extends AppCompatActivity {

    public String user1;
    public String pass1;
    private String req;
    private Handler dl;
    private Activity ctx;
    RequestQueue queue;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.success);

        checkPermissions();

        Intent i = getIntent();
        user1 =  i.getStringExtra("user");
        pass1 = i.getStringExtra("pass");

        Button keepplaying = findViewById(R.id.keepPlaying);
        keepplaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetCats(view);
                Intent myIntent = new Intent(Success.this, Game.class);
                myIntent.putExtra("user", user1);
                myIntent.putExtra("pass", pass1);
                startActivity(myIntent);
            }
        });

        Button doneplaying = findViewById(R.id.donePlaying);
        doneplaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(Success.this, MainActivity.class);
                myIntent.putExtra("user", user1);
                myIntent.putExtra("pass", pass1);
                startActivity(myIntent);
            }
        });

    }

    //Pet cat - request
    public void resetCats(View v){

        req = buildPetHTML();

        new Thread(new Runnable() {

            @Override
            public void run() {
                String url = "http://cs65.cs.dartmouth.edu/resetlist.pl?" + req;

                Log.d("URL", url);
                StringRequest req = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        postResetResults(res);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("THE ERROR", error.toString());
                        postResetResults("Error" + error.toString());
                    }
                });


                // Add the request to the RequestQueue.
                queue.add(req);
            }
        }).start();

    }

    // builds string for retrieving cat list
    private String buildPetHTML(){
        String s = "name=" + user1 + "&password=" + pass1;
        return s;
    }

    //support methods for post - error caught or draw the cat markers
    private void postResetResults(final String res){
        dl.post(new Runnable() {
            @Override
            public void run() {
                if(res.contains("ERROR")) {
                    Log.d("RESPONSE", res);
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(ctx);
                    dlgAlert.setTitle("Uh oh...");
                    dlgAlert.setPositiveButton("OK", null);
                    dlgAlert.setCancelable(true);
                    dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dlgAlert.setMessage("Could not reset game, check connection and try again");
                    dlgAlert.create().show();
                }
                else {
                    Log.d("RESET SUCCESS", res);
                }

            }
        });
    }

    //Check to ensure permissions
    private void checkPermissions() {
        if(Build.VERSION.SDK_INT < 23)
            return;

        if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.INTERNET}, 0);
        }
    }


}
