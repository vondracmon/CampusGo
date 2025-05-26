package com.example.campusgo;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

public class OvertimeNotificationReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "OvertimeNotificationChannel";
    private static final int NOTIF_ID = 101;

    @Override
    public void onReceive(Context context, Intent intent) {
        String room = intent.getStringExtra("room");

        Intent activityIntent = new Intent(context, FacultyRoomAvailable.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.campusgo_logo_1)
                .setContentTitle("Room Overtime Alert")
                .setContentText("Room " + room + " has exceeded scheduled time.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIF_ID + room.hashCode(), builder.build());
    }
}
