package com.example.ridesharedriver;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.ridesharedriver.Common.Common;
import com.example.ridesharedriver.Direction.DirectionsJSONParser;
import com.example.ridesharedriver.DirectionHelpers.FetchURL;
import com.example.ridesharedriver.DirectionHelpers.TaskLoadedCallback;
import com.example.ridesharedriver.Model.DataMessage;
import com.example.ridesharedriver.Model.FCMResponse;
import com.example.ridesharedriver.Model.Token;
import com.example.ridesharedriver.Remote.IFCMService;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DriverTracking extends FragmentActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private View mapView;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private LocationCallback locationCallback;
    private final float DEFAULT_ZOOM = 15;

    double riderLat, riderLng, desti_lat, desti_lng;

    //Direction
    private MarkerOptions place1, place2;
    private Polyline currentPolyline;

    List<MarkerOptions> markerOptionsList = new ArrayList<>();

    private Marker marker1, marker2;

    IFCMService mFCMService;

    GeoFire geoFire;

    String customerId;

    private DatabaseReference changeDriver;

    private Button btnStartTrip;

    private Location pickUpLocation;

    TextView pricePrimary, baseFare, timePrimary, distancePrimary, priceSecondary, startLocation, endLocation;

    LinearLayout detailLayout;

    boolean btnClicked = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_tracking);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

        mFCMService = Common.getFCMService();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(DriverTracking.this);

        if(getIntent() != null){
            riderLat = getIntent().getDoubleExtra("lat", -1.0);
            riderLng = getIntent().getDoubleExtra("lng", -1.0);

            desti_lat = getIntent().getDoubleExtra("desti_lat", -1.0);
            desti_lng = getIntent().getDoubleExtra("desti_lng", -1.0);

            customerId = getIntent().getStringExtra("customerId");


            Log.d("NEW_TOKEN1", String.valueOf(riderLat));
            Log.d("NEW_TOKEN1", String.valueOf(riderLng));

//            if(marker1 != null || marker2 != null){
//                marker1.remove();
//                marker2.remove();
//                markerOptionsList.clear();
//            }

//            if(currentPolyline != null)
//                currentPolyline.remove();



//            if(marker1 != null || marker2 != null) {
//                marker1.remove();
//                marker2.remove();
//                markerOptionsList.clear();
//
//                place1 = new MarkerOptions().position(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude())).title("Location 1");
//                markerOptionsList.add(place1);
//                marker1 = mMap.addMarker(place1);
//
//                place2 = new MarkerOptions().position(new LatLng(riderLat, riderLng)).title("Location 2");
//                markerOptionsList.add(place2);
//                marker2 = mMap.addMarker(place2);
//            }

//            showAllMarkers();
//            showPolyline();

        }

        changeDriver = FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        changeDriver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //if have any change from Driver table, we will reload aal driver available
                //loadAllAvaliableDriver();
                if(dataSnapshot.exists()) {
                    if(btnStartTrip.getText().equals("START TRIP")){

                        getDeviceLocation();
                    } else if(btnStartTrip.getText().equals("DROP OFF HERE")){

                        if(btnClicked){
                            changePointer();
                        }else{
                            calculateCashFee();
                        }

                    }
//                    Toast.makeText(DriverTracking.this, "test", Toast.LENGTH_SHORT).show();
//                    getDeviceLocation();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        btnStartTrip = findViewById(R.id.btnStartTrip);
        btnStartTrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnStartTrip.getText().equals("START TRIP")){

                    pickUpLocation = Common.mLastKnownLocation;
                    btnStartTrip.setText("DROP OFF HERE");
                } else if(btnStartTrip.getText().equals("DROP OFF HERE")){

                    btnClicked = true;
                    calculateCashFee();
                } else{
                    Toast.makeText(DriverTracking.this, "ERROR: Dropping", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //trip details
        detailLayout = findViewById(R.id.detailLayout);
        pricePrimary = findViewById(R.id.pricePrimary);
        baseFare = findViewById(R.id.baseFare);
        timePrimary = findViewById(R.id.timePrimary);
        distancePrimary = findViewById(R.id.distancePrimary);
        priceSecondary = findViewById(R.id.priceSecondary);
        startLocation = findViewById(R.id.startLocation);
        endLocation = findViewById(R.id.endLocation);


//        driverLocationChange = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
//        driverLocationChange.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                //if have any change from Driver table, we will reload aal driver available
//                //loadAllAvaliableDriver();
//                Toast.makeText(DriverTracking.this, "1", Toast.LENGTH_SHORT).show();
//
//                mMap.clear();
//                calculateCashFee();
//
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        updateFirebaseToken();
    }

    @SuppressLint("MissingPermission")
    private void changePointer() {

        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Common.mLastKnownLocation = task.getResult();
                            if (Common.mLastKnownLocation != null) {

//                                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), new GeoFire.CompletionListener() {
//                                    @Override
//                                    public void onComplete(String key, DatabaseError error) {
//                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
//
//                                    }
//                                });

                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                                if(marker1 != null || marker2 != null){
                                    marker1.remove();
                                    marker2.remove();
                                    markerOptionsList.clear();
                                }

                                place1 = new MarkerOptions().position(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude())).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                                markerOptionsList.add(place1);
                                marker1 = mMap.addMarker(place1);
//                                marker1 = mMap.addCircle(new CircleOptions()
//                                .center(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude()))
//                                .radius(50)
//                                .strokeColor(Color.BLUE)
//                                .fillColor(0x220000FF)
//                                .strokeWidth(5.0f));

                                place2 = new MarkerOptions().position(new LatLng(desti_lat, desti_lng)).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker));
                                markerOptionsList.add(place2);
                                marker2 = mMap.addMarker(place2);

                                showAllMarkers();
                                showPolyline();

                            } else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) {
                                            return;
                                        }
                                        Common.mLastKnownLocation = locationResult.getLastLocation();

