package com.waynehillsfbla.waynehillsnow;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

/**
 * Created by Kartik on 3/15/2015.
 */
public class CommentsListAdapter extends ArrayAdapter<String> {

    private final Activity activity;
    private final String[] pictures;
    private final String[] names;
    private final String[] comments;

    public CommentsListAdapter(Activity activity, String[] pictures, String[] names, String[] comments) {
        super(activity, R.layout.comments_list, pictures);
        this.activity = activity;
        this.pictures = pictures;
        this.comments = comments;
        this.names = names;
    }

    //Set profile pictures of attendees from String[] of picture URLs
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.comments_list, null, true);

        TextView commenterName = (TextView) rowView.findViewById(R.id.commenterName);
        commenterName.setText(names[position]);

        TextView commentBody = (TextView) rowView.findViewById(R.id.commentBody);
        commentBody.setText(comments[position]);

        final ImageView profilePic = (ImageView) rowView.findViewById(R.id.commenterPicture);

        Picasso.with(activity.getBaseContext()).load(pictures[position]).into(profilePic, new Callback() {
            @Override
            public void onSuccess() {
                profilePic.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError() {
                profilePic.setVisibility(View.INVISIBLE);
            }
        });

        return rowView;
    }

}
