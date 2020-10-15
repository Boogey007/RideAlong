package com.cooldevs.ridealong.Utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.cooldevs.ridealong.FriendRequestActivity;

import com.cooldevs.ridealong.R;

public class NotificationHelper extends ContextWrapper {

    private static final String XYZ_CHANNEL_ID="com.cooldevs.ridealong";
    private static final String XYZ_CHANNEL_NAME="RideAlong";

    private NotificationManager manager;

    public NotificationHelper(Context base) {
        super(base);

        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O)
            createChannel();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createChannel() {

        NotificationChannel xyzchannel = new NotificationChannel(XYZ_CHANNEL_ID,XYZ_CHANNEL_NAME,NotificationManager.IMPORTANCE_DEFAULT);

        xyzchannel.enableLights(true);
        xyzchannel.enableVibration(true);
        xyzchannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(xyzchannel);


    }

    public NotificationManager getManager(){

        if(manager==null){

            manager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        }

        return manager;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getLocationTrackerNotification(String title, String content, Uri defaultsound) {
        Intent intent = new Intent(this, FriendRequestActivity.class);

        PendingIntent pendingIntent = TaskStackBuilder.create(getApplicationContext())
                .addNextIntent(intent)
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        return new Notification.Builder(getApplicationContext(),XYZ_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(content)
                .setSound(defaultsound)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

    }
}