//                                        geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), new GeoFire.CompletionListener() {
//                                            @Override
//                                            public void onComplete(String key, DatabaseError error) {
//                                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
//
//                                            }
//                                        });

                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                                        if(marker1 != null || marker2 != null){
                                            marker1.remove();
                                            marker2.remove();
                                            markerOptionsList.clear();
                                        }

                                        place1 = new MarkerOptions().position(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude())).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                                        markerOptionsList.add(place1);
                                        marker1 = mMap.addMarker(place1);

                                        place2 = new MarkerOptions().position(new LatLng(desti_lat, desti_lng)).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker));
                                        markerOptionsList.add(place2);
                                        marker2 = mMap.addMarker(place2);

                                        showAllMarkers();
                                        showPolyline();

                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                            }
                        } else {
                            Toast.makeText(DriverTracking.this, "unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @SuppressLint("MissingPermission")
    private void calculateCashFee() {

        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Common.mLastKnownLocation = task.getResult();
                            if (Common.mLastKnownLocation != null) {

//                                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), new GeoFire.CompletionListener() {
//                                    @Override
//                                    public void onComplete(String key, DatabaseError error) {
//                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
//
//                                    }
//                                });

                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                                if(marker1 != null || marker2 != null){
                                    marker1.remove();
                                    marker2.remove();
                                    markerOptionsList.clear();
                                }

                                if(place1 != null || place2 != null){
                                    place1 = null;
                                    place2 = null;
                                }

                                place1 = new MarkerOptions().position(new LatLng(riderLat, riderLng)).title("Pick Up Point").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                                markerOptionsList.add(place1);
                                marker1 = mMap.addMarker(place1);
//                                marker1 = mMap.addCircle(new CircleOptions()
//                                .center(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude()))
//                                .radius(50)
//                                .strokeColor(Color.BLUE)
//                                .fillColor(0x220000FF)
//                                .strokeWidth(5.0f));

                                place2 = new MarkerOptions().position(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude())).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker));
                                markerOptionsList.add(place2);
                                marker2 = mMap.addMarker(place2);

                                showAllMarkers();
                                showPolyline();

                                getDirection(riderLat, riderLng, Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude());
                                sendDropOffNotification(customerId);
                            } else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) {
                                            return;
                                        }
                                        Common.mLastKnownLocation = locationResult.getLastLocation();

