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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Kartik on 1/17/2015.
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<EventInfo> eventList;

    public EventAdapter(List<EventInfo> eventList) {
        this.eventList = eventList;
    }

    public int getItemCount() {
        return eventList.size();
    }

    private String getDisplayDate(String date) throws ParseException {
        String day, month, year, result;

        SimpleDateFormat simpForm = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat dispForm = new SimpleDateFormat("EEEE, MMMM dd yyyy");

        day = date.substring(8, 10);
        month = date.substring(5, 7);
        year = date.substring(0, 4);
        result = month + "/" + day + "/" + year;

        result = dispForm.format(simpForm.parse(result));

        return result;
    }

    public void onBindViewHolder(final EventViewHolder eventViewHolder, final int i) {
        eventViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bund = new Bundle();
                bund.putInt("Id", eventList.get(i).id);
                bund.putString("Title", eventList.get(i).title);
                bund.putString("Type", eventList.get(i).type);
                bund.putString("Location", eventList.get(i).location);
                bund.putString("Description", eventList.get(i).description);
                bund.putString("Contact", eventList.get(i).contact);
                bund.putString("StartDate", eventList.get(i).startDatetime);
                bund.putString("EndDate", eventList.get(i).endDatetime);
                Intent intent = new Intent(v.getContext(), DetailedEventActivity.class);
                intent.putExtras(bund);
                v.getContext().startActivity(intent);
            }
        });
        EventInfo ei = eventList.get(i);
        eventViewHolder.vTitle.setText(ei.title);
        try {
            eventViewHolder.vDate.setText(getDisplayDate(ei.startDatetime));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Picasso.with(eventViewHolder.context).load(ei.pictureURL).into(eventViewHolder.vPicture, new Callback() {
            @Override
            public void onSuccess() {
                eventViewHolder.vProgressBar.setVisibility(View.INVISIBLE);
                eventViewHolder.vPicture.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError() {
                eventViewHolder.vProgressBar.setVisibility(View.VISIBLE);
                eventViewHolder.vProgressBar.setVisibility(View.INVISIBLE);
            }
        });
        eventViewHolder.vType.setText(ei.type);
    }

    public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);
        return new EventViewHolder(itemView);
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        protected TextView vTitle;
        protected TextView vDate;
        protected ImageView vPicture;
        protected TextView vType;
        protected ProgressBar vProgressBar;
        protected Context context;

        public EventViewHolder(View v) {
            super(v);
            vTitle = (TextView) v.findViewById(R.id.txtTitle);
            vDate = (TextView) v.findViewById(R.id.txtDate);
            vPicture = (ImageView) v.findViewById(R.id.picture);
            vType = (TextView) v.findViewById(R.id.txtType);
            vProgressBar = (ProgressBar) v.findViewById(R.id.imgLoad);
            context = v.getContext();
        }
    }
}
