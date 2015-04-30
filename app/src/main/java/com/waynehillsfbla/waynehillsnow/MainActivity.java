package com.waynehillsfbla.waynehillsnow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * ****************************************************
 * This activity allows the user to go the calendar view
 * of events or the list view of events. There is also a
 * banner image scroller, which scrolls through images
 * of the next 5 events. This activity also is where
 * the user can sign in to Google+.
 * ****************************************************
 */
public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;

    Intent intent;

    TextView titleTextView;
    ImageView pictureImageView;
    TextView dateTextView;
    RecyclerView recList;
    SwipeRefreshLayout swipeRefreshLayout;
    LinearLayoutManager lin;

    String[] drawerItems = {"Google Plus", "My Events", "Calendar","Live@Hills", "Search"};
    int[] icons = {R.drawable.ic_sign_in, R.drawable.ic_my_events, R.drawable.ic_calendar, R.drawable.ic_photos, R.drawable.ic_search};

    //TODO weather drawer layout


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView drawerRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        drawerRecyclerView.setHasFixedSize(true);

        final RecyclerView.Adapter drawerListAdapter;
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        titleTextView = (TextView) findViewById(R.id.txtTitle);
        pictureImageView = (ImageView) findViewById(R.id.picture);
        dateTextView = (TextView) findViewById(R.id.txtDate);
        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        lin = new LinearLayoutManager(getApplicationContext());
        lin.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(lin);

        recList.setOnScrollListener(new RecyclerView.OnScrollListener() {

            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            }

            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                swipeRefreshLayout.setEnabled(lin.findFirstCompletelyVisibleItemPosition() == 0);
            }
        });

        getEvents();

    }

    @Override
    public void onRefresh() {
        getEvents();
    }

    public boolean canScrollVertically(int direction) {
        // check if scrolling up
        if (direction < 1) {
            boolean original = recList.canScrollVertically(direction);
            return !original && recList.getChildAt(0) != null && recList.getChildAt(0).getTop() < 0 || original;
        }
        return recList.canScrollVertically(direction);

    }

    //Set up the cards
    private void initCards(JSONArray jsonArray) {
        EventAdapter ea = new EventAdapter(createList(jsonArray.length(), jsonArray));
        recList.setAdapter(ea);
        swipeRefreshLayout.setRefreshing(false);
    }

    //Give the adapter all the information about each event
    private List<EventInfo> createList(int size, JSONArray eventDetails) {
        List<EventInfo> result = new ArrayList<EventInfo>();
        JSONObject jsonObject;
        for (int i = 0; i < size; i++) {
            EventInfo ei = new EventInfo();
            try {
                jsonObject = eventDetails.getJSONObject(i);
                ei.id = Integer.parseInt(jsonObject.getString("id"));
                ei.title = jsonObject.getString("title");
                ei.startDatetime = jsonObject.getString("startDate");
                ei.pictureURL = jsonObject.getString("pictureURL");
                ei.type = jsonObject.getString("type");
                ei.contact = jsonObject.getString("contact");
                ei.endDatetime = jsonObject.getString("endDate");
                ei.location = jsonObject.getString("location");
                ei.description = jsonObject.getString("description");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            result.add(ei);
        }
        return result;
    }

    private void getEvents() {
        ClientServerInterface.get("get_events.php", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                initCards(response);
            }
        });
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



}
