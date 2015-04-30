package com.waynehillsfbla.waynehillsnow;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * ******************************************************
 * This activity presents the events in a list of cards.
 * It gets the event information from the database.
 * ******************************************************
 */
public class ListEventActivity extends AppCompatActivity {
    int year;
    int month;
    int day;
    RecyclerView recList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_event);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        year = extras.getInt("year");
        month = extras.getInt("month");
        day = extras.getInt("day");

        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        getEvents();
    }

    private List<EventInfo> createList(int size, JSONArray jsonArray) {
        List<EventInfo> result = new ArrayList<EventInfo>();
        for (int i = 0; i < size; i++) {
            EventInfo ei = new EventInfo();
            try {
                ei.id = Integer.parseInt(jsonArray.getJSONObject(i).getString("id"));
                ei.title = jsonArray.getJSONObject(i).getString("title");
                ei.startDatetime = jsonArray.getJSONObject(i).getString("startDate");
                ei.pictureURL = jsonArray.getJSONObject(i).getString("pictureURL");
                ei.type = jsonArray.getJSONObject(i).getString("type");
                ei.contact = jsonArray.getJSONObject(i).getString("contact");
                ei.endDatetime = jsonArray.getJSONObject(i).getString("endDate");
                ei.location = jsonArray.getJSONObject(i).getString("location");
                ei.description = jsonArray.getJSONObject(i).getString("description");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            result.add(ei);
        }
        return result;
    }

    @Override
    public void onBackPressed() {
        finish();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initCards(JSONArray jsonArray) {
        EventAdapter ea = new EventAdapter(createList(jsonArray.length(), jsonArray));
        recList.setAdapter(ea);
    }

    private void getEvents() {
        RequestParams requestParams = new RequestParams();
        requestParams.put("year", year);
        requestParams.put("month", month);
        requestParams.put("day", day);
        ClientServerInterface.post("get_specific_events.php", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                initCards(response);
            }
        });
    }
}
