package com.cooldevs.ridealong.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.Random;

import com.cooldevs.ridealong.Model.User;
import com.cooldevs.ridealong.R;
import com.cooldevs.ridealong.Utils.Commonx;
import com.cooldevs.ridealong.Utils.NotificationHelper;

public class MyFCMService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData() != null) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                sendNotificationWithChannel(remoteMessage);

            else
                sendNotification(remoteMessage);
        }

    }

  // build friend requesst here
    private void addRequestToUserInformation(Map < String, String > data) {

        DatabaseReference fr = FirebaseDatabase.getInstance().getReference(Commonx.USER_INFORMATION).child(data.get(Commonx.TO_UID)).child(Commonx.FRIEND_REQUEST);
        User userx = new User();
        userx.setUid(data.get(Commonx.FROM_UID));
        userx.setEmail(data.get(Commonx.FROM_NAME));
        //userx.setPhone(data.get(Commonx.PhoneNumber));

        fr.child(userx.getUid()).setValue(userx);

    }

// attach that info to the notificatio nitself
// take bits from https://stackoverflow.com/questions/41009936/firebase-onmessagereceivedremotemessage-remotemessage-is-not-called-when-the
    private void sendNotification(RemoteMessage remoteMessage) {

        Map < String, String > data = remoteMessage.getData();
        String title = "Friend Requests";
        String content = "New friend request from " + data.get(Commonx.FROM_NAME);

        Uri defaultsound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(title)
            .setContentText(content)
            .setSound(defaultsound)
            .setAutoCancel(false);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(new Random().nextInt(), builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendNotificationWithChannel(RemoteMessage remoteMessage) {

        Map < String, String > data = remoteMessage.getData();
        String title = "Friend Request";
        String content = "New friend request from " + data.get(Commonx.FROM_NAME);


        NotificationHelper helper;
        Notification.Builder builder;

        Uri defaultsound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        helper = new NotificationHelper(this);
        builder = helper.getLocationTrackerNotification(title, content, defaultsound);

        helper.getManager().notify(new Random().nextInt(), builder.build());
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            final DatabaseReference tokens = FirebaseDatabase.getInstance()
                .getReference(Commonx.TOKENS);
            tokens.child(user.getUid()).setValue(s);
        }
    }
}