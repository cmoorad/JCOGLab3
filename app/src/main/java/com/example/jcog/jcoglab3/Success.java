package com.example.jcog.jcoglab3;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by ucla201 on 10/19/17.
 */

public class Success extends AppCompatActivity {

    public String user1;
    public String pass1;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.success);

        Intent i = getIntent();
        user1 =  i.getStringExtra("user");
        pass1 = i.getStringExtra("pass");

        Button keepplaying = findViewById(R.id.keepPlaying);
        keepplaying.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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




}
