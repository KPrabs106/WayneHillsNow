package com.waynehillsfbla.waynehillsnow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.software.shell.fab.ActionButton;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.lucasr.twowayview.TwoWayView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

/**
 * ************************************************************
 * This activity displays event details in a more detailed view.
 * It has Google+ integration, which allows users to attend events
 * and allows users to see who all are attending events and.
 * *************************************************************
 */
public class DetailedEventActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {
    String nameCurrentUser;
    String userId;

    String[] nameAttendees;
    String[] pictureAttendees;
    String[] googleIdAttendees;

    String[] nameCommenters;
    String[] pictureCommenters;
    String[] comments;

    Button attendButton;
    Button cancelButton;
    AlertDialog.Builder commentDialog;

    int timeBefore;
    AlertDialog.Builder timePick;
    NumberPicker numPick;

    int id;
    String title;
    String type;
    String location;
    String description;
    String contact;
    String startDate;
    String endDate;
    ImageView attendIcon;
    ImageView notifIcon;
    String commentBody;
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    SwipeRefreshLayout swipeRefreshLayout;
    TextView txtTitle;
    TextView txtType;
    TextView txtLocation;
    TextView txtDescription;
    TextView txtContact;
    TextView txtStartDate;
    TextView txtEndDate;
    RequestParams eventIdParam;
    private int x;

