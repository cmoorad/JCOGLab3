package com.example.jcog.jcoglab3;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Chris Moorad on 9/29/17.
 */

public class PassCheck extends DialogFragment {

    EditText passVerify;
    View view;
    String p;
    Activity a;
    Button b;

    //empty constructor for dialog class
    public PassCheck() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //creates dialog view
        view = inflater.inflate(R.layout.dialog_box, container);
        getDialog();

        //retrieves password from dialog box
        passVerify = view.findViewById(R.id.passCheckEdit);

        //retrieves password from new account
        a = getActivity();
        p = ((NewAccount) a).getPassword();

        //handles submit button listener and functionality
        b = view.findViewById(R.id.submitBtn);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (p.equals(passVerify.getText().toString())) {
                    onDestroyView();
                }
                else {
                    passVerify.setText("");
                    passVerify.setHint("Try again");
                }
            }
        } );
        return view;
    }
}
