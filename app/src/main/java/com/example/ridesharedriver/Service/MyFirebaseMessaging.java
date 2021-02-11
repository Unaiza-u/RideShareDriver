package com.example.ridesharedriver.Service;

import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.ridesharedriver.CustomerCall;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class MyFirebaseMessaging extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

//        remoteMessage.getData();
//        //String sented = remoteMessage.getData().get("body");
//
//        Map<String, String> data = new HashMap<>();

        String title = remoteMessage.getData().get("title");
        final String message = remoteMessage.getData().get("message");
        String desti = remoteMessage.getData().get("desti");

        //because i will send the firebase message which contain lat and lng from Rider app
        //so i need convert message to LatLng
        LatLng customer_location = new Gson().fromJson(message, LatLng.class);

        LatLng destination_location = new Gson().fromJson(desti, LatLng.class);

        Log.d("NEW_TOKEN", String.valueOf(customer_location.latitude));
        Log.d("NEW_TOKEN", String.valueOf(customer_location.longitude));

        Intent intent = new Intent(this, CustomerCall.class);
        intent.putExtra("lat", customer_location.latitude);
        intent.putExtra("lng", customer_location.longitude);
        intent.putExtra("customer", title);

        intent.putExtra("desti_lat", destination_location.latitude);
        intent.putExtra("desti_lng", destination_location.longitude);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);


        //Toast.makeText(this, "yes we got message", Toast.LENGTH_LONG).show();
    }
}
