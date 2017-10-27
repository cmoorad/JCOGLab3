package com.example.jcog.jcoglab3;



import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.Manifest;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.net.Uri;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.soundcloud.android.crop.Crop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import android.os.Handler;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;



/**
 * Created by Chris Moorad on 10/10/17.
 */

public class NewAccount extends AppCompatActivity {

    //VARIABLE DECLARATIONS
    String password;
    boolean newAccountError = true;

    final int REQUEST_IMAGE_CAPTURE = 0;
    Bitmap bitmap;

    private static final String IMAGE_UNSPECIFIED = "IMAGE/*";
    private static final String URI_INSTANCE_STATE_KEY = "saved_uri";

    private Uri mImageCaptureUri;
    private ImageView mImageView;
    private boolean isTakenFromCamera;

    private Handler dl;
    private String req;
    private Activity ctx;
    RequestQueue queue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_account);

        //start the password check focus listener
        focusChange();

        //start the username check focus listener
        checkPermissions();
        userTaken();

        //add additional focus change listener that calls to network and checks if username is taken

        dl = new Handler();
        ctx = this;
        queue = Volley.newRequestQueue(this);


        mImageView = findViewById(R.id.imageProfile);

        if (savedInstanceState != null) {
            mImageCaptureUri = savedInstanceState.getParcelable(URI_INSTANCE_STATE_KEY);
        }

        if (bitmap != null)
            mImageView.setImageBitmap(bitmap);

        loadSnap();


        //redirect to sign in page
        Button existing = findViewById(R.id.existingaccountbutton);
        existing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Send to sign in activity
                Intent myIntent = new Intent(NewAccount.this, SignIn.class);
                startActivity(myIntent);

            }
        } );


        //on Click listener for done button
        Button save = findViewById(R.id.donebutton);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //here's the info (username and password) to be passed to network...
                EditText user = findViewById(R.id.editusername);
                final String username = user.getText().toString();
                EditText pass = findViewById(R.id.editpassword);
                final String password = pass.getText().toString();

                //code for error box
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(ctx);
                dlgAlert.setTitle("Error Message...");
                dlgAlert.setPositiveButton("OK", null);
                dlgAlert.setCancelable(true);
                dlgAlert.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                //if the username and password fields are left blank
                if (username.equals("") && password.equals("")) {
                    dlgAlert.setMessage("Please set a username and password");
                    dlgAlert.create().show();
                }

                //if the username field is left blank
                else if (username.equals("")) {
                    dlgAlert.setMessage("Please set a username");
                    dlgAlert.create().show();
                }

                //if the password field is left blank
                else if (password.equals("")) {
                    dlgAlert.setMessage("Please set a password");
                    dlgAlert.create().show();
                }

                else {

                    //save the new account to the network
                    doPost(view);

                    //redirect to account page after delay
                    final Handler delay = new Handler();
                    delay.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!newAccountError) {
                                Intent myIntent = new Intent(NewAccount.this,
                                        MainActivity.class);
                                myIntent.putExtra("user", username);
                                myIntent.putExtra("pass", password);
                                startActivity(myIntent);
                            }
                        }
                    }, 1500);
                }

            }
        });

    }

    //focus change listener for whether username is taken or not
    public void userTaken() {
        final EditText user = findViewById(R.id.editusername);
        user.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    nameCheck(view);
                    final Handler delay = new Handler();
                    delay.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                        }
                    }, 1000);
                }
            }
        });
    }

    //Check to ensure permissions
    private void checkPermissions() {
        if(Build.VERSION.SDK_INT < 23)
            return;

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
        }

        if (checkSelfPermission(Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.INTERNET}, 0);
        }
    }

    //Name check
    public void nameCheck (View v){

        EditText user = findViewById(R.id.editusername);
        String username = user.getText().toString();

        req = "name=" + username;

        new Thread(new Runnable() {

            String res = null;

            @Override
            public void run() {

                String url = "http://cs65.cs.dartmouth.edu/nametest.pl?" + req;
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {


                    @Override
                    public void onResponse(String res) {
                        postResultsToUI(res);
                    }
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

    //handles profile saving
    public void doPost(View v){

        req = buildJson();

        new Thread(new Runnable() {

            String res = null;
            @Override
            public void run() {
                try {
                    URL url = new URL("http://cs65.cs.dartmouth.edu/profile.pl");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    try {
                        conn.setDoOutput(true);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setRequestProperty("Accept-Encoding", "identity");
                        conn.setFixedLengthStreamingMode(req.length());

                        OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                        out.write(req.getBytes());
                        out.flush();
                        out.close();

                        InputStream in = new BufferedInputStream(conn.getInputStream());
                        res = readStream(in);
                    }
                    catch(Exception e){
                        Log.d("THREAD", e.toString());
                    } finally {
                        conn.disconnect();
                    }
                }
                catch( Exception e){
                    Log.d("THREAD", e.toString());
                }

                if( res!= null ) {
                    Log.d("NET POST", res);
                    postResultsFromPost(res);
                }
                else{
                    Log.d("NET ERR", "empty result");
                }
            }
        }).start();
    }

    //support method for profile save
    private void postResultsFromPost(final String res) {
        dl.post(new Runnable() {
            @Override
            public void run() {
                Log.d("JSON RESPONSE", res);
                if(res.contains("ERROR")) {
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(ctx);
                    dlgAlert.setTitle("Error Message...");
                    dlgAlert.setPositiveButton("OK", null);
                    dlgAlert.setCancelable(true);
                    dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dlgAlert.setMessage("Something went wrong with your network request - it's " +
                            "possible that someone has recently signed up with your desired " +
                            "username. Please re-enter your information, and try again");
                    dlgAlert.create().show();
                }
                else {
                    newAccountError = false;
                    Log.d("Success", "Succeeded");
                }

            }
        });
    }

    // build Json object
    private String buildJson(){

        //retrieve username and password
        EditText user = findViewById(R.id.editusername);
        String username = user.getText().toString();
        EditText pass = findViewById(R.id.editpassword);
        String password = pass.getText().toString();

        JSONObject o = new JSONObject();
        try {
            o.put("name", username);
            o.put( "password", password);
        }
        catch( JSONException e){
            Log.d("JSON", e.toString());
        }
        return o.toString();
    }

    //support method for name check
    private void postResultsToUI(final String res){

        dl.post(new Runnable() {
            @Override
            public void run() {
                Log.d("JSON RESPONSE", res);
                if(res.contains("false")) {
                    AlertDialog.Builder dlgAlert = new AlertDialog.Builder(ctx);
                    dlgAlert.setTitle("Error Message...");
                    dlgAlert.setPositiveButton("OK", null);
                    dlgAlert.setCancelable(true);
                    dlgAlert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    dlgAlert.setMessage("Username already taken, please try again");
                    dlgAlert.create().show();
                }
                else {
                    Log.d("Success", "Succeeded");
                }

            }
        });
    }

    //support method for reading name check and post request responses
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
        EditText user = findViewById(R.id.editusername);
        user.setText("");
        EditText pass = findViewById(R.id.editpassword);
        pass.setText("");
    }

    //PASSWORD CHECK
    //method that returns password
    public String getPassword() {
        return password;
    }

    //calls the dialog fragment instance to be shown
    void showDialog() {
        FragmentManager fm = getSupportFragmentManager();
        PassCheck passCheck = new PassCheck();
        passCheck.show(fm, "dialog_box");
    }

    //calls passaword-check dialog to show (above) - called in onCreate to instantiate listener
    public void focusChange() {
        final EditText pass1 = findViewById(R.id.editpassword);
        pass1.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if(!hasFocus) {
                    EditText t = findViewById(R.id.editpassword);
                    password = t.getText().toString();
                    showDialog();
                }
            }
        });
    }


    //CAMERA AND CROP HERE (From lab 1)

    public void camButtonClicked(View v){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        ContentValues values = new ContentValues(1);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        mImageCaptureUri = getContentResolver()
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                mImageCaptureUri);
        takePictureIntent.putExtra("return-data", true);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // save the bitmap
        Log.d("STATE", "onSaveState");
        outState.putParcelable("IMG", bitmap);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d("STATE", "onRestoreState");
        bitmap = savedInstanceState.getParcelable("IMG");
        mImageView.setImageBitmap(bitmap);
    }

    public void onSaveClicked(View v) {
        // Save picture
        saveSnap();
        // Making a "toast" informing the user the picture is saved.
        Toast.makeText(getApplicationContext(),
                getString(R.string.ui_profile_toast_save_text),
                Toast.LENGTH_SHORT).show();
        // Close the activity
        finish();
    }

    public void onChangePhotoClicked(View v) {
        // changing the profile image, show the dialog asking the user
        // to choose between taking a picture
        // Go to MyRunsDialogFragment for details.
        displayDialog(MyRunsDialogFragment.DIALOG_ID_PHOTO_PICKER);
    }

    // Handle data after activity returns.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                // Send image taken from camera for cropping
                beginCrop(mImageCaptureUri);
                break;

            case Crop.REQUEST_CROP: //We changed the RequestCode to the one being used by the library.
                // Update image view after image crop
                handleCrop(resultCode, data);

                // Delete temporary image taken by camera after crop.
                if (isTakenFromCamera) {
                    File f = new File(mImageCaptureUri.getPath());
                    if (f.exists())
                        f.delete();
                }

                break;
        }
    }

    public void displayDialog(int id) {
        DialogFragment fragment = MyRunsDialogFragment.newInstance(id);
        fragment.show(getFragmentManager(),
                getString(R.string.dialog_fragment_tag_photo_picker));
    }

    public void onPhotoPickerItemSelected(int item) {
        Intent intent;

        switch (item) {

            case MyRunsDialogFragment.ID_PHOTO_PICKER_FROM_CAMERA:
                // Take photo from camera，
                // Construct an intent with action
                // MediaStore.ACTION_IMAGE_CAPTURE
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Construct temporary image path and name to save the taken
                // photo
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
                mImageCaptureUri = getContentResolver()
                        .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                /**
                 This was the previous code to generate a URI. This was throwing an exception -
                 "android.os.StrictMode.onFileUriExposed" in Android N.
                 This was because StrictMode prevents passing URIs with a file:// scheme. Once you
                 set the target SDK to 24, then the file:// URI scheme is no longer supported because the
                 security is exposed. You can change the  targetSDK version to be <24, to use the following code.
                 The new code as written above works nevertheless.
                 mImageCaptureUri = Uri.fromFile(new File(Environment
                 .getExternalStorageDirectory(), "tmp_"
                 + String.valueOf(System.currentTimeMillis()) + ".jpg"));
                 **/

                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        mImageCaptureUri);
                intent.putExtra("return-data", true);
                try {
                    // Start a camera capturing activity
                    // REQUEST_CODE_TAKE_FROM_CAMERA is an integer tag you
                    // defined to identify the activity in onActivityResult()
                    // when it returns
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                isTakenFromCamera = true;
                break;

            default:
                return;
        }

    }

    private void loadSnap() {


        // Load profile photo from internal storage
        try {
            FileInputStream fis = openFileInput(getString(R.string.profile_photo_file_name));
            Bitmap bmap = BitmapFactory.decodeStream(fis);
            mImageView.setImageBitmap(bmap);
            fis.close();
        } catch (IOException e) {
            // Default profile photo if no photo saved before.
            mImageView.setImageResource(R.drawable.default_profile);
        }
    }

    private void saveSnap() {

        // Commit all the changes into preference file
        // Save profile image into internal storage.
        mImageView.buildDrawingCache();
        Bitmap bmap = mImageView.getDrawingCache();
        try {
            FileOutputStream fos = openFileOutput(
                    getString(R.string.profile_photo_file_name), MODE_PRIVATE);
            bmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).asSquare().start(this);
    }

    private void handleCrop(int resultCode, Intent result) {
        if (resultCode == RESULT_OK) {
            mImageView.setImageURI(null);
            mImageView.setImageURI(Crop.getOutput(result));
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}


