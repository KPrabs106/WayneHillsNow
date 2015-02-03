package com.waynehillsfbla.waynehillsnow;

import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;


import com.tyczj.extendedcalendarview.CalendarProvider;
import com.tyczj.extendedcalendarview.Day;
import com.tyczj.extendedcalendarview.Event;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class CalendarActivity extends ActionBarActivity {
    int[][] eventDates;
    JSONObject jobj = null;
    JSONArray jarr = null;
    ClientServerInterface clientServerInterface = new ClientServerInterface();
    String startDatetime = "";
    String endDatetime = "";
    String date = "";
    String time = "";
    String year = "";
    String month = "";
    String day = "";
    String hour = "";
    String minute = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        ExtendedCalendarView calendar = (ExtendedCalendarView) findViewById(R.id.calendar);
        getContentResolver().delete(CalendarProvider.CONTENT_URI, null, null);

        ContentValues values = new ContentValues();

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

        eventDates = new int[jarr.length()][6];
        Calendar cal = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();

        for(int i = 0; i < jarr.length(); i++){
            try {
                jobj = jarr.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                startDatetime = jobj.getString("startDate");
                endDatetime = jobj.getString("endDate");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            date = (startDatetime.split(" "))[0];
            time = (startDatetime.split(" "))[1];

            year = date.split("-")[0];
            month = date.split("-")[1];
            day = date.split("-")[2];

            hour = time.split(":")[0];
            minute = time.split(":")[1];

            values.put(CalendarProvider.COLOR, Event.COLOR_RED);
            try {
                values.put(CalendarProvider.DESCRIPTION, jobj.getString("description"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                values.put(CalendarProvider.LOCATION, jobj.getString("location"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                values.put(CalendarProvider.EVENT, jobj.getString("title"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            cal.set(Integer.parseInt(year), Integer.parseInt(month)-1,
                    Integer.parseInt(day), Integer.parseInt(hour),
                    Integer.parseInt(minute));

            int StartDayJulian = Time.getJulianDay(cal.getTimeInMillis(),
                    TimeUnit.MILLISECONDS.toSeconds(tz.getOffset(cal.getTimeInMillis())));
            values.put(CalendarProvider.START, cal.getTimeInMillis());
            values.put(CalendarProvider.START_DAY, StartDayJulian);

            eventDates[i][0] = Integer.parseInt(year);
            eventDates[i][1] = Integer.parseInt(month);
            eventDates[i][2] = Integer.parseInt(day);

            date = (endDatetime.split(" "))[0];
            time = (endDatetime.split(" "))[1];

            year = date.split("-")[0];
            month = date.split("-")[1];
            day = date.split("-")[2];

            hour = time.split(":")[0];
            minute = time.split(":")[1];

            cal.set(Integer.parseInt(year), Integer.parseInt(month)-1,
                    Integer.parseInt(day), Integer.parseInt(hour),
                    Integer.parseInt(minute));
            int endDayJulian = Time.getJulianDay(cal.getTimeInMillis(),
                    TimeUnit.MILLISECONDS.toSeconds(tz.getOffset(cal.getTimeInMillis())));
            values.put(CalendarProvider.END, cal.getTimeInMillis());
            values.put(CalendarProvider.END_DAY, endDayJulian);

            eventDates[i][3] = Integer.parseInt(year);
            eventDates[i][4] = Integer.parseInt(month);
            eventDates[i][5] = Integer.parseInt(day);

            getContentResolver().insert(CalendarProvider.CONTENT_URI, values);
        }

        calendar.refreshCalendar();

        calendar.setOnDayClickListener( new ExtendedCalendarView.OnDayClickListener() {
            @Override
            public void onDayClicked(AdapterView<?> adapter, View view, int position, long id, Day day) {
                for(int i = 0; i < eventDates.length; i++){
                    if( ( eventDates[i][0] <= day.getYear() && day.getYear() <= eventDates[i][3] ) &&
                            ( eventDates[i][1] <= day.getMonth()+1 && day.getMonth()+1 <= eventDates[i][4] ) &&
                            ( eventDates[i][2] <= day.getDay() && day.getDay() <= eventDates[i][5]) ){
                        Bundle bund = new Bundle();
                        bund.putInt("year", eventDates[i][0]);
                        bund.putInt("month", eventDates[i][1]);
                        bund.putInt("day", eventDates[i][2]);
                        Intent intent = new Intent(view.getContext(), ListEventActivity.class);
                        intent.putExtras(bund);
                        startActivity(intent);
                    }
                }
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_calendar, menu);
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

    class RetrieveData extends AsyncTask<String,String,JSONArray>
    {
        protected JSONArray doInBackground(String... arg0) {
            jarr = clientServerInterface.makeHttpRequest("http://54.164.136.46/printresult.php");
            return jarr;
        }
    }
}
