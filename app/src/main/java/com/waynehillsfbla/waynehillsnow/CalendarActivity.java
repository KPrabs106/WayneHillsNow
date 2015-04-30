package com.waynehillsfbla.waynehillsnow;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class CalendarActivity extends AppCompatActivity implements BaseSliderView.OnSliderClickListener {

    SliderLayout imageScroller;
    CaldroidFragment caldroidFragment;
    List<Date> eventDates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        imageScroller = (SliderLayout) findViewById(R.id.slider);
        getEvents();

        caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, false);
        caldroidFragment.setArguments(args);

        android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendarView, caldroidFragment);
        t.commit();

        getPictures(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR));
    }

    private void initCalendar(JSONArray eventDetails) {
        eventDates = new ArrayList<Date>();
        JSONObject jsonObject;
        String date;
        String[] dateComponents;
        Calendar cal = Calendar.getInstance();
        Date startDate;
        Date endDate;
        for (int i = 0; i < eventDetails.length(); i++) {
            try {
                jsonObject = eventDetails.getJSONObject(i);

                date = jsonObject.getString("startDate").split(" ")[0];
                dateComponents = date.split("-");
                cal.set(Calendar.YEAR, Integer.parseInt(dateComponents[0]));
                cal.set(Calendar.MONTH, Integer.parseInt(dateComponents[1]) - 1);
                cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateComponents[2]));
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                startDate = cal.getTime();

                date = jsonObject.getString("endDate").split(" ")[0];
                dateComponents = date.split("-");
                cal.set(Calendar.YEAR, Integer.parseInt(dateComponents[0]));
                cal.set(Calendar.MONTH, Integer.parseInt(dateComponents[1]) - 1);
                cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dateComponents[2]));
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                endDate = cal.getTime();

                eventDates.addAll(datesBetween(startDate, endDate));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        for (Date eventDate : eventDates) {
            caldroidFragment.setBackgroundResourceForDate(R.color.caldroid_light_red, eventDate);
        }

        caldroidFragment.refreshView();

        final CaldroidListener listener = new CaldroidListener() {
            @Override
            public void onSelectDate(Date date, View view) {
                Log.e(null, date.toString());
                if (eventDates.contains(date)) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(date);

                    Bundle bundle = new Bundle();
                    bundle.putInt("year", calendar.get(Calendar.YEAR));
                    bundle.putInt("month", calendar.get(Calendar.MONTH) + 1);
                    bundle.putInt("day", calendar.get(Calendar.DAY_OF_MONTH));
                    Intent intent = new Intent(view.getContext(), ListEventActivity.class);
                    intent.putExtras(bundle);

                    startActivity(intent);
                }
            }

            public void onChangeMonth(int month, int year) {
                getPictures(month, year);
            }
        };

        caldroidFragment.setCaldroidListener(listener);
    }

    private List<Date> datesBetween(Date startDate, Date endDate) {
        List<Date> dateList = new ArrayList<Date>();
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(startDate);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        while (!calendar.getTime().after(endDate)) {
            Date result = calendar.getTime();
            dateList.add(result);
            calendar.add(Calendar.DATE, 1);
        }

        return dateList;
    }

    private void getEvents() {
        ClientServerInterface.get("get_all_events.php", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                initCalendar(response);
            }
        });
    }

    @Override
    public void onSliderClick(BaseSliderView baseSliderView) {
        Bundle bundle = baseSliderView.getBundle();
        Intent intent = new Intent(baseSliderView.getContext(), DetailedEventActivity.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void getPictures(int month, int year) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("month", month);
        requestParams.put("year", year);
        ClientServerInterface.post("get_pictures_by_month.php", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                initPictures(response);
            }
        });
    }

    private void initPictures(JSONArray pictures) {
        imageScroller.removeAllSliders();

        JSONObject jsonObject;
        Bundle bundle;
        for (int i = 0; i < pictures.length(); i++) {
            TextSliderView textSliderView = new TextSliderView(getApplicationContext());
            bundle = textSliderView.getBundle();

            try {
                jsonObject = pictures.getJSONObject(i);
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

        return super.onOptionsItemSelected(item);
    }
}
