package com.waynehillsfbla.waynehillsnow;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.google.android.gms.common.SignInButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ****************************************************
 * This activity allows the user to go the calendar view
 * of events or the list view of events. There is also a
 * banner image scroller, which scrolls through images
 * of the next 5 events. This activity also is where
 * the user can sign in to Google+.
 * ****************************************************
 */
public class MainActivity extends ActionBarActivity implements BaseSliderView.OnSliderClickListener {

    JSONArray jarr;
    ClientServerInterface clientServerInterface = new ClientServerInterface();
    HashMap<String, String> pictureData = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Stop the app if there is no internet connection
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No Internet connection", Toast.LENGTH_LONG).show();
            finish();
        }

        ImageButton listButton = (ImageButton) findViewById(R.id.buttonList);
        ImageButton calendarButton = (ImageButton) findViewById(R.id.buttonCalendar);
        SignInButton signInButton = (SignInButton) findViewById(R.id.signin_button);


        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CardListActivity.class);
                startActivity(intent);
            }
        });

        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), CalendarActivity.class);
                startActivity(intent);
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), GooglePlusSignIn.class);
                startActivity(intent);
            }
        });

        RetrievePictures rp = new RetrievePictures();
        rp.execute();
        try {
            rp.get(2000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        SliderLayout imageScroller = (SliderLayout) findViewById(R.id.slider);

        JSONObject jsonObject;
        Bundle bundle;

        //Create banner image scroller and add each images
        for (int i = 0; i < jarr.length(); i++) {
            TextSliderView textSliderView = new TextSliderView(this);
            bundle = textSliderView.getBundle();

            try {
                jsonObject = jarr.getJSONObject(i);
                textSliderView.description(jsonObject.getString("title"))
                        .image(jsonObject.getString("pictureURL"))
                        .setScaleType(BaseSliderView.ScaleType.Fit)
                        .setOnSliderClickListener(this);
                bundle.putInt("Id", Integer.parseInt(jsonObject.getString("id")));
                bundle.putString("Title", jsonObject.getString("title"));
                bundle.putString("Type", jsonObject.getString("type"));
                bundle.putString("Location", jsonObject.getString("location"));
                bundle.putString("Description", jsonObject.getString("description"));
                bundle.putString("Contact", jsonObject.getString("contact"));
                bundle.putString("StartDate", jsonObject.getString("startDate"));
                bundle.putString("EndDate", jsonObject.getString("endDate"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            imageScroller.addSlider(textSliderView);
        }
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

    //Start a new activity when the banner image scroller is clicked with the event details
    @Override
    public void onSliderClick(BaseSliderView baseSliderView) {
        Bundle bundle = baseSliderView.getBundle();
        Intent intent = new Intent(baseSliderView.getContext(), DetailedEventActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    //Check for internet connectivity
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //Get picture URLs from the database and store it in a JSON Object
    class RetrievePictures extends AsyncTask<String, String, Void> {

        protected Void doInBackground(String... arg0) {
            jarr = clientServerInterface.makeHttpRequest("http://54.164.136.46/get_pictures.php");
            JSONObject jsonObject;
            for (int i = 0; i < jarr.length(); i++) {
                try {
                    jsonObject = jarr.getJSONObject(i);
                    pictureData.put(jsonObject.getString("title"), jsonObject.getString("pictureURL"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}
