package com.waynehillsfbla.waynehillsnow;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EventListTab extends Fragment {
    JSONArray jarr = null;
    JSONObject jobj = null;
    ClientServerInterface clientServerInterface = new ClientServerInterface();
    TextView titleTextView;
    ImageView pictureImageView;
    TextView dateTextView;
    TextView typeTextView;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_card_list, container, false);

        titleTextView = (TextView) v.findViewById(R.id.txtTitle);
        pictureImageView = (ImageView) v.findViewById(R.id.picture);
        dateTextView = (TextView) v.findViewById(R.id.txtDate);
        typeTextView = (TextView) v.findViewById(R.id.txtType);

        RetrieveData rd = new RetrieveData();
        rd.execute();

        //Wait until the JSON data from the server is received
        try {
            rd.get(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }

        //Set up the cards
        RecyclerView recList = (RecyclerView) v.findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity().getApplicationContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        EventAdapter ea = new EventAdapter(createList(jarr.length()));
        recList.setAdapter(ea);
        return v;
    }

    //Give the adapter all the information about each event
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

    //Get the event details
    class RetrieveData extends AsyncTask<String, String, JSONArray> {
        protected JSONArray doInBackground(String... arg0) {
            jarr = clientServerInterface.makeHttpRequest("http://54.164.136.46/printresult.php");
            return jarr;
        }
    }
}