//                                        geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), new GeoFire.CompletionListener() {
//                                            @Override
//                                            public void onComplete(String key, DatabaseError error) {
//                                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
//
//                                            }
//                                        });

                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                                        if(marker1 != null || marker2 != null){
                                            marker1.remove();
                                            marker2.remove();
                                            markerOptionsList.clear();
                                        }

                                        place1 = new MarkerOptions().position(new LatLng(riderLat, riderLng)).title("Pick Up Point").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                                        markerOptionsList.add(place1);
                                        marker1 = mMap.addMarker(place1);

                                        place2 = new MarkerOptions().position(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude())).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker));
                                        markerOptionsList.add(place2);
                                        marker2 = mMap.addMarker(place2);

                                        showAllMarkers();
                                        showPolyline();
                                        getDirection(riderLat, riderLng, Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude());
                                        sendDropOffNotification(customerId);

                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                            }
                        } else {
                            Toast.makeText(DriverTracking.this, "unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void getDirection(double latitude, double longitude, double latitude1, double longitude1) {

        String requestApi = null;
        try{

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+ latitude+","+longitude+"&"+
                    "destination="+latitude1+","+longitude1+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("EDMTDEV", requestApi);//print url for debug
            //Toast.makeText(DriverTracking.this, "han g 1", Toast.LENGTH_LONG).show();

            DownloadTask downloadTask = new DownloadTask();
            // Start downloading json data from Google Directions API
            downloadTask.execute(requestApi);


        } catch (Exception e){
            e.printStackTrace();
            Log.d("NEW_TOKEN", "ERROR: 3");
            //Toast.makeText(DriverTracking.this, "han g 3", Toast.LENGTH_LONG).show();
        }
    }

    private void showPolyline() {
        new FetchURL(DriverTracking.this)
                .execute(getUrl(place1.getPosition(),place2.getPosition(),"driving"),"driving");

    }

    private String getUrl(LatLng origin, LatLng destination, String directionMode) {
        String str_origin = "origin=" + origin.latitude + ","+ origin.longitude;

        String str_dest = "destination=" + destination.latitude + "," + destination.longitude;

        String mode = "mode=" + directionMode;

        String parameter = mode + "&" + str_origin + "&" + str_dest  ;

        String format = "json";

        String url = "https://maps.googleapis.com/maps/api/directions/" + format + "?"
                + parameter + "&key="+getResources().getString(R.string.google_direction_api);

        Log.d("ERROR",url);

        return url;
    }

    private void updateFirebaseToken() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        reference.child(user.getUid()).setValue(token);
    }

    private void showAllMarkers() {

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for(MarkerOptions m : markerOptionsList){
            builder.include(m.getPosition());
        }

        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.30);

        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        mMap.animateCamera(cu);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        //for custom window
//        mMap.getUiSettings().setZoomControlsEnabled(true);
//        mMap.getUiSettings().setZoomGesturesEnabled(true);
        //mMap.setInfoWindowAdapter(new CustomInfoWindow(this));

        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            layoutParams.setMargins(0, 450, 80, 0);
        }

        //check if gps is enabled or not and then request user to enable it
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(DriverTracking.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(DriverTracking.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        task.addOnFailureListener(DriverTracking.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(DriverTracking.this, 51);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        //Create Geo fencing with radius is 50m
        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.driver_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid()));
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(riderLat, riderLng), 0.1f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                sendArrivedNotification(customerId);
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }

    private void sendArrivedNotification(String customerId) {
        Token token = new Token(customerId);
//        Data data = new Data("Arrived", String.format("The driver has arrived at your location", Common.currentUser.getName()), "empty");
//        Sender sender = new Sender(token.getToken(), data);

        Map<String, String> content = new HashMap<>();
        content.put("title", "Arrived");
        content.put("message", String.format("The driver %s has arrived at your location", Common.currentUser.getName()));
        DataMessage dataMessage = new DataMessage(token.getToken(), content);

        mFCMService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if(response.body().success != 1){
                    Toast.makeText(DriverTracking.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    private void sendDropOffNotification(String customerId) {

        FirebaseDatabase.getInstance().getReference(Common.booked_driver_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //Toast.makeText(DriverTracking.this, "driver deleted from BookedDrivers", Toast.LENGTH_SHORT).show();
            }
        });

        GeoFire geoFireAgain = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.driver_tbl));
        geoFireAgain.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                //Toast.makeText(DriverTracking.this, "Driver added to driver tbl", Toast.LENGTH_SHORT).show();

            }
        });

        Token token = new Token(customerId);
