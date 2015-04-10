package com.waynehillsfbla.waynehillsnow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.daimajia.slider.library.SliderTypes.BaseSliderView;

import java.util.HashMap;

/**
 * ****************************************************
 * This activity allows the user to go the calendar view
 * of events or the list view of events. There is also a
 * banner image scroller, which scrolls through images
 * of the next 5 events. This activity also is where
 * the user can sign in to Google+.
 * ****************************************************
 */
public class MainActivity extends ActionBarActivity implements BaseSliderView.OnSliderClickListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;

    Toolbar toolbar;
    ViewPager viewPager;
    ViewPagerAdapterMain viewPagerAdapter;
    SlidingTabLayout slidingTabLayout;
    CharSequence Titles[] = {"Events List", "Calendar"};
    int numTabs = 2;
    Intent intent;

    String[] drawerItems = {"Google Plus", "My Events", "Settings"};
    int[] icons = {R.drawable.ic_sign_in, R.drawable.ic_my_events, R.drawable.ic_settings};

    HashMap<String, String> pictureData = new HashMap<String, String>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView drawerRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        drawerRecyclerView.setHasFixedSize(true);

        RecyclerView.Adapter drawerListAdapter;
        if (isSignedIn()) {
            SharedPreferences userDetails = getSharedPreferences("userDetails", MODE_PRIVATE);
            String name = userDetails.getString("displayName", null);
            String profilePictureURL = userDetails.getString("profilePictureURL", null);
            drawerListAdapter = new DrawerListAdapter(drawerItems, icons, name, profilePictureURL);
        } else {
            drawerListAdapter = new DrawerListAdapter(drawerItems, icons);
        }

        drawerRecyclerView.setAdapter(drawerListAdapter);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        drawerRecyclerView.setLayoutManager(layoutManager);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

        };

        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        viewPagerAdapter = new ViewPagerAdapterMain(getSupportFragmentManager(), Titles, numTabs);

        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(viewPagerAdapter);

        slidingTabLayout = (SlidingTabLayout) findViewById(R.id.tabs);
        slidingTabLayout.setDistributeEvenly(true);

        slidingTabLayout.setViewPager(viewPager);
    }

    private boolean isSignedIn() {
        return getSharedPreferences("userDetails", MODE_PRIVATE).contains("displayName");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        actionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    //Start a new activity when the banner image scroller is clicked with the event details
    @Override
    public void onSliderClick(BaseSliderView baseSliderView) {
        Bundle bundle = baseSliderView.getBundle();
        Intent intent = new Intent(baseSliderView.getContext(), DetailedEventActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
