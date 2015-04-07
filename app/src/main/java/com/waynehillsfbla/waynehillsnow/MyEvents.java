package com.waynehillsfbla.waynehillsnow;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.TextView;

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

        getMyEvents();
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