    //TODO Add notifications for upcoming events
    //TODO fix crash when not signed in

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_event);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        attendIcon = (ImageView) findViewById(R.id.attendIcon);
        notifIcon = (ImageView) findViewById(R.id.notifIcon);
        x = 75;
        Picasso.with(getApplicationContext()).load(R.drawable.notify).resize(x,x).into(notifIcon);
        Picasso.with(getApplicationContext()).load(R.drawable.attend).resize(x,x).into(attendIcon);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

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

        //The attend and cancel button are invisible by default, and become visible if the user is
        //logged into Google+
        attendButton = (Button) findViewById(R.id.attend_button);
        attendButton.setEnabled(false);
        cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setEnabled(false);

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

        eventIdParam = new RequestParams("eventId", id);

        if (isSignedIn())
            initGooglePlus();

        getComments();
        getAttendance();
        getWeather();

        txtTitle = (TextView) findViewById(R.id.txtTitle);
        txtTitle.setText(title);

        txtType = (TextView) findViewById(R.id.txtType);
        txtType.setText(type);

        txtLocation = (TextView) findViewById(R.id.txtLocation);
        txtLocation.setText(location);

        txtDescription = (TextView) findViewById(R.id.txtDesc);
        txtDescription.setText(description);

        txtContact = (TextView) findViewById(R.id.txtContact);
        txtContact.setText(contact);

        txtStartDate = (TextView) findViewById(R.id.txtStartDate);
        if(startDate != null)
        {
            try {
                txtStartDate.setText(getDetailedDisplayDate(startDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        txtEndDate = (TextView) findViewById(R.id.txtEndDate);
        if(endDate != null){
            try {
                txtEndDate.setText(getDetailedDisplayDate(endDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        getEventDetails();
        getLocationDetails();

        timePick = new AlertDialog.Builder(this);
        timePick.setTitle("How long before to notify?");
        //timePick.setView(numPick);

        Button notificationButton = (Button) findViewById(R.id.notificationButton);
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.MONTH, Integer.parseInt(startDate.substring(5, 7)) - 1);
                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(startDate.substring(8, 10)));
                calendar.set(Calendar.YEAR, Integer.parseInt(startDate.substring(0, 4)));

                Intent notification = new Intent(DetailedEventActivity.this.getApplicationContext(), AlarmReceiver.class);

                PendingIntent pendingIntent = PendingIntent.getBroadcast(DetailedEventActivity.this.getApplicationContext(), 0, notification, 0);

                AlarmManager alarmManager = (AlarmManager) DetailedEventActivity.this.getSystemService(ALARM_SERVICE);

                alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis()-timeBefore, pendingIntent);*/

                timePick.show();
            }
        });


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

        commentDialog = new AlertDialog.Builder(this);
        commentDialog.setTitle("Enter comment");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        commentDialog.setView(input);

        // Set up the buttons
        commentDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                commentBody = input.getText().toString();
                DetailedEventActivity.this.publishComment();
                dialog.dismiss();
                onRefresh();
            }
        });
        commentDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        commentDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    DetailedEventActivity.this.finish();
                    //dialog.dismiss();
                }
                return true;
            }
        });

        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle userEventDetails = new Bundle();
                userEventDetails.putString("eventId", String.valueOf(id));
                userEventDetails.putString("userId", userId);

                commentDialog.show();
            }
        });
    }

    private boolean isSignedIn() {
        return getSharedPreferences("userDetails", MODE_PRIVATE).contains("displayName");
    }

    public void attendanceDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.requestWindowFeature(Window.FEATURE_SWIPE_TO_DISMISS);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.attendance_dialog);
        ListView fullAttendanceList = (ListView) dialog.findViewById(R.id.fullAttendanceList);
        FullAttendanceAdapter fullAttendanceAdapter = new FullAttendanceAdapter(this, nameAttendees, pictureAttendees);
        fullAttendanceList.setAdapter(fullAttendanceAdapter);
        dialog.show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
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

    //The user is connected to Google+, which means they can now attend events
    private void initGooglePlus() {
        SharedPreferences userDetails = getSharedPreferences("userDetails", MODE_PRIVATE);
        nameCurrentUser = userDetails.getString("displayName", null);
        userId = userDetails.getString("googleId", null);
    }

    private void getEventDetails(){
        ClientServerInterface.post("get_event_details.php", eventIdParam, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                try {
                    initEventDetails(response.getJSONObject(0));
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initEventDetails(JSONObject event) throws JSONException, ParseException {
        txtTitle.setText(event.getString("title"));
        txtType.setText(event.getString("type"));
        txtLocation.setText(event.getString("location"));
        txtDescription.setText(event.getString("description"));
        txtContact.setText(event.getString("contact"));
        txtStartDate.setText(getDetailedDisplayDate(event.getString("startDate")));
        txtEndDate.setText(getDetailedDisplayDate(event.getString("endDate")));
    }

    private void getLocationDetails() {
        ClientServerInterface.post("get_location_details.php", eventIdParam, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    initLocationDetails(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initLocationDetails(JSONObject location) throws JSONException {
        Log.e("json location", location.toString());
        String label = (String) txtLocation.getText();
        String uriBegin = "geo:0,0?q=";
        String query = location.getString("street_address") + ", " +
                location.getString("city") + ", " + location.getString("state") + "(" + label + ")";
        String encodedQuery = Uri.encode(query);
        String uriString = uriBegin + encodedQuery;
        final Uri locationIntentUri = Uri.parse(uriString);
        Log.e("uri", locationIntentUri.toString());
        txtLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("location", "clicked");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, locationIntentUri);
                //mapIntent.setPackage("com.google.android.gms.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    Log.e(null, "no mapps");
                }
            }
        });

    }

    private void getComments() {
        ClientServerInterface.post("get_comments.php", eventIdParam, new JsonHttpResponseHandler() {
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
        ClientServerInterface.post("get_attendance.php", eventIdParam, new JsonHttpResponseHandler() {
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

        //If the user is already attending the event, the appropriate buttons are enabled or disabled
        if (isSignedIn()) {
            if (Arrays.asList(nameAttendees).contains(nameCurrentUser)) {
                cancelButton.setEnabled(true);
                attendButton.setEnabled(false);
                attendIcon.setVisibility(View.VISIBLE);
            } else {
                attendButton.setEnabled(true);
                cancelButton.setEnabled(false);
            }
        }

        attendeeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.e(null, "clicked");
                attendanceDialog();
            }
        });
    }

    private void addAttendance() {
        RequestParams requestParams = new RequestParams();
        requestParams.put("eventId", id);
        requestParams.put("userId", userId);

        ClientServerInterface.post("add_attendance.php", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                getAttendance();
                Toast.makeText(getApplicationContext(), "You are now attending", Toast.LENGTH_SHORT).show();
            }
        });

        Log.e(null, requestParams.toString());
    }

    private void removeAttendance() {
        RequestParams requestParams = new RequestParams();
        requestParams.put("eventId", id);
        requestParams.put("userId", userId);

        ClientServerInterface.post("remove_attendance.php", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                getAttendance();
                Toast.makeText(getApplicationContext(), "You are no longer attending", Toast.LENGTH_SHORT).show();
            }
        });

        Log.e(null, requestParams.toString());
    }

    private void publishComment() {
        RequestParams requestParams = new RequestParams();
        requestParams.put("eventId", id);
        requestParams.put("userId", userId);
        requestParams.put("commentBody", commentBody);
        Log.e("Request Params", requestParams.toString());
        ClientServerInterface.post("publish_comment.php", requestParams, new JsonHttpResponseHandler());
    }

    private void getWeather() {
        ClientServerInterface.post("get_weather.php", eventIdParam, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                initWeather(response);
            }
        });
    }

    private void initWeather(JSONObject weatherDetails) {
        try {
            String summary = weatherDetails.getString("summary");
            int highTemperature = weatherDetails.getInt("temperatureMax");
            int lowTemperature = weatherDetails.getInt("temperatureMin");

            TextView description = (TextView) findViewById(R.id.descriptionWeather);
            description.setText(summary);

            TextView low = (TextView) findViewById(R.id.lowTemperature);
            low.setText(lowTemperature + "\u00B0" + "F");

            TextView high = (TextView) findViewById(R.id.highTemperature);
            high.setText(highTemperature + "\u00B0" + "F");

            ImageView weatherIcon = (ImageView) findViewById(R.id.weatherIcon);
            String icon = weatherDetails.getString("icon");
            icon = icon.replace('-', '_');
            int id = getResources().getIdentifier(icon, "drawable", getPackageName());
            weatherIcon.setImageDrawable(getResources().getDrawable(id));

            swipeRefreshLayout.setRefreshing(false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh() {
        getEventDetails();
        getLocationDetails();
        getComments();
        getAttendance();
        getWeather();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent.getExtras() != null) {
            id = Integer.parseInt(intent.getExtras().getString("Id"));
        }
    }
}