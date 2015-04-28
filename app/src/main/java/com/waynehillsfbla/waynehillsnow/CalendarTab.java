package com.waynehillsfbla.waynehillsnow;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateChangedListener;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CalendarTab extends Fragment implements BaseSliderView.OnSliderClickListener, OnDateChangedListener {
    View v;
    List<CalendarDay> events;
    SliderLayout imageScroller;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.activity_calendar, container, false);
        imageScroller = (SliderLayout) v.findViewById(R.id.slider);
        getEvents();

        return v;
    }

    public void initCalendar(JSONArray jsonArray) {
        MaterialCalendarView calendarView = (MaterialCalendarView) v.findViewById(R.id.calendarView);
        calendarView.setOnDateChangedListener(this);

        JSONObject jsonObject;
        String date;
        String[] dateComponents;
        CalendarDay eventDay;
        events = new ArrayList<CalendarDay>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                jsonObject = jsonArray.getJSONObject(i);
                date = jsonObject.getString("startDate").split(" ")[0];
                dateComponents = date.split("-");
                eventDay = new CalendarDay(Integer.parseInt(dateComponents[0]), Integer.parseInt(dateComponents[1]) - 1, Integer.parseInt(dateComponents[2]));
                events.add(eventDay);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        CalendarDay minDay = events.get(0);
        calendarView.setMinimumDate(new CalendarDay(minDay.getYear(), minDay.getMonth(), 1));
        CalendarDay maxDay = events.get(events.size() - 1);
        calendarView.setMaximumDate(new CalendarDay(maxDay.getYear(), maxDay.getMonth(), 31));

        Log.e("events", events.toString());

        calendarView.setSelectedDate(calendarView.getMaximumDate());
        calendarView.setSelectedDate(calendarView.getMinimumDate());
        calendarView.setSelectedDate(Calendar.getInstance().getTime());

        calendarView.addDecorator(new HighlightDecorator(events));

        calendarView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.e(null, "touched " + v.toString());

            }
        });


        getPictures(calendarView.getSelectedDate().getMonth() + 1);
        //calendarView.setSelectedDate(new CalendarDay(2016, Calendar.JUNE, 10));
        //calendarView.setSelectedDate(new CalendarDay(Calendar.getInstance().getTime().getYear(), Calendar.getInstance().getTime().getMonth()));

/*
        calendarView.addDecorator(new DayViewDecorator() {
            @Override
            public boolean shouldDecorate(CalendarDay calendarDay) {
                return events.contains(calendarDay);
            }

            @Override
            public void decorate(DayViewFacade dayViewFacade) {
                Log.e(null, "decorating");
                Drawable drawable = getResources().getDrawable(R.drawable.date_with_event);
                drawable.setAlpha(225);
                dayViewFacade.setBackgroundUnselected(drawable);
                //dayViewFacade.setBackgroundUnselected(getResources().getDrawable(R.drawable.date_with_event));
                //dayViewFacade.setBackgroundUnselected(new ColorDrawable(0x660033));
                //dayViewFacade.setBackgroundUnselected(getResources().getDrawable(R.drawable.sunny));
                //dayViewFacade.setBackground(new ColorDrawable(0x660033));
            }
        });
*/
        //calendarView.invalidateDecorators();
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

    @Override
    public void onDateChanged(MaterialCalendarView materialCalendarView, CalendarDay calendarDay) {
        Log.e(null, "cahnged");
        if (events.contains(calendarDay)) {
            Bundle bundle = new Bundle();
            bundle.putInt("year", calendarDay.getYear());
            bundle.putInt("month", calendarDay.getMonth() + 1);
            bundle.putInt("day", calendarDay.getDay());
            Intent intent = new Intent(v.getContext(), ListEventActivity.class);
            intent.putExtras(bundle);

            startActivity(intent);
        }
    }

    private void getPictures(int month) {
        RequestParams requestParams = new RequestParams("month", month);
        ClientServerInterface.post("get_pictures_by_month.php", requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                initPictures(response);
            }
        });
    }

    private void initPictures(JSONArray pictures) {
        JSONObject jsonObject;
        Bundle bundle;
        for (int i = 0; i < pictures.length(); i++) {
            TextSliderView textSliderView = new TextSliderView(v.getContext());
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
}