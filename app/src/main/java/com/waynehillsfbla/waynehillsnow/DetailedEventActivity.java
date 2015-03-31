package com.waynehillsfbla.waynehillsnow;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.software.shell.fab.ActionButton;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.lucasr.twowayview.TwoWayView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

/**
 * ************************************************************
 * This activity displays event details in a more detailed view.
 * It has Google+ integration, which allows users to attend events
 * and allows users to see who all are attending events and.
 * *************************************************************
 */
public class DetailedEventActivity extends ActionBarActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        ResultCallback<People.LoadPeopleResult>, View.OnClickListener {
    String userId;

    String[] nameAttendees;
    String[] pictureAttendees;
    String[] googleIdAttendees;

    String[] nameCommenters;
    String[] pictureCommenters;
    String[] comments;

    String nameCurrentUser;
    Button attendButton;
    Button cancelButton;

    int id;
    String title;
    String type;
    String location;
    String description;
    String contact;
    String startDate;
    String endDate;
    String commentBody;
    ListView drawerList;
    ArrayAdapter<String> arrayAdapter;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    String activityTitle;
    private GoogleApiClient mGoogleApiClient;

    //TODO Add notifications for upcoming events
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_event);

        drawerList = (ListView) findViewById(R.id.navList);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        String[] drawerItems = {"Sign In", "Settings", "My Events"};

        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, drawerItems);
        Log.d(getPackageName(), drawerList != null ? "lvCountries is not null!" : "lvCountries is null!");

        drawerList.setAdapter(arrayAdapter);


        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent intent = new Intent(view.getContext(), GooglePlusSignIn.class);
                        startActivity(intent);
                }
            }
        });

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);
        activityTitle = getTitle().toString();

        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close) {
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

        };

        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        final Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        id = extras.getInt("Id");
        title = extras.getString("Title");
        type = extras.getString("Type");
        location = extras.getString("Location");
        description = extras.getString("Description");
        contact = extras.getString("Contact");
        startDate = extras.getString("StartDate");
        endDate = extras.getString("EndDate");

        mGoogleApiClient = buildGoogleApiClient();
        mGoogleApiClient.connect();

        Plus.PeopleApi.loadVisible(mGoogleApiClient, null)
                .setResultCallback(this);

        getComments();
        getAttendance();

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

        Button notificationButton = (Button) findViewById(R.id.notificationButton);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.MONTH, Integer.parseInt(startDate.substring(5,7))-1);
                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(startDate.substring(8,10)));
                calendar.set(Calendar.YEAR, Integer.parseInt(startDate.substring(0,4)));

                Intent notification = new Intent(getApplicationContext(), AlarmReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, notification, 0);

                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

                alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
            }
        });

        //The attend and cancel button are invisible by default, and become visible if the user is
        //logged into Google+
        attendButton = (Button) findViewById(R.id.attend_button);
        attendButton.setVisibility(View.INVISIBLE);
        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setVisibility(View.INVISIBLE);

        //If the user clicks on the attend button, send a JSON Object of event ID and google ID to
        //the webpage, which will then process it and add the user to the database
        attendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addAttendance();
            }
        });

        //If the user clicks on the cancel button, send a JSON Object of the event id and google ID
        //to the webpage, which will then process it and remove the user from the database
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeAttendance();
            }
        });

        ActionButton actionButton = (ActionButton) findViewById(R.id.action_button);

        /*actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle userEventDetails = new Bundle();
                userEventDetails.putString("eventId", String.valueOf(id));
                userEventDetails.putString("userId", userId);
                Intent writeComment = new Intent(getApplicationContext(), WriteCommentActivity.class);
                writeComment.putExtras(userEventDetails);
                startActivity(writeComment);
            }
        });*/

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter comment");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                commentBody = input.getText().toString();
                publishComment();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle userEventDetails = new Bundle();
                userEventDetails.putString("eventId", String.valueOf(id));
                userEventDetails.putString("userId", userId);

                builder.show();

            }
        });

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    private void restartActivity() {
        finish();
        startActivity(getIntent());
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

        day = date.substring(8, 10);
        month = date.substring(5, 7);
        year = date.substring(0, 4);
        hr = date.substring(11, 13);
        min = date.substring(14, 16);

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
        userId = currentUser.getId();

        final Button attendButton = (Button) findViewById(R.id.attend_button);
        final Button cancelButton = (Button) findViewById(R.id.cancel_button);

        //If the user is already attending the event, the appropriate buttons are enabled or disabled
        if (Arrays.asList(nameAttendees).contains(nameCurrentUser)) {
            attendButton.setEnabled(false);
            cancelButton.setEnabled(true);
        } else {
            attendButton.setEnabled(true);
            cancelButton.setEnabled(false);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

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

    @Override
    public void onClick(View v) {

    }

    private void getComments() {
        RequestParams requestParams = new RequestParams("eventId", id);
        ClientServerInterface.post("get_comments.php", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                nameCommenters = new String[response.length()];
                pictureCommenters = new String[response.length()];
                comments = new String[response.length()];

                for (int i = 0; i < response.length(); i++) {
                    try {
                        nameCommenters[i] = response.getJSONObject(i).getString("name");
                        pictureCommenters[i] = (response.getJSONObject(i).getString("profilePicture")).substring(0, 96) + "103";
                        comments[i] = response.getJSONObject(i).getString("body");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                initComments();
            }
        });
    }

    private void initComments() {
        CommentsListAdapter commentsListAdapter = new CommentsListAdapter(this, pictureCommenters, nameCommenters, comments);
        ListView commentsList = (ListView) findViewById(R.id.commentsList);
        commentsList.setAdapter(commentsListAdapter);
    }

    private void getAttendance() {
        RequestParams requestParams = new RequestParams("eventId", id);
        ClientServerInterface.post("get_attendance.php", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                nameAttendees = new String[response.length()];
                pictureAttendees = new String[response.length()];
                googleIdAttendees = new String[response.length()];

                for (int i = 0; i < response.length(); i++) {
                    try {
                        nameAttendees[i] = response.getJSONObject(i).getString("name");
                        pictureAttendees[i] = (response.getJSONObject(i).getString("profilePicture")).substring(0, 96) + "103";
                        googleIdAttendees[i] = response.getJSONObject(i).getString("googleId");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                TextView peopleAttending = (TextView) findViewById(R.id.peopleAttending);
                peopleAttending.setText(pictureAttendees.length + " people are attending.");

                initAttendance();
            }
        });
    }

    private void initAttendance() {
        //Create the list adapter that will add names and pictures to the list of those attending
        AttendeeListAdapter adapter = new AttendeeListAdapter(this, pictureAttendees);
        TwoWayView attendeeList = (TwoWayView) findViewById(R.id.lvItems);
        attendeeList.setAdapter(adapter);
    }

    private void addAttendance() {
        RequestParams requestParams = new RequestParams();
        requestParams.put("eventId", id);
        requestParams.put("userId", userId);

        ClientServerInterface.post("add_attendance.php", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                cancelButton.setEnabled(true);
                attendButton.setEnabled(false);
                restartActivity();
                Toast.makeText(getApplicationContext(), "You are now attending", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeAttendance() {
        RequestParams requestParams = new RequestParams();
        requestParams.put("eventId", id);
        requestParams.put("userId", userId);

        ClientServerInterface.post("remove_attendance.php", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                attendButton.setEnabled(true);
                cancelButton.setEnabled(false);
                restartActivity();
                Toast.makeText(getApplicationContext(), "You are no longer attending", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void publishComment() {
        RequestParams requestParams = new RequestParams();
        requestParams.put("eventId", id);
        requestParams.put("userId", userId);
        requestParams.put("commentBody", commentBody);
        ClientServerInterface.post("publish_comment.php", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                restartActivity();
            }
        });
    }
}