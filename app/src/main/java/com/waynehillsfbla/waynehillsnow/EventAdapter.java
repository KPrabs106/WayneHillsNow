package com.waynehillsfbla.waynehillsnow;

import android.content.Context;
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

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * ****************************************************************
 * Given information about events, this adapter adds the information
 * to the cards.
 * ****************************************************************
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<EventInfo> eventList;
    private int x;

    public EventAdapter(List<EventInfo> eventList) {
        this.eventList = eventList;
    }

    public int getItemCount() {
        return eventList.size();
    }

    //TODO attend and notify icons
    //TODO notifications
    //TODO banner image scroller
    //TODO picstream type thing w user uploads
    //TODO Help page
    //TODO search

    //Puts the date into a more aesthetically pleasing format
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
        //If the user clicks on a card, they are then taken to the detailed view
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
                bund.putString("PictureURL", eventList.get(i).pictureURL);
                Intent intent = new Intent(v.getContext(), DetailedEventActivity.class);
                intent.putExtras(bund);
                v.getContext().startActivity(intent);
            }
        });

        eventViewHolder.vPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("pictureURL", eventList.get(i).pictureURL);
                bundle.putString("eventName", eventList.get(i).title);
                Intent intent = new Intent(v.getContext(), ViewImageActivity.class);
                intent.putExtras(bundle);
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

        x = 75;
        Picasso.with(eventViewHolder.context).load(R.drawable.ic_notify).resize(x,x).into(eventViewHolder.notifIcon);
        Picasso.with(eventViewHolder.context).load(R.drawable.ic_attend).resize(x,x).into(eventViewHolder.attendIcon);

        setupNotification(eventViewHolder, ei);
        setupAttendance(eventViewHolder, ei);
    }

    public EventViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);
        return new EventViewHolder(itemView);
    }

    private void setupNotification(final EventViewHolder eventViewHolder, EventInfo ei) {
        SharedPreferences notifications = eventViewHolder.context.getSharedPreferences("notifications", Context.MODE_PRIVATE);
        Map<String, ?> keys = notifications.getAll();
        for (Map.Entry<String, ?> entry : keys.entrySet()) {
            if (entry.getValue().equals(ei.id))
                eventViewHolder.notifIcon.setVisibility(View.VISIBLE);
        }
    }

    private void setupAttendance(final EventViewHolder eventViewHolder, EventInfo ei) {
        if (getGoogleId(eventViewHolder.context) != null) {
            RequestParams requestParams = new RequestParams();
            requestParams.put("eventId", ei.id);
            requestParams.put("userId", getGoogleId(eventViewHolder.context));
            ClientServerInterface.post("is_attending.php", requestParams, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        if (response.getBoolean("isAttending"))
                            eventViewHolder.attendIcon.setVisibility(View.VISIBLE);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private String getGoogleId(Context context) {
        if (context.getSharedPreferences("userDetails", Context.MODE_PRIVATE).contains("googleId")) {
            return context.getSharedPreferences("userDetails", Context.MODE_PRIVATE).getString("googleId", null);
        }
        return null;
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        protected TextView vTitle;
        protected TextView vDate;
        protected ImageView vPicture;
        protected ProgressBar vProgressBar;
        protected ImageView attendIcon;
        protected ImageView notifIcon;
        protected Context context;

        public EventViewHolder(View v) {
            super(v);
            vTitle = (TextView) v.findViewById(R.id.txtTitle);
            vDate = (TextView) v.findViewById(R.id.txtDate);
            vPicture = (ImageView) v.findViewById(R.id.picture);
            attendIcon = (ImageView) v.findViewById(R.id.attendIcon);
            notifIcon = (ImageView) v.findViewById(R.id.notifIcon);
            vProgressBar = (ProgressBar) v.findViewById(R.id.imgLoad);
            context = v.getContext();
        }
    }
}