//        Data data = new Data("DropOff", customerId, "empty");
//        Sender sender = new Sender(token.getToken(), data);

        Map<String, String> content = new HashMap<>();
        content.put("title", "DropOff");
        content.put("message", FirebaseAuth.getInstance().getCurrentUser().getUid());
        DataMessage dataMessage = new DataMessage(token.getToken(), content);

        mFCMService.sendMessage(dataMessage).enqueue(new Callback<FCMResponse>() {
            @Override
            public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                if(response.body().success != 1){
                    Toast.makeText(DriverTracking.this, "Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FCMResponse> call, Throwable t) {

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 51) {
            if (resultCode == RESULT_OK) {
                getDeviceLocation();
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void getDeviceLocation() {
        mFusedLocationProviderClient.getLastLocation()
                .addOnCompleteListener(new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Common.mLastKnownLocation = task.getResult();
                            if (Common.mLastKnownLocation != null) {

//                                geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), new GeoFire.CompletionListener() {
//                                    @Override
//                                    public void onComplete(String key, DatabaseError error) {
//                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
//
//                                    }
//                                });

                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                                if(marker1 != null || marker2 != null){
                                    marker1.remove();
                                    marker2.remove();
                                    markerOptionsList.clear();
                                }

                                place1 = new MarkerOptions().position(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude())).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                                markerOptionsList.add(place1);
                                marker1 = mMap.addMarker(place1);
//                                marker1 = mMap.addCircle(new CircleOptions()
//                                .center(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude()))
//                                .radius(50)
//                                .strokeColor(Color.BLUE)
//                                .fillColor(0x220000FF)
//                                .strokeWidth(5.0f));

                                place2 = new MarkerOptions().position(new LatLng(riderLat, riderLng)).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker));
                                markerOptionsList.add(place2);
                                marker2 = mMap.addMarker(place2);

                                showAllMarkers();
                                showPolyline();

                            } else {
                                final LocationRequest locationRequest = LocationRequest.create();
                                locationRequest.setInterval(10000);
                                locationRequest.setFastestInterval(5000);
                                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                                locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        if (locationResult == null) {
                                            return;
                                        }
                                        Common.mLastKnownLocation = locationResult.getLastLocation();

//                                        geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), new GeoFire.CompletionListener() {
//                                            @Override
//                                            public void onComplete(String key, DatabaseError error) {
//                                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
//
//                                            }
//                                        });

                                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));

                                        if(marker1 != null || marker2 != null){
                                            marker1.remove();
                                            marker2.remove();
                                            markerOptionsList.clear();
                                        }

                                        place1 = new MarkerOptions().position(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude())).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                                        markerOptionsList.add(place1);
                                        marker1 = mMap.addMarker(place1);

                                        place2 = new MarkerOptions().position(new LatLng(riderLat, riderLng)).title("Destination").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker));
                                        markerOptionsList.add(place2);
                                        marker2 = mMap.addMarker(place2);

                                        showAllMarkers();
                                        showPolyline();

                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                            }
                        } else {
                            Toast.makeText(DriverTracking.this, "unable to get last location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onTaskDone(Object... values) {

        if(currentPolyline != null)
            currentPolyline.remove();

        currentPolyline = mMap.addPolyline((PolylineOptions) values[0]);

    }



    ////////////////// get duration ///////////////////////////////////////

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";

            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();


            parserTask.execute(result);

        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {


            Log.d("result", result.toString());

            for (int i = 0; i < result.size(); i++) {


                List<HashMap<String, String>> path = result.get(i);

                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    String startingAddress, destinationAddress;

                    String  distanceok = point.get("distance");
                    String  duration = point.get("duration");
                    startingAddress = point.get("start_address");
                    destinationAddress = point.get("end_address");

//                    txtDistance.setText(distance);
//                    txtAddress.setText(end_address);
//                    txtTime.setText(duration);

                    if(startingAddress.isEmpty()){
                        startingAddress = "Pick Up Location";
                    }

                    String substr = distanceok.substring(distanceok.length()-2, distanceok.length());

                    String distance = "";

                    if(substr.equals("km") || substr.equals("Km")){
                        distance = distanceok.substring(0,distanceok.length()-3);
                    } else {
                        distance = distanceok.substring(0,distanceok.length()-2);
                    }

                    String dur;

                    if(duration.length() < 6){
                        dur = duration.substring(0,duration.length()-4);
                    } else {
                        dur = duration.substring(0,duration.length()-5);
                    }

                    Log.d("LAPTOP", distanceok);
                    Log.d("LAPTOP", duration);
                    Log.d("LAPTOP", substr);
                    Log.d("LAPTOP", distance);
                    Log.d("LAPTOP", dur);

                    double rate = Common.getPrice(Double.parseDouble(distance), Integer.parseInt(dur));
                    //double rate = 40.0;
                    Log.d("LAPTOP", String.valueOf(rate));

                    pricePrimary.setText(""+rate);
                    baseFare.setText("Rs. "+Common.base_fare);
                    timePrimary.setText(duration);
                    distancePrimary.setText(distance);
                    priceSecondary.setText("Rs. "+rate);

                    startLocation.setText(startingAddress);
                    endLocation.setText(destinationAddress);

                    detailLayout.setVisibility(View.VISIBLE);

                    btnStartTrip.setVisibility(View.GONE);



//                    mBottomSheet = BottomSheetRiderFragment.newInstance(startingAddress, destinationAddress, distance, duration);
//                    mBottomSheet.show(getSupportFragmentManager(), mBottomSheet.getTag());
                }


            }

        }
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.connect();

            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();
            Log.d("data", data);

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}