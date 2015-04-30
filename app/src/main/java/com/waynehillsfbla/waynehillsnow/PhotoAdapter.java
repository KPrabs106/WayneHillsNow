package com.waynehillsfbla.waynehillsnow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Kartik on 4/21/2015.
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    AlertDialog.Builder deleteDialog;
    Activity activity;
    PhotoInfo photoInfo;
    private List<PhotoInfo> photoInfoList;

    public PhotoAdapter(List<PhotoInfo> photoInfoList, Activity activity) {
        this.photoInfoList = photoInfoList;
        this.activity = activity;
    }

    public int getItemCount() {
        return photoInfoList.size();
    }

    public PhotoViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.photo_card_layout, viewGroup, false);
        return new PhotoViewHolder(itemView);
    }

    public void onBindViewHolder(final PhotoViewHolder photoViewHolder, final int position) {
        photoViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("pictureURL", photoInfoList.get(position).pictureURL);
                bundle.putString("eventName", photoInfoList.get(position).eventTitle);
                Intent intent = new Intent(v.getContext(), ViewImageActivity.class);
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        });

        photoInfo = photoInfoList.get(position);
        photoViewHolder.vEventTitle.setText(photoInfo.eventTitle);
        photoViewHolder.vSubmitter.setText(photoInfo.submitterName);

        photoViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (canDelete(position))
                    showDeleteDialog(activity, position);
                return true;
            }
        });

        Picasso.with(photoViewHolder.vContext).load(photoInfo.pictureURL).into(photoViewHolder.vPicture, new Callback() {
            @Override
            public void onSuccess() {
                photoViewHolder.vProgressBar.setVisibility(View.INVISIBLE);
                photoViewHolder.vPicture.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError() {
                photoViewHolder.vProgressBar.setVisibility(View.VISIBLE);
                photoViewHolder.vProgressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private boolean isSignedIn() {
        return activity.getSharedPreferences("userDetails", Context.MODE_PRIVATE).contains("displayName");
    }

    private boolean canDelete(int position) {
        if (isSignedIn()) {
            SharedPreferences userDetails = activity.getSharedPreferences("userDetails", Context.MODE_PRIVATE);
            String idCurrentUser = userDetails.getString("googleId", null);
            return idCurrentUser.equals(photoInfoList.get(position).submitterGoogleId);
        } else {
            return false;
        }
    }

    private void showDeleteDialog(Activity activity, final int position) {
        deleteDialog = new AlertDialog.Builder(activity.getWindow().getContext(),5);
        deleteDialog.setTitle("Remove from feed?");

        // Set up the buttons
        deleteDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteImage(position);
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

    private void deleteImage(int position) {
        RequestParams requestParams = new RequestParams();
        requestParams.put("pictureURL", photoInfoList.get(position).pictureURL);
        ClientServerInterface.post("delete_uploaded_pictures.php", requestParams, new JsonHttpResponseHandler());
        activity.recreate();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        protected TextView vEventTitle;
        protected TextView vSubmitter;
        protected ImageView vPicture;
        protected ProgressBar vProgressBar;
        protected Context vContext;

        public PhotoViewHolder(View v) {
            super(v);
            vEventTitle = (TextView) v.findViewById(R.id.txtTitle);
            vSubmitter = (TextView) v.findViewById(R.id.txtSubmitter);
            vPicture = (ImageView) v.findViewById(R.id.picture);
            vProgressBar = (ProgressBar) v.findViewById(R.id.imgLoad);
            vContext = v.getContext();
        }
    }
}
