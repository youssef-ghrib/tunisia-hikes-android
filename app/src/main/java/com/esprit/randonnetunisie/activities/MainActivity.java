package com.esprit.randonnetunisie.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.esprit.randonnetunisie.R;
import com.esprit.randonnetunisie.entities.User;
import com.esprit.randonnetunisie.fragments.HomeFragment;
import com.esprit.randonnetunisie.fragments.ProfileFragment;
import com.esprit.randonnetunisie.fragments.SearchFragment;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.google.gson.Gson;

import bolts.AppLinks;

public class MainActivity extends AppCompatActivity {

    public User user;

    SharedPreferences shared;
    SharedPreferences.Editor editor;

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        Uri targetUrl = AppLinks.getTargetUrlFromInboundIntent(this, getIntent());
        if (targetUrl != null) {
            Log.i("Activity", "App Link Target URL: " + targetUrl.toString());
        }

        shared = getSharedPreferences("pref", MODE_PRIVATE);
        editor = shared.edit();

        Gson gson = new Gson();
        String json = shared.getString("user", "");

        if(json == ""){

            Intent intent = new Intent(this, SplashScreenActivity.class);
            startActivity(intent);

        } else {

            user = gson.fromJson(json, User.class);
        }

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, NewHikeActivity.class);
                startActivity(intent);
            }
        });

    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    return new HomeFragment();
                case 1:
                    return new SearchFragment();
                case 2:
                    return new ProfileFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Home";
                case 1:
                    return "Search";
                case 2:
                    return "Profile";
            }
            return null;
        }
    }

    public void listGoing(View view){

        Intent intent = new Intent(this, ListGoingActivity.class);
        startActivity(intent);
    }

    public void listWishlist(View view){

        Intent intent = new Intent(this, ListWishlistActivity.class);
        startActivity(intent);
    }

    public void listMyHikes(View view){

        Intent intent = new Intent(this, MyHikesActivity.class);
        startActivity(intent);
    }

    public void signout(View view){

        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);

        LoginManager.getInstance().logOut();

        editor.putString("user", "");
        editor.commit();
    }
}
