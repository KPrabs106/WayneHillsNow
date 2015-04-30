package com.waynehillsfbla.waynehillsnow;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * *************************************
 * Receives alarm set for notification  *
 * **************************************
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent notificationIntent = new Intent(context, NotificationService.class);
        notificationIntent.putExtras(intent.getExtras());
        context.startService(notificationIntent);
    }
}
