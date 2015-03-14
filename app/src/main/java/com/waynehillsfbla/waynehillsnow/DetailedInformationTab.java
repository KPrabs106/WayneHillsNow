package com.waynehillsfbla.waynehillsnow;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by Sudharshan on 3/13/2015.
 */
public class DetailedInformationTab extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.detailed_event_tab, container, false);

        Intent intent = getActivity().getIntent();
        Bundle extras = intent.getExtras();

        int id = extras.getInt("Id");
        String title = extras.getString("Title");
        String type = extras.getString("Type");
        String location = extras.getString("Location");
        String description = extras.getString("Description");
        String contact = extras.getString("Contact");
        String startDate = extras.getString("StartDate");
        String endDate = extras.getString("EndDate");

        TextView txtTitle = (TextView) v.findViewById(R.id.txtTitle);
        txtTitle.setText(title);

        TextView txtType = (TextView) v.findViewById(R.id.txtType);
        txtType.setText(type);

        TextView txtLocation = (TextView) v.findViewById(R.id.txtLocation);
        txtLocation.setText(location);

        TextView txtDescription = (TextView) v.findViewById(R.id.txtDesc);
        txtDescription.setText(description);

        TextView txtContact = (TextView) v.findViewById(R.id.txtContact);
        txtContact.setText(contact);

        TextView txtStartDate = (TextView) v.findViewById(R.id.txtStartDate);
        try {
            txtStartDate.setText(getDetailedDisplayDate(startDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        TextView txtEndDate = (TextView) v.findViewById(R.id.txtEndDate);
        try {
            txtEndDate.setText(getDetailedDisplayDate(endDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return v;
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
}
