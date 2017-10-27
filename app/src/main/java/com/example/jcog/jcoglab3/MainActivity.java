package com.example.jcog.jcoglab3;

/**
 * Created by Chris Moorad and Jack Taylor on 10/10/17.
 *
 *
 * Our project has overall functionality. It correctly makes network requests, successfully
 * creating new accounts, checking username availability, and verifying sign-in parameters.
 *
 * It prompts the user to enter a password and username, and maintains all functionality
 * we implemented from the previous lab.
 *
 *
 * One thing we couldn't quite figure out was how to call setText on the tab containing our
 * preferences fragment. We were able to pass the username successfully between activities
 * via the intent, but weren't able to actually implement it in our preferences page.
 *
 *
 * Other than that, all functionality for lab 2 is included.
 *
 */


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    public String user1;
    public String pass1;
    private static final int MY_PERMISSIONS_REQUEST = 301;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();

        Intent i = getIntent();
        user1 =  i.getStringExtra("user");
        pass1 = i.getStringExtra("pass");


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //add all the tabs to the tablayout
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Tab 1"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab 2"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab 3"));
        tabLayout.addTab(tabLayout.newTab().setText("Tab 4"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //add tabs to adapter to concatenate view
        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        final FragmentPagerAdapter adapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public int getCount() {
                return 4;
            }

            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        PlayFrag tab1 = new PlayFrag();
                        return tab1;
                    case 1:
                        history_Frag tab2 = new history_Frag();
                        return tab2;
                    case 2:
                        ranking_Frag tab3 = new ranking_Frag();
                        return tab3;
                    case 3:
                        preferences_Frag tab4 = new preferences_Frag();
                        return tab4;

                    default:
                        return null;
                }
            }


            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return "Play";
                    case 1:
                        return "History";
                    case 2:
                        return "Ranking";
                    case 3:
                        return "Settings";
                    default:
                        return "Settings";
                }
            }
        };


        //BELOW ARE SOME FAILED IMPLEMENTATIONS OF OUR SETTEXT FOR THE PREFERENCES TAB
        //PLEASE IGNORE.

        //Fragment x = adapter.getItem(3);
        //Context v = x.getActivity();
        //viewPager.getAdapter().getItem
        //View v = adapter.getItem(3).getView();
        //View v = ((ViewGroup) tabLayout.getChildAt(0).getChildAt(3);
        //View v = tabLayout.getTabAt(3).getCustomView();
        //TextView widg = findViewById(R.id.displayusername);
        //widg.setText(user);
        //tv = findViewById(R.id.displayusername);
        //tv.setText(p);

        //set the adapter to the viewpager and tab layout via pager
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);

    }

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