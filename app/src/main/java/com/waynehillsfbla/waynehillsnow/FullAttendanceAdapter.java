package com.waynehillsfbla.waynehillsnow;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Arrays;

/**
 * Created by Kartik on 4/10/2015.
 */
public class FullAttendanceAdapter extends ArrayAdapter<String> {
    private final Activity activity;
    private final String[] names;
    private final String[] profilePictures;

    public FullAttendanceAdapter(Activity activity, String[] names, String[] profilePictures) {
        super(activity, R.layout.full_attendance_list);
        Log.e(null, "constuctor");
        this.activity = activity;
        this.names = names;
        this.profilePictures = profilePictures;
        Log.e(null, Arrays.toString(names));
        Log.e(null, Arrays.toString(profilePictures));
    }

    public View getView(int position, View view, ViewGroup parent) {
        Log.e("getView", "" + position);
        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.full_attendance_list, null, true);

        final ImageView profilePicture = (ImageView) rowView.findViewById(R.id.profilePicture);
        final TextView name = (TextView) rowView.findViewById(R.id.displayName);

        Picasso.with(activity.getApplicationContext()).load(profilePictures[position]).into(profilePicture);
        name.setText(names[position]);

        return rowView;
    }

    @Override
    public int getCount() {
        return names.length;
    }
}