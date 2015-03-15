package com.waynehillsfbla.waynehillsnow;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.PersonBuffer;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Kartik on 3/14/2015.
 */
public class CommentsTab extends Fragment implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<People.LoadPeopleResult>, View.OnClickListener {

    View v;
    JSONObject userEventData = new JSONObject();
    String[] nameCommenter;
    String[] pictureCommenter;
    String[] googleIdCommenter;
    String[] comments;
    String nameCurrentUser;
    private GoogleApiClient mGoogleApiClient;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.comments_tab, container, false);

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

        Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        nameCurrentUser = currentUser.getDisplayName();
        try {
            userEventData.put("googleId", currentUser.getId());
        } catch (JSONException e) {
            e.printStackTrace();
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
}
