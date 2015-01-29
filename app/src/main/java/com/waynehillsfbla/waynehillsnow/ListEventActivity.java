package com.waynehillsfbla.waynehillsnow;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class ListEventActivity extends ActionBarActivity {
    JSONObject json = new JSONObject();
    JSONArray jarr = new JSONArray();
    JSONObject jobj = null;
    PostData pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_event);
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
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
        super.onBackPressed();
        pd.cancel(true);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_event, menu);
        return true;
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

    class PostData extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            ClientServerInterface clientServerInterface = new ClientServerInterface();
            jarr = clientServerInterface.postData("http://54.164.136.46/decodejson.php", json);
            return null;
        }
    }
}
