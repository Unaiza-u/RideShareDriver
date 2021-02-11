package com.example.ridesharedriver.Remote;

import com.example.ridesharedriver.Model.DataMessage;
import com.example.ridesharedriver.Model.FCMResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAcElKzzs:APA91bGVYvNH1rjzASmYRyfQi-99HeZMKP4-k138AKPRxAPugTivXxvfxH032k3LszPF__OtJvmX8UZl4pY7AMP5kgLpYqxE_WVkg69neZRDvXmm6LtMvkcDBonu2zlOSkL2urbfCbDW"
    })

    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body DataMessage body);
}
