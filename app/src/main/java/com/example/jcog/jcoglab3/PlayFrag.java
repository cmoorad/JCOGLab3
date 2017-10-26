package com.example.jcog.jcoglab3;

/**
 * Created by Chris Moorad on 10/9/17.
 */



import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class PlayFrag extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.play_frag, container, false);

        TextView usernametext = v.findViewById(R.id.hiuser);
        final String userholder = ((MainActivity)getActivity()).user1;

        Button playbutton = v.findViewById(R.id.playButton);
        playbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(getActivity(), Game.class);
                myIntent.putExtra("user", userholder);
                startActivity(myIntent);



                //MAKE CALL TO METHOD (INPUT USER) THAT MAKES NETWORK CALL TO POPULATE MAP WITH
                //CATS ASSOCIATED WITH USERNAME



            }
        });


        //set username text

        if (userholder != null) {
            usernametext.setText("Hi, " + userholder + "!");
        }


        return v;

    }



}