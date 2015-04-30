package com.waynehillsfbla.waynehillsnow;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This activity displays the events that the user is attending
 */
public class MyEventsActivity extends AppCompatActivity {
    RecyclerView recList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_events);

        if (!isSignedIn()) {
            Intent signInIntent = new Intent(getApplicationContext(), GooglePlusSignIn.class);
            startActivity(signInIntent);
            Toast.makeText(getApplicationContext(), "You need to be signed in.", Toast.LENGTH_SHORT).show();
            finish();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        getMyEvents();
    }

    //Set up the cards
    private void initCards(JSONArray jsonArray) {
        EventAdapter ea = new EventAdapter(createList(jsonArray.length(), jsonArray));
        recList.setAdapter(ea);
    }

    //Check if the user is signed in
    private boolean isSignedIn() {
        return getSharedPreferences("userDetails", MODE_PRIVATE).contains("displayName");
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

    //Get the events that a particular user is attending
    private void getMyEvents() {
        RequestParams requestParams = new RequestParams();
        requestParams.put("googleId", getSharedPreferences("userDetails", MODE_PRIVATE).getString("googleId", null));
        ClientServerInterface.post("get_my_events.php", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                initCards(response);
            }
        });
    }
}
