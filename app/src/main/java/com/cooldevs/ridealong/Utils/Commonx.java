package com.cooldevs.ridealong.Utils;

import com.cooldevs.ridealong.Remote.FBMessenger;
import com.cooldevs.ridealong.Remote.RetrofitClient;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;


import com.cooldevs.ridealong.Model.User;

// shout out to IIQ for the name suggestion lol
public class Commonx {

    public static final String USER_INFORMATION="UserInformation";
    public static final String USER_UID_SAVE_KEY = "SaveUid";
    public static final String TOKENS ="Tokens" ;
    public static final String FROM_NAME ="FromName" ;
    public static final String FROM_PHONE ="FromPhone" ;
    public static final String ACCEPT_LIST ="acceptList" ;
    public static final String FROM_UID = "FromUid";
    public static final String TO_UID ="ToUid" ;
    public static final String TO_NAME ="ToName" ;
    public static final String FRIEND_REQUEST = "FriendRequests";
    public static final String PUBLIC_LOCATION = "PublicLocation";
    public static final String Car = "Car";
    public static final String Email = "email";
    public static final String PhoneNumber = "phoneNumber";
    public static User loggedUser;
    public static User trackingUser;
    public static User userProfile;


    public static FBMessenger getFBService(){

        return RetrofitClient.getClient("https://fcm.googleapis.com/")
                .create(FBMessenger.class);
    }

    public static Date convertTimeStampToDate(long time){

        return new Date(new Timestamp(time).getTime());
    }

    public static String getDateFormatted(Date date){

        return  new SimpleDateFormat("dd-MM-yyyy hh:mm a").format(date).toString();
    }
}
