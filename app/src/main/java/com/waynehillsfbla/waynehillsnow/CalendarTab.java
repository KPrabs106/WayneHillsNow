package com.waynehillsfbla.waynehillsnow;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.tyczj.extendedcalendarview.CalendarProvider;
import com.tyczj.extendedcalendarview.Day;
import com.tyczj.extendedcalendarview.Event;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class CalendarTab extends Fragment {
    View v;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_calendar, container, false);

        getEvents();

        return v;
    }

    public void initCalendar(JSONArray jsonArray) {
        ExtendedCalendarView calendar = (ExtendedCalendarView) v.findViewById(R.id.calendar);
        getActivity().getContentResolver().delete(CalendarProvider.CONTENT_URI, null, null);

        ContentValues values = new ContentValues();

        final int[][] eventDates;

        eventDates = new int[jsonArray.length()][6];
        Calendar cal = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();

        String startDatetime = null;
        String endDatetime = null;
        String date;
        String time;
        String year;
        String month;
        String day;
        String hour;
        String minute;

        JSONObject jsonObject = null;
        //Add every event into the calendar
        //TODO Resolve issue with missing events
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                jsonObject = jsonArray.getJSONObject(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            /**Add starting date and time to the calendar**/
            try {
                startDatetime = jsonObject.getString("startDate");
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
                values.put(CalendarProvider.DESCRIPTION, jsonObject.getString("description"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                values.put(CalendarProvider.LOCATION, jsonObject.getString("location"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {
                values.put(CalendarProvider.EVENT, jsonObject.getString("title"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //Month needs to be subtracted by 1, because the Android calendar month starts at 0
            cal.set(Integer.parseInt(year), Integer.parseInt(month) - 1,
                    Integer.parseInt(day), Integer.parseInt(hour),
                    Integer.parseInt(minute));

            int StartDayJulian = Time.getJulianDay(cal.getTimeInMillis(),
                    TimeUnit.MILLISECONDS.toSeconds(tz.getOffset(cal.getTimeInMillis())));

            //Put the start date and time into calendar
            values.put(CalendarProvider.START, cal.getTimeInMillis());
            values.put(CalendarProvider.START_DAY, StartDayJulian);

            //Store the start date into an array
            eventDates[i][0] = Integer.parseInt(year);
            eventDates[i][1] = Integer.parseInt(month);
            eventDates[i][2] = Integer.parseInt(day);

            /**Add end date and time to the calendar**/
            try {
                endDatetime = jsonObject.getString("endDate");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            date = (endDatetime.split(" "))[0];
            time = (endDatetime.split(" "))[1];

            year = date.split("-")[0];
            month = date.split("-")[1];
            day = date.split("-")[2];

            hour = time.split(":")[0];
            minute = time.split(":")[1];

            cal.set(Integer.parseInt(year), Integer.parseInt(month) - 1,
                    Integer.parseInt(day), Integer.parseInt(hour),
                    Integer.parseInt(minute));
            int endDayJulian = Time.getJulianDay(cal.getTimeInMillis(),
                    TimeUnit.MILLISECONDS.toSeconds(tz.getOffset(cal.getTimeInMillis())));

            //Put the end date and time into calendar
            values.put(CalendarProvider.END, cal.getTimeInMillis());
            values.put(CalendarProvider.END_DAY, endDayJulian);

            //Store the end date into an array
            eventDates[i][3] = Integer.parseInt(year);
            eventDates[i][4] = Integer.parseInt(month);
            eventDates[i][5] = Integer.parseInt(day);

            getActivity().getContentResolver().insert(CalendarProvider.CONTENT_URI, values);
        }

        calendar.refreshCalendar();

        //Check if the user clicked a day that has an event
        calendar.setOnDayClickListener(new ExtendedCalendarView.OnDayClickListener() {
            @Override
            public void onDayClicked(AdapterView<?> adapter, View view, int position, long id, Day day) {
                for (int[] eventDate : eventDates) {
                    /**We have the start and end dates already in an array.
                     * We can check to see if the date the user clicked on
                     * is between the start and end date of any event.
                     */
                    if ((eventDate[0] <= day.getYear() && day.getYear() <= eventDate[3]) &&
                            (eventDate[1] <= day.getMonth() + 1 && day.getMonth() + 1 <= eventDate[4]) &&
                            (eventDate[2] <= day.getDay() && day.getDay() <= eventDate[5])) {
                        Bundle bund = new Bundle();
                        bund.putInt("year", eventDate[0]);
                        bund.putInt("month", eventDate[1]);
                        bund.putInt("day", eventDate[2]);
                        Intent intent = new Intent(view.getContext(), ListEventActivity.class);
                        intent.putExtras(bund);

                        //Start the list event activity, and give it the selected year, month, and day
                        startActivity(intent);
                    }
                }
            }
        });
    }

    private void getEvents() {
        ClientServerInterface.get("get_events.php", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                initCalendar(response);
            }
        });
    }
}