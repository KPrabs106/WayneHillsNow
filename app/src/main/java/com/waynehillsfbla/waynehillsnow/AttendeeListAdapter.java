package com.waynehillsfbla.waynehillsnow;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class AttendeeListAdapter extends ArrayAdapter<String> {

    private final Activity activity;
    private final String[] names;
    private final String[] pictures;


    public AttendeeListAdapter(Activity activity, String[] names, String[] pictures) {
        super(activity, R.layout.attendee_list, names);
        this.activity = activity;
        this.names = names;
        this.pictures = pictures;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.attendee_list, null, true);

        TextView txtName = (TextView) rowView.findViewById(R.id.attendeeName);
        final ImageView profilePic = (ImageView) rowView.findViewById(R.id.attendeePicture);
        final ProgressBar progressBar = (ProgressBar) rowView.findViewById(R.id.progressBar);

        txtName.setText(names[position]);
        Picasso.with(activity.getBaseContext()).load(pictures[position]).into(profilePic, new Callback() {
            @Override
            public void onSuccess() {
                profilePic.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError() {
                profilePic.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
            }
        });

        return rowView;
    }
}
