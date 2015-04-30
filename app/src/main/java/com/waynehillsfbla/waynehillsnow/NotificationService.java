package com.waynehillsfbla.waynehillsnow;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * Created by Kartik on 3/20/2015.
 */
public class NotificationService extends Service {

    private NotificationManager notificationManager;
    private PendingIntent detailedEventActivityIntent;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Bundle eventDetails = intent.getExtras();
        final String eventName = eventDetails.getString("eventName");
        final String location = eventDetails.getString("location");
        final String startDate = eventDetails.getString("startDate");
        final String pictureURL = eventDetails.getString("pictureURL");
        final long notificationTime = eventDetails.getLong("notificationTimeInMillis");

        Bundle bundle = new Bundle();
        bundle.putInt("Id", eventDetails.getInt("id"));

        Intent detailedViewIntent = new Intent(getApplicationContext(), DetailedEventActivity.class);
        detailedViewIntent.putExtras(bundle);
        detailedViewIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        detailedEventActivityIntent = PendingIntent.getActivity(getApplicationContext(), 0, detailedViewIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        Picasso.with(getApplicationContext()).load(pictureURL).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                Notification notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle(eventName)
                        .setContentText("on " + startDate + " at " + location)
                        .setSmallIcon(R.drawable.ic_notify)
                        .setLargeIcon(bitmap)
                        .setContentIntent(detailedEventActivityIntent)
                        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                        .build();

                SharedPreferences userDetails = getSharedPreferences("notifications", MODE_PRIVATE);
                SharedPreferences.Editor editor = userDetails.edit();
                editor.remove(String.valueOf(notificationTime));
                editor.apply();

                notificationManager.notify(1, notification);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Notification notification = new Notification.Builder(getApplicationContext())
                        .setContentTitle(eventName)
                        .setContentText("on " + startDate + " at " + location)
                        .setSmallIcon(R.drawable.ic_notify)
                        .setLargeIcon(((BitmapDrawable) errorDrawable).getBitmap())
                        .setContentIntent(detailedEventActivityIntent)
                        .build();

                SharedPreferences userDetails = getSharedPreferences("notifications", MODE_PRIVATE);
                SharedPreferences.Editor editor = userDetails.edit();
                editor.remove(String.valueOf(notificationTime));
                editor.apply();

                notificationManager.notify(1, notification);
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
        return Service.START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
    }
}
