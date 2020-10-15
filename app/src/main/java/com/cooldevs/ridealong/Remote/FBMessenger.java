package com.cooldevs.ridealong.Remote;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import com.cooldevs.ridealong.Model.MyResponse;
import com.cooldevs.ridealong.Model.Request;

public interface FBMessenger {

    @Headers({

            "Content-Type:application/json",
            "Authorization:key=AAAAWHQJT6E:APA91bEJo7as36sFHnnQ_i3-AA-DAy9lSajY63YgOxlNsnLSM5DzN7-ReaiAdHwKbeWej2Pxz9fGij1b3CuOz7l7oMOpVdhiPjaBxL1Clz5zSfcr7VDTC2m0wP6QNXEJGsrRq6X6C-fV"

    })

    @POST("fcm/send")
    Observable<MyResponse> sendFriendRequestToUser(@Body Request body);
}