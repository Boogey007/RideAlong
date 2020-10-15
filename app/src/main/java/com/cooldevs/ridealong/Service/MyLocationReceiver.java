package com.cooldevs.ridealong.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import io.paperdb.Paper;
import com.cooldevs.ridealong.Utils.Commonx;

public class MyLocationReceiver extends BroadcastReceiver {

    public static final String ACTION = "com.cooldevs.ridealong.UPDATE_LOCATION";

    DatabaseReference publicLocation;

    String uid;

    public MyLocationReceiver() {
        publicLocation = FirebaseDatabase.getInstance().getReference(Commonx.PUBLIC_LOCATION);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Paper.init(context);

        uid = Paper.book().read(Commonx.USER_UID_SAVE_KEY);

        if (intent != null) {

            final String action = intent.getAction();

            if (action.equals(ACTION)) {

                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {

                    Location location = result.getLastLocation();
                    Log.d("PotatoSoup", "onReceive: location" + String.valueOf(location));
                    if (Commonx.loggedUser != null)
                        publicLocation.child(Commonx.loggedUser.getUid()).setValue(location);
                    else {
                        publicLocation.child(uid).setValue(location);
                    }
                }
            }
        }
    }

}