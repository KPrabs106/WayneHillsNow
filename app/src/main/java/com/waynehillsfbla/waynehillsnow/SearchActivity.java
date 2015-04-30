package com.waynehillsfbla.waynehillsnow;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Sudharshan on 4/19/2015.
 */
public class SearchActivity extends AppCompatActivity {

    EditText input;
    ImageButton enterButton;
    RecyclerView recList;
    TextView failure;
    ProgressBar progressBar;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        failure = (TextView) findViewById(R.id.failureMessage);
        progressBar = (ProgressBar) findViewById(R.id.progressBar2);

        input = (EditText) findViewById(R.id.input);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        enterButton = (ImageButton) findViewById(R.id.enterButton);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                failure.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                if (input.getText().length() == 0) {
                    failure.setVisibility(View.VISIBLE);
                } else {
                    search(String.valueOf(input.getText()));
                }
            }
        });
        recList = (RecyclerView) findViewById(R.id.cardList);
        recList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);

        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void search(String searchTerm) {
        RequestParams requestParams = new RequestParams("search", searchTerm);
        ClientServerInterface.post("search.php", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                if (response.length() == 0) {
                    progressBar.setVisibility(View.INVISIBLE);
                    failure.setVisibility(View.VISIBLE);
                }
                initCards(response);
            }
        });
    }

    //Set up the cards
    private void initCards(JSONArray jsonArray) {
        EventAdapter ea = new EventAdapter(createList(jsonArray.length(), jsonArray));
        recList.setAdapter(ea);
        progressBar.setVisibility(View.INVISIBLE);
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
}
