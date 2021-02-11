package com.example.ridesharedriver.Common;

import android.location.Location;
import android.util.Log;

import com.example.ridesharedriver.Model.User;
import com.example.ridesharedriver.Remote.FCMClient;
import com.example.ridesharedriver.Remote.IFCMService;
import com.example.ridesharedriver.Remote.IGoogleAPI;
import com.example.ridesharedriver.Remote.RetrofitClient;

public class Common {
    public static final String driver_tbl = "Drivers";
    public static final String booked_driver_tbl = "BookedDrivers";
    public static final String user_driver_tbl = "DriversInformation";
    public static final String user_rider_tbl = "RidersInformation";
    public static final String pickup_request_tbl = "PickupRequest";
    public static final String token_tbl = "Tokens";

    public static User currentUser;

    public static Location mLastKnownLocation = null;

    public static final String baseURL = "https://maps.googleapis.com/";
    public static final String fcmURL = "https://fcm.googleapis.com/";
    public static final String user_field = "usr";
    public static final String pwd_field = "pwd";

    public static double base_fare = 30.55;
    public static double time_rate = 10.35;
    public static double distance_rate = 10.75;

    public static double getPrice(double km, int min){
        Log.d("LAPTOP", String.valueOf(km));
        Log.d("LAPTOP", String.valueOf(min));
        return (base_fare + ( time_rate * min) + (distance_rate * km));
    }

    public static IGoogleAPI getGoogleAPI(){
        return RetrofitClient.getClient(baseURL).create(IGoogleAPI.class);
    }

    public static IFCMService getFCMService(){
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }
}
