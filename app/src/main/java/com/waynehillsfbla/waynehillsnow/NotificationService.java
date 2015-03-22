package com.waynehillsfbla.waynehillsnow;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Kartik on 3/20/2015.
 */
public class NotificationService extends Service {

    private NotificationManager notificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        notificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        Intent detailedView = new Intent(getApplicationContext(), DetailedEventActivity.class);

        Notification notification = new Notification(R.drawable.icon, "Notifictaion", System.currentTimeMillis());

        detailedView.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingNotificationIntent = PendingIntent.getActivity(getApplicationContext(), 0, detailedView, PendingIntent.FLAG_UPDATE_CURRENT);

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notification.setLatestEventInfo(getApplicationContext(), "Wayne Hills Now", "Event", pendingNotificationIntent);

        notificationManager.notify(0, notification);

        return Service.START_STICKY;
    }

    public void onDestroy() {
        super.onDestroy();
    }
}
