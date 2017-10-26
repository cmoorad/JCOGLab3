package com.example.jcog.jcoglab3;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Chris Moorad on 10/10/17.
 */

public class preferences_Frag extends Fragment {

    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.preferences_frag, container, false);


        //set the username in preferences
        TextView usernametext = v.findViewById(R.id.displayusername);
        String userholder = ((MainActivity)getActivity()).user1;
        usernametext.setText(userholder);

        return v;


    }


}
