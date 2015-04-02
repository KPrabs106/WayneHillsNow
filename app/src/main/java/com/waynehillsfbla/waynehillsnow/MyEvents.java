package com.waynehillsfbla.waynehillsnow;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.plus.model.people.Person;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MyEvents extends ActionBarActivity {
    TextView titleTextView;
    ImageView pictureImageView;
    TextView dateTextView;
    TextView typeTextView;
    RecyclerView recList;

    String userId;
    int eventId;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_events);

        titleTextView = (TextView) findViewById(R.id.txtTitle);
        pictureImageView = (ImageView) findViewById(R.id.picture);
        dateTextView = (TextView) findViewById(R.id.txtDate);
        typeTextView = (TextView) findViewById(R.id.txtType);
        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);

        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        getEvents();

    }

    //Set up the cards
    private void initCards(JSONArray jsonArray) {
        EventAdapter ea = new EventAdapter(createList(jsonArray.length(), jsonArray));
        recList.setAdapter(ea);
    }

    //Give the adapter all the information about each event
    private List<EventInfo> createList(int size, JSONArray eventDetails) {

        List<EventInfo> result = new ArrayList<EventInfo>();
        JSONObject jsonObject;
        for (int i = 0; i < size; i++) {
            EventInfo ei = new EventInfo();
            try {
                jsonObject = eventDetails.getJSONObject(i);
                if(isAttending(userId, ei.id)) {
                    ei.id = Integer.parseInt(jsonObject.getString("id"));
                    ei.title = jsonObject.getString("title");
                    ei.startDatetime = jsonObject.getString("startDate");
                    ei.pictureURL = jsonObject.getString("pictureURL");
                    ei.type = jsonObject.getString("type");
                    ei.contact = jsonObject.getString("contact");
                    ei.endDatetime = jsonObject.getString("endDate");
                    ei.location = jsonObject.getString("location");
                    ei.description = jsonObject.getString("description");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            result.add(ei);
        }
        return result;
    }

    private boolean isAttending(String user, int event) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("eventId", eventId);
        requestParams.put("userId", userId);
        boolean b = false;

        ClientServerInterface.get("event_users.php", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    if(response.getJSONObject(1) == eventId)
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });


    }

    private void getEvents() {
        ClientServerInterface.get("get_events.php", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                initCards(response);
            }
        });
    }
}