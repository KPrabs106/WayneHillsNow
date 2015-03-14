package com.waynehillsfbla.waynehillsnow;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
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

import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Sudharshan on 3/13/2015.
 */
public class AttendanceTab extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<People.LoadPeopleResult>, View.OnClickListener {

    JSONObject userEventData = new JSONObject();
    String[] nameAttendees;
    String[] pictureAttendees;
    String[] googleIdAttendees;
    String nameCurrentUser;
    Button attendButton;
    Button cancelButton;

    private GoogleApiClient mGoogleApiClient;

    View v;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.attendance_tab, container, false);
        mGoogleApiClient = buildGoogleApiClient();
        mGoogleApiClient.connect();

        Intent intent = getActivity().getIntent();
        Bundle extras = intent.getExtras();

        int id = extras.getInt("Id");
        Plus.PeopleApi.loadVisible(mGoogleApiClient, null)
                .setResultCallback(this);

        //Put the event id into a JSON Object that is sent if a user clicks a button
        JSONObject eventDetails = new JSONObject();
        try {
            eventDetails.put("eventId", Integer.toString(id));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Get the names and profile pictures of those attending the event
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

        ListView listView = (ListView) v.findViewById(R.id.list);

        //Create the list adapter that will add names and pictures to the list of those attending
        AttendeeListAdapter adapter = new AttendeeListAdapter(getActivity(), nameAttendees, pictureAttendees);
        listView.setAdapter(adapter);

        ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.progressBar2);
        progressBar.setVisibility(View.INVISIBLE);

        //Put the eventId into another JSON Object that could be sent
        try {
            userEventData.put("eventId", Integer.toString(id));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //The attend and cancel button are invisible by default, and become visible if the user is
        //logged into Google+
        attendButton = (Button) v.findViewById(R.id.attend_button);
        attendButton.setVisibility(View.INVISIBLE);
        cancelButton = (Button) v.findViewById(R.id.cancel_button);
        cancelButton.setVisibility(View.INVISIBLE);

        //If the user clicks on the attend button, send a JSON Object of event ID and google ID to
        //the webpage, which will then process it and add the user to the database
        attendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddAttendance addAttendance = new AddAttendance();
                addAttendance.execute(userEventData);
                cancelButton.setEnabled(true);
                attendButton.setEnabled(false);
                //TODO figure out how to restart
                //getActivity().restartActivity();
                Toast.makeText(getActivity().getApplicationContext(), "You are now attending", Toast.LENGTH_SHORT).show();
            }
        });

        //If the user clicks on the cancel button, send a JSON Object of the event id and google ID
        //to the webpage, which will then process it and remove the user from the database
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RemoveAttendance removeAttendance = new RemoveAttendance();
                removeAttendance.execute(userEventData);
                attendButton.setEnabled(true);
                cancelButton.setEnabled(false);
                //restartActivity();
                Toast.makeText(getActivity().getApplicationContext(), "You are no longer attending", Toast.LENGTH_SHORT).show();

            }
        });

        return v;
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
            Log.e("Error requesting visible circles: ", peopleData.getStatus().toString());
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        //The user is connected to Google+, which means they can now attend events
        attendButton.setVisibility(View.VISIBLE);
        cancelButton.setVisibility(View.VISIBLE);

        Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        nameCurrentUser = currentUser.getDisplayName();
        try {
            userEventData.put("googleId", currentUser.getId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Button attendButton = (Button) v.findViewById(R.id.attend_button);
        final Button cancelButton = (Button) v.findViewById(R.id.cancel_button);

        //If the user is already attending the event, the appropriate buttons are enabled or disabled
        if (Arrays.asList(nameAttendees).contains(nameCurrentUser)) {
            attendButton.setEnabled(false);
            cancelButton.setEnabled(true);
        } else {
            attendButton.setEnabled(true);
            cancelButton.setEnabled(false);
        }
    }

    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

    private GoogleApiClient buildGoogleApiClient() {
        // When we build the GoogleApiClient we specify where connected and
        // connection failed callbacks should be returned, which Google APIs our
        // app uses and which OAuth 2.0 scopes our app requests.
        return new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    //Gets names and pictures of those attending this event, given JSON Object of event id
    class GetAttendance extends AsyncTask<JSONObject, Void, Void> {

        @Override
        protected Void doInBackground(JSONObject... params) {
            JSONObject jsonObject = params[0];
            ClientServerInterface clientServerInterface = new ClientServerInterface();
            JSONArray attendanceDetails = clientServerInterface.postData("http://54.164.136.46/get_attendance.php", jsonObject);

            JSONArray jarr = null;
            try {
                jarr = new JSONArray(attendanceDetails.get(0).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            nameAttendees = new String[jarr.length()];
            pictureAttendees = new String[jarr.length()];
            googleIdAttendees = new String[jarr.length()];

            for (int i = 0; i < jarr.length(); i++) {
                try {
                    JSONObject jObject = jarr.getJSONObject(i);
                    nameAttendees[i] = jObject.getString("name");
                    pictureAttendees[i] = (jObject.getString("profilePicture")).substring(0, 96) + "103";
                    googleIdAttendees[i] = jObject.getString("googleId");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            return null;
        }
    }

    //Adds user to the database, given JSON Object of user id and event id
    class AddAttendance extends AsyncTask<JSONObject, Void, Void> {
        protected Void doInBackground(JSONObject... params) {
            JSONObject jsonObject = params[0];
            ClientServerInterface clientServerInterface = new ClientServerInterface();
            clientServerInterface.postData("http://54.164.136.46/add_attendance.php", jsonObject);
            return null;
        }
    }

    //Removes user from the database, given JSON Object of user id and event id
    class RemoveAttendance extends AsyncTask<JSONObject, Void, Void> {
        @Override
        protected Void doInBackground(JSONObject... params) {
            JSONObject jsonObject = params[0];
            ClientServerInterface clientServerInterface = new ClientServerInterface();
            clientServerInterface.postData("http://54.164.136.46/remove_attendance.php", jsonObject);
            return null;
        }
    }

}
