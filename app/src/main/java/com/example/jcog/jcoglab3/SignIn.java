package com.example.jcog.jcoglab3;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.content.SharedPreferences;
import android.support.v4.app.FragmentManager;
import android.widget.Button;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by Chris Moorad on 10/10/17.
 */

public class SignIn extends AppCompatActivity {

    //Declare variables
    private String req;
    private Handler dl;
    private Activity ctx;
    RequestQueue queue;
    boolean errorSignIn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);

        dl = new Handler();
        ctx = this;
        queue = Volley.newRequestQueue(this);

        checkPermissions();


        //redirect to new account page
        Button newaccount = findViewById(R.id.newaccountbutton);
        newaccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Send to new account activity
                Intent myIntent = new Intent(SignIn.this, NewAccount.class);
                startActivity(myIntent);

            }
        } );


        //sign in button clicked
        Button signin = findViewById(R.id.signinbutton);
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //heres da info...
                EditText user = findViewById(R.id.editUser);
                final String username = user.getText().toString();
                EditText pass = findViewById(R.id.editPass);
                final String password = pass.getText().toString();

                //call the get to verify sign in
                doGet(view);

                Log.d("TESTING","TESTING123");

                //after a delay, if there was no sign in error, point to new activity
                final Handler delay = new Handler();
                delay.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!errorSignIn) {
                            Intent myIntent = new Intent(SignIn.this, MainActivity.class);
                            myIntent.putExtra("user", username);
                            myIntent.putExtra("pass", password);
                            startActivity(myIntent);
                        }
                    }
                }, 1500);



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

    //Get requests
    public void doGet(View v){

        req = buildHTML();

        new Thread(new Runnable() {

            String res = null;

            @Override
            public void run() {
                    String url =  "http://cs65.cs.dartmouth.edu/profile.pl?" + req;

                    Log.d("URL", url);

                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                            new Response.Listener<String>() {


                                @Override
                                public void onResponse(String res) {
                                    postResultsToUI(res);   }
                                }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            postResultsToUI("Error" + error.toString());
                        }
                    });


                    // Add the request to the RequestQueue.
                    queue.add(stringRequest);
            }
        }).start();
    }

    // for simple testing
    private String buildHTML(){

        //retrieve username and password
        EditText user = findViewById(R.id.editUser);
        String username = user.getText().toString();
        EditText pass = findViewById(R.id.editPass);
        String password = pass.getText().toString();

        String s = "name=" + username + "&password=" + password;

        return s;
    }

    //support methods for post
    private void postResultsToUI(final String res){
        dl.post(new Runnable() {
            @Override
            public void run() {
                Log.d("RESPONSE", res);
                //if(res.substring(2,5).equals("code")) {
                if(res.contains("error")) {
                    errorSignIn = true;
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(ctx);
                    dlgAlert.setTitle("Error Message...");
                    dlgAlert.setPositiveButton("OK", null);
                    dlgAlert.setCancelable(true);
                    dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dlgAlert.setMessage("Wrong username/password combination, please try again");
                    dlgAlert.create().show();
                }
                else {
                    errorSignIn = false;
                    Log.d("Success", "Succeeded");
                }

            }
        });
    }
    private String readStream(InputStream in) throws IOException {
        BufferedReader r = new BufferedReader(new InputStreamReader(in));
        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line).append('\n');
        }
        return total.toString();
    }



    //CLEAR BUTTON
    public void clearButtonClicked (View v) {
        EditText user = findViewById(R.id.editUser);
        user.setText("");
        EditText pass = findViewById(R.id.editPass);
        pass.setText("");
    }



    }
