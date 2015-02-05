package com.waynehillsfbla.waynehillsnow;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class CardListActivity extends ActionBarActivity {
    JSONArray jarr = null;
    JSONObject jobj = null;
    ClientServerInterface clientServerInterface = new ClientServerInterface();
    TextView titleTextView;
    ImageView pictureImageView;
    TextView dateTextView;
    TextView typeTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_list);

        //Stop app if there is no internet connection
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No Internet connection", Toast.LENGTH_LONG).show();
            finish();
        }

        titleTextView = (TextView) findViewById(R.id.txtTitle);
        pictureImageView = (ImageView) findViewById(R.id.picture);
        dateTextView = (TextView) findViewById(R.id.txtDate);
        typeTextView = (TextView) findViewById(R.id.txtType);

        RetrieveData rd = new RetrieveData();
        rd.execute();

        try {
            rd.get(10000, TimeUnit.MILLISECONDS);
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_card_list, menu);
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

    //Check for internet connectivity
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //Get the event details
    class RetrieveData extends AsyncTask<String, String, JSONArray> {
        protected JSONArray doInBackground(String... arg0) {
            jarr = clientServerInterface.makeHttpRequest("http://54.164.136.46/printresult.php");
            return jarr;
        }
    }
}
