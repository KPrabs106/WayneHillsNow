package com.waynehillsfbla.waynehillsnow;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.software.shell.fab.ActionButton;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * This activity displays all the uploaded images
 */
public class LiveAtHillsActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    SwipeRefreshLayout swipeRefreshLayout;
    RecyclerView recyclerView;
    ActionButton actionButton;
    LinearLayoutManager llm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_at_hills);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        actionButton = (ActionButton) findViewById(R.id.action_button);
        recyclerView = (RecyclerView) findViewById(R.id.photoCardList);

        recyclerView.setHasFixedSize(true);
        llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(llm);

        //Prevents conflict between scrolling and SwipeRefreshLayout
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            }

            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                swipeRefreshLayout.setEnabled(llm.findFirstCompletelyVisibleItemPosition() == 0);
            }
        });

        getUploadedPictures();

        //Only allow the user to upload pictures if they are logged in
        if (!isSignedIn()) {
            actionButton.setVisibility(View.INVISIBLE);
        }

        //If the "+" Button is clicked, the Image Upload activity is started
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ImageUploadActivity.class));
            }
        });
    }

    //Check if the user is signed in
    private boolean isSignedIn() {
        return getSharedPreferences("userDetails", MODE_PRIVATE).contains("displayName");
    }

    //Get all uploaded pictures
    private void getUploadedPictures() {
        ClientServerInterface.post("get_uploaded_pictures.php", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                initCards(response);
            }
        });
        swipeRefreshLayout.setRefreshing(false);
    }

    //Give the adapter all the information about the photos
    private List<PhotoInfo> createList(int size, JSONArray photoData) {
        List<PhotoInfo> result = new ArrayList<PhotoInfo>();
        for (int i = 0; i < size; i++) {
            PhotoInfo photoInfo = new PhotoInfo();
            try {
                photoInfo.eventTitle = photoData.getJSONObject(i).getString("title");
                photoInfo.pictureURL = photoData.getJSONObject(i).getString("picture_link");
                photoInfo.submitterName = photoData.getJSONObject(i).getString("name");
                photoInfo.submitterGoogleId = photoData.getJSONObject(i).getString("googleId");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            result.add(photoInfo);
        }
        return result;
    }

    //Set up the cards
    private void initCards(JSONArray data) {
        PhotoAdapter photoAdapter = new PhotoAdapter(createList(data.length(), data), this);
        recyclerView.setAdapter(photoAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_live_at_hills, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void onRefresh() {
        getUploadedPictures();
    }
}
