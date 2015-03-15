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

/**
 * Created by Kartik on 3/14/2015.
 */
public class CommentsListAdapter extends ArrayAdapter<String> {

    private final Activity activity;
    private final String[] names;
    private final String[] pictures;
    private final String[] comments;


    public CommentsListAdapter(Activity activity, String[] names, String[] pictures, String[] comments) {
        super(activity, R.layout.comments_list, names);
        this.activity = activity;
        this.names = names;
        this.pictures = pictures;
        this.comments = comments;
    }

    //Set names and profile pictures of attendees from String[] of names and picture URLs
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.comments_list, null, true);

        TextView txtName = (TextView) rowView.findViewById(R.id.attendeeName);
        txtName.setText(names[position]);

        TextView comment = (TextView) rowView.findViewById(R.id.commentBody);
        comment.setText(comments[position]);

        final ImageView profilePic = (ImageView) rowView.findViewById(R.id.attendeePicture);
        final ProgressBar progressBar = (ProgressBar) rowView.findViewById(R.id.progressBar);

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
