package com.waynehillsfbla.waynehillsnow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
    AlertDialog.Builder deleteDialog;
    private String nameCurrentUser;

    public CommentsListAdapter(Activity activity, String[] pictures, String[] names, String[] comments) {
        super(activity, R.layout.comments_list, pictures);
        this.activity = activity;
        this.pictures = pictures;
        this.comments = comments;
        this.names = names;
    }

    //Set profile pictures of attendees from String[] of picture URLs
    public View getView(final int position, View view, ViewGroup parent) {
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

        rowView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(isSignedIn() && canDelete(position))
                    showDeleteDialog(activity);

                return true;
            }
        });

        return rowView;
    }

    private boolean isSignedIn() {
        return activity.getSharedPreferences("userDetails", Context.MODE_PRIVATE).contains("displayName");
    }

    private void initGooglePlus() {
        SharedPreferences userDetails = activity.getSharedPreferences("userDetails", Context.MODE_PRIVATE);
        nameCurrentUser = userDetails.getString("displayName", null);
    }

    private boolean canDelete(int position) {
        if(isSignedIn())
            initGooglePlus();
        //TODO use google ID to compare instead of name
        return nameCurrentUser.equals(names[position]);
    }

    private void showDeleteDialog(Activity activity) {
        deleteDialog = new AlertDialog.Builder(activity.getWindow().getContext(),5);
        deleteDialog.setTitle("Delete comment?");

        // Set up the buttons
        deleteDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteComment();
                dialog.dismiss();
            }
        });
        deleteDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        deleteDialog.show();
    }

    private void deleteComment() {
        //TODO finish method to delete method
    }

}
