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

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
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
    private final String[] googleIds;
    AlertDialog.Builder deleteDialog;

    public CommentsListAdapter(Activity activity, String[] pictures, String[] names, String[] comments, String[] googleIds) {
        super(activity, R.layout.comments_list, pictures);
        this.activity = activity;
        this.pictures = pictures;
        this.comments = comments;
        this.names = names;
        this.googleIds = googleIds;
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
                if (canDelete(position))
                    showDeleteDialog(activity, position);
                return true;
            }
        });

        return rowView;
    }

    private boolean isSignedIn() {
        return activity.getSharedPreferences("userDetails", Context.MODE_PRIVATE).contains("displayName");
    }

    private boolean canDelete(int position) {
        if (isSignedIn()) {
            SharedPreferences userDetails = activity.getSharedPreferences("userDetails", Context.MODE_PRIVATE);
            String idCurrentUser = userDetails.getString("googleId", null);
            return idCurrentUser.equals(googleIds[position]);
        } else {
            return false;
        }
    }

    private void showDeleteDialog(Activity activity, final int position) {
        deleteDialog = new AlertDialog.Builder(activity.getWindow().getContext(), 5);
        deleteDialog.setTitle("Delete comment?");

        // Set up the buttons
        deleteDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteComment(position);
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

    private void deleteComment(int position) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("commenterGoogleId", googleIds[position]);
        requestParams.put("commentBody", comments[position]);
        ClientServerInterface.post("delete_comment.php", requestParams, new JsonHttpResponseHandler());
        activity.recreate();
    }

}
