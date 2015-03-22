package com.waynehillsfbla.waynehillsnow;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ******************************************************
 * This activity presents the events in a list of cards.
 * It gets the event information from the database.
 * ******************************************************
 */
public class ListEventActivity extends ActionBarActivity {
    JSONObject json = new JSONObject();
    JSONArray jarr = new JSONArray();
    JSONObject jobj = null;
    PostData pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_event);

        //Stop app if there is no internet connection
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No Internet connection", Toast.LENGTH_LONG).show();
            finish();
        }

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        int year = extras.getInt("year");
        int month = extras.getInt("month");
        int day = extras.getInt("day");

        try {
            json.put("year", year);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            json.put("month", month);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            json.put("day", day);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        pd = new PostData();
        pd.execute();

        try {
            pd.get(1000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }

        RecyclerView recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        EventAdapter ea = new EventAdapter(createList(jarr.length()));
        recList.setAdapter(ea);
    }

    private List<EventInfo> createList(int size) {
        List<EventInfo> result = new ArrayList<EventInfo>();
        for (int i = 0; i < size; i++) {
            EventInfo ei = new EventInfo();
            try {
                jobj = jarr.getJSONObject(i);
                ei.id = Integer.parseInt(jobj.getString("id"));
                ei.title = jobj.getString("title");
                ei.startDatetime = jobj.getString("startDate");
                ei.pictureURL = jobj.getString("pictureURL");
                ei.type = jobj.getString("type");
                ei.contact = jobj.getString("contact");
                ei.endDatetime = jobj.getString("endDate");
                ei.location = jobj.getString("location");
                ei.description = jobj.getString("description");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            result.add(ei);
        }
        return result;
    }

    @Override
    public void onBackPressed() {
        pd.cancel(true);
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

    //Check for internet connectivity
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //Get all activities for a given day
    class PostData extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            ClientServerInterface clientServerInterface = new ClientServerInterface();
            JSONArray events = clientServerInterface.postData("http://54.164.136.46/decodejson.php", json);

            try {
                jarr = new JSONArray(events.get(0).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
