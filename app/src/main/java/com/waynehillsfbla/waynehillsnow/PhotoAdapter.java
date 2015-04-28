package com.waynehillsfbla.waynehillsnow;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Kartik on 4/21/2015.
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private List<PhotoInfo> photoInfoList;

    public PhotoAdapter(List<PhotoInfo> photoInfoList) {
        this.photoInfoList = photoInfoList;
    }

    public int getItemCount() {
        return photoInfoList.size();
    }

    public PhotoViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.photo_card_layout, viewGroup, false);
        return new PhotoViewHolder(itemView);
    }

    public void onBindViewHolder(final PhotoViewHolder photoViewHolder, final int i) {
        photoViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("pictureURL", photoInfoList.get(i).pictureURL);
                bundle.putString("eventName", photoInfoList.get(i).eventTitle);
                Intent intent = new Intent(v.getContext(), ViewEventImage.class);
                intent.putExtras(bundle);
                v.getContext().startActivity(intent);
            }
        });

        PhotoInfo photoInfo = photoInfoList.get(i);
        photoViewHolder.vEventTitle.setText(photoInfo.eventTitle);
        photoViewHolder.vSubmitter.setText(photoInfo.submitterName);

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
