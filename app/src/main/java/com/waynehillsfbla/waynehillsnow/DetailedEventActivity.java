package com.waynehillsfbla.waynehillsnow;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.sql.ClientInfoStatus;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class DetailedEventActivity extends ListActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<People.LoadPeopleResult>, View.OnClickListener {
    JSONObject userEventData = new JSONObject();
    String[] nameAttendees;
    String[] pictureAttendees;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_event);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        int id = extras.getInt("Id");
        String title = extras.getString("Title");
        String type = extras.getString("Type");
        String location = extras.getString("Location");
        String description = extras.getString("Description");
        String contact = extras.getString("Contact");
        String startDate = extras.getString("StartDate");
        String endDate = extras.getString("EndDate");

        mGoogleApiClient = buildGoogleApiClient();
        mGoogleApiClient.connect();

        Plus.PeopleApi.loadVisible(mGoogleApiClient, null)
                .setResultCallback(this);

        TextView txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText(title);

        TextView txtType = (TextView) findViewById(R.id.txtType);
        txtType.setText(type);

        TextView txtLocation = (TextView) findViewById(R.id.txtLocation);
        txtLocation.setText(location);

        TextView txtDescription = (TextView) findViewById(R.id.txtDesc);
        txtDescription.setText(description);

        TextView txtContact = (TextView) findViewById(R.id.txtContact);
        txtContact.setText(contact);

        TextView txtStartDate = (TextView) findViewById(R.id.txtStartDate);
        try {
            txtStartDate.setText(getDetailedDisplayDate(startDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        TextView txtEndDate = (TextView) findViewById(R.id.txtEndDate);
        try {
            txtEndDate.setText(getDetailedDisplayDate(endDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        JSONObject eventDetails = new JSONObject();
        try {
            eventDetails.put("eventId", Integer.toString(id));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        GetAttendance getAttendance = new GetAttendance();
        getAttendance.execute(eventDetails);
        try {
            getAttendance.get(10000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }

        AttendeeListAdapter adapter = new AttendeeListAdapter(this, nameAttendees, pictureAttendees);
        setListAdapter(adapter);

        ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.INVISIBLE);

        try {
            userEventData.put("eventId", Integer.toString(id) );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Button attendButton = (Button) findViewById(R.id.attend_button);
        final TextView attendStatus = (TextView) findViewById(R.id.attend_status);

        //TODO make button invisible when no one is logged in
        //TODO make button grey when already logged in
        //TODO add button to not attend

        attendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attendStatus.setText("You are attending");
                AddAttendance addAttendance = new AddAttendance();
                addAttendance.execute(userEventData);
                Toast.makeText(getApplicationContext(), "You are now attending.", Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detailed_event, menu);
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

    private String getDetailedDisplayDate(String date) throws ParseException {
        String day, month, year, hr, min, result;

        SimpleDateFormat origForm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        SimpleDateFormat dispForm = new SimpleDateFormat("EEEE, MM/dd/yy hh:mm aa");

        day = date.substring(8,10);
        month = date.substring(5,7);
        year = date.substring(0,4);
        hr = date.substring(11,13);
        min = date.substring(14,16);

        result = year + "-" + month + "-" + day + " " + hr + ":" + min;

        return dispForm.format(origForm.parse(result));
    }

    @Override
    public void onResult(People.LoadPeopleResult peopleData) {

        if (peopleData.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
            PersonBuffer personBuffer = peopleData.getPersonBuffer();
            try {
                int count = personBuffer.getCount();
                for (int i = 0; i < count; i++) {
                    Log.d("Display name: ", personBuffer.get(i).getDisplayName());
                }
            } finally {
                personBuffer.close();
            }
        } else {
            Log.e( "Error requesting visible circles: ", peopleData.getStatus().toString());
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        Log.e("Connection", "connected");
        try {
            userEventData.put("googleId", currentUser.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        /*try {
            userEventData.put("googleId", currentUser.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void onPause()
    {
        super.onPause();
        Log.e("Exit", "onPause() is called");
        mGoogleApiClient.disconnect();
    }

    class GetAttendance extends AsyncTask<JSONObject, Void, Void>
    {

        @Override
        protected Void doInBackground(JSONObject... params) {
            JSONObject jsonObject = params[0];
            ClientServerInterface clientServerInterface = new ClientServerInterface();
            JSONArray attendanceDetails = clientServerInterface.postData("http://54.164.136.46/get_attendance.php", jsonObject);
            Log.e("Attendance Details: ", attendanceDetails.toString());

            nameAttendees = new String[attendanceDetails.length()];
            pictureAttendees = new String[attendanceDetails.length()];

            JSONObject jobj = null;

            for(int i = 0; i < attendanceDetails.length(); i++)
            {
                try {
                    //Log.e("Attendance details get i: ", attendanceDetails.get(i).toString());
                    JSONArray jarr = new JSONArray(attendanceDetails.get(i).toString());
                    JSONObject jObject = jarr.getJSONObject(i);
                    //JSONObject jObject =  new JSONObject(attendanceDetails.get(i).toString());
                    nameAttendees[i] = jObject.getString("name");
                    pictureAttendees[i] = jObject.getString("profilePicture");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }


    class AddAttendance extends AsyncTask<JSONObject,Void,Void>
    {
        protected Void doInBackground(JSONObject... params) {
            JSONObject jsonObject = params[0];
            ClientServerInterface clientServerInterface = new ClientServerInterface();
            clientServerInterface.postData("http://54.164.136.46/add_attendance.php", jsonObject);
            //Toast.makeText(getApplicationContext(), "You are now attending.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    class removeAttendance extends AsyncTask<JSONObject,Void,Void>
    {
        @Override
        protected Void doInBackground(JSONObject... params) {
            JSONObject jsonObject = params[1];
            ClientServerInterface clientServerInterface = new ClientServerInterface();
            //TODO add new URL
            //clientServerInterface.postData(, jsonObject);
            Toast.makeText(getApplicationContext(), "You are no longer attending.", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private GoogleApiClient buildGoogleApiClient() {
        // When we build the GoogleApiClient we specify where connected and
        // connection failed callbacks should be returned, which Google APIs our
        // app uses and which OAuth 2.0 scopes our app requests.
        return new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

}
