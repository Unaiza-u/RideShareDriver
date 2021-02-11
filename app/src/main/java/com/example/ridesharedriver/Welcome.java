package com.example.ridesharedriver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ridesharedriver.Common.Common;
import com.example.ridesharedriver.DirectionHelpers.FetchURL;
import com.example.ridesharedriver.DirectionHelpers.TaskLoadedCallback;
import com.example.ridesharedriver.Model.Token;
import com.example.ridesharedriver.Remote.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;


import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.skyfishjy.library.RippleBackground;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Welcome extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback, PopupMenu.OnMenuItemClickListener {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private List<AutocompletePrediction> predictionList;


    private LocationCallback locationCallback;

//    private MaterialSearchBar materialSearchBar;
    private View mapView;
//    private Button btnFind;
    //private RippleBackground rippleBg;

    private LatLng latLngOfPlace;

    GeoFire geoFire;
    //ImageView rip;
    ImageView btnMenu;

    private final float DEFAULT_ZOOM = 15;

    SwitchCompat location_switch;
    SupportMapFragment mapFragment;

    //Direction
    private MarkerOptions place1, place2;
    private Polyline currentPolyline;

    List<MarkerOptions> markerOptionsList = new ArrayList<>();

    private Marker marker1, marker2;

    private DatabaseReference onlineRef, currentUserRef;

    private DatabaseReference changeDriver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

//        materialSearchBar = findViewById(R.id.searchBar);
//        btnFind = findViewById(R.id.btn_find_user);
        btnMenu = findViewById(R.id.btn_menu);
//        rippleBg = findViewById(R.id.ripple_bg);
//        rip = findViewById(R.id.rip);


        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapView = mapFragment.getView();

        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        currentUserRef = FirebaseDatabase.getInstance().getReference(Common.driver_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        onlineRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //we will remove value from Driver tbl when driver disconnected
                currentUserRef.onDisconnect().removeValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        //rip.setVisibility(View.GONE);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(Welcome.this);
        Places.initialize(Welcome.this, getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);
        final AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

//        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
//            @Override
//            public void onSearchStateChanged(boolean enabled) {
//
//            }
//
//            @Override
//            public void onSearchConfirmed(CharSequence text) {
//                startSearch(text.toString(), true, null, true);
//            }
//
//            @Override
//            public void onButtonClicked(int buttonCode) {
//                if (buttonCode == MaterialSearchBar.BUTTON_NAVIGATION) {
//                    //opening or closing a navigation drawer
//                } else if (buttonCode == MaterialSearchBar.BUTTON_BACK) {
//                    materialSearchBar.disableSearch();
//                }
//            }
//        });
//
//        materialSearchBar.addTextChangeListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                FindAutocompletePredictionsRequest predictionsRequest = FindAutocompletePredictionsRequest.builder()
//                        .setCountry("pk")
//                        .setTypeFilter(TypeFilter.ADDRESS)
//                        .setSessionToken(token)
//                        .setQuery(s.toString())
//                        .build();
//                placesClient.findAutocompletePredictions(predictionsRequest).addOnCompleteListener(new OnCompleteListener<FindAutocompletePredictionsResponse>() {
//                    @Override
//                    public void onComplete(@NonNull Task<FindAutocompletePredictionsResponse> task) {
//                        if (task.isSuccessful()) {
//                            FindAutocompletePredictionsResponse predictionsResponse = task.getResult();
//                            if (predictionsResponse != null) {
//                                predictionList = predictionsResponse.getAutocompletePredictions();
//                                List<String> suggestionsList = new ArrayList<>();
//                                for (int i = 0; i < predictionList.size(); i++) {
//                                    AutocompletePrediction prediction = predictionList.get(i);
//                                    suggestionsList.add(prediction.getFullText(null).toString());
//                                }
//                                materialSearchBar.updateLastSuggestions(suggestionsList);
//                                if (!materialSearchBar.isSuggestionsVisible()) {
//                                    materialSearchBar.showSuggestionsList();
//                                }
//                            }
//                        } else {
//                            Log.i("mytag", "prediction fetching task unsuccessful");
//                        }
//                    }
//                });
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//
//        materialSearchBar.setSuggstionsClickListener(new SuggestionsAdapter.OnItemViewClickListener() {
//            @Override
//            public void OnItemClickListener(int position, View v) {
//                if (position >= predictionList.size()) {
//                    return;
//                }
//                AutocompletePrediction selectedPrediction = predictionList.get(position);
//                String suggestion = materialSearchBar.getLastSuggestions().get(position).toString();
//                materialSearchBar.setText(suggestion);
//
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        materialSearchBar.clearSuggestions();
//                    }
//                }, 1000);
//                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//                if (imm != null)
//                    imm.hideSoftInputFromWindow(materialSearchBar.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
//                final String placeId = selectedPrediction.getPlaceId();
//                List<Place.Field> placeFields = Arrays.asList(Place.Field.LAT_LNG);
//
//                FetchPlaceRequest fetchPlaceRequest = FetchPlaceRequest.builder(placeId, placeFields).build();
//                placesClient.fetchPlace(fetchPlaceRequest).addOnSuccessListener(new OnSuccessListener<FetchPlaceResponse>() {
//                    @Override
//                    public void onSuccess(FetchPlaceResponse fetchPlaceResponse) {
//                        Place place = fetchPlaceResponse.getPlace();
//                        Log.i("mytag", "Place found: " + place.getName());
//                        latLngOfPlace = place.getLatLng();
//                        if (latLngOfPlace != null) {
//                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngOfPlace, DEFAULT_ZOOM));
////                            if(markerOptionsList != null){
////                                marker1.remove();
////                                marker2.remove();
////                                markerOptionsList.clear();
////                            }
//
//                            if(marker1 != null || marker2 != null){
//                                marker1.remove();
//                                marker2.remove();
//                                markerOptionsList.clear();
//                            }
//
//                            if(currentPolyline != null)
//                                currentPolyline.remove();
//
//                            place1 = new MarkerOptions().position(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude())).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
//                            markerOptionsList.add(place1);
//                            marker1 = mMap.addMarker(place1);
//
//                            place2 = new MarkerOptions().position(new LatLng(latLngOfPlace.latitude, latLngOfPlace.longitude)).title("Passenger").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker));
//                            markerOptionsList.add(place2);
//                            marker2 = mMap.addMarker(place2);
//
//                            showAllMarkers();
//                        }
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        if (e instanceof ApiException) {
//                            ApiException apiException = (ApiException) e;
//                            apiException.printStackTrace();
//                            int statusCode = apiException.getStatusCode();
//                            Log.i("mytag", "place not found: " + e.getMessage());
//                            Log.i("mytag", "status code: " + statusCode);
//                        }
//                    }
//                });
//            }
//
//            @Override
//            public void OnItemDeleteListener(int position, View v) {
//
//            }
//        });
//        btnFind.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                new FetchURL(Welcome.this)
//                        .execute(getUrl(place1.getPosition(),place2.getPosition(),"driving"),"driving");
//
////                LatLng currentMarkerLocation = mMap.getCameraPosition().target;
////                rippleBg.startRippleAnimation();
////                new Handler().postDelayed(new Runnable() {
////                    @Override
////                    public void run() {
////                        rippleBg.stopRippleAnimation();
////                        //startActivity(new Intent(Welcome.this, TempActivity.class));
////                        //finish();
////                    }
////                }, 3000);
//
//            }
//        });

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(Welcome.this, v);
                popupMenu.setOnMenuItemClickListener(Welcome.this);
                popupMenu.inflate(R.menu.menu_welcome);
                popupMenu.show();
            }
        });



        location_switch = findViewById(R.id.location_switch);
        location_switch.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if(location_switch.isChecked()){

                    FirebaseDatabase.getInstance().goOnline(); // set connected when switch to on

                    //rip.setVisibility(View.VISIBLE);
                    mMap.setMyLocationEnabled(true);
                    getDeviceLocation();
                    Snackbar.make(mapFragment.getView(), "You are online", Snackbar.LENGTH_SHORT)
                            .show();
                } else {

                    FirebaseDatabase.getInstance().goOffline(); // set disconnected when switch to off

                    mMap.clear();
                    //rip.setVisibility(View.GONE);
                    mMap.setMyLocationEnabled(false);
                    Snackbar.make(mapFragment.getView(), "You are offline", Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        });
//        location_switch.setOnCheckedChangeListener(new MaterialAnimatedSwitch.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(boolean isOnline) {
//                if(isOnline){
//                    rip.setVisibility(View.VISIBLE);
//                    mMap.setMyLocationEnabled(true);
//                    getDeviceLocation();
//                    Snackbar.make(mapFragment.getView(), "You are online", Snackbar.LENGTH_SHORT)
//                            .show();
//                } else {
////                    if(mCurrent != null)
////                        mCurrent.remove();
//                    mMap.clear();
//                    rip.setVisibility(View.GONE);
//                    mMap.setMyLocationEnabled(false);
//                    Snackbar.make(mapFragment.getView(), "You are offline", Snackbar.LENGTH_SHORT)
//                            .show();
//                }
//
//            }
//        });

        changeDriver = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
        changeDriver.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //if have any change from Driver table, we will reload aal driver available
                //loadAllAvaliableDriver();

                    //Toast.makeText(Welcome.this, "1", Toast.LENGTH_SHORT).show();
//                    if(markerCar != null ){
//                        markerCar.remove();
//                        markerOptionsList.clear();
//                        loadAllAvaliableDriver();


                    //markerOptionsList.clear();
                    if (marker1 != null) {
                        mMap.clear();
                        marker1.remove();
                        markerOptionsList.clear();
                        getDeviceLocation();
//                        if (marker2 != null) {
//                            markerOptionsList.add(place2);
//                        }
//
//                        if (currentPolyline != null) {
//                            currentPolyline.remove();
//
//                        }
                    }



//                    if (Common.mLastKnownLocation != null) {
//
//                        place1 = new MarkerOptions().position(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude())).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
//                        markerOptionsList.add(place1);
//                        marker1 = mMap.addMarker(place1);
//                    }
//
//                    if (place2 != null) {
//                        place2 = new MarkerOptions().position(new LatLng(latLngOfPlace.latitude, latLngOfPlace.longitude)).title("Passenger").icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_marker));
//                        markerOptionsList.add(place2);
//                        marker2 = mMap.addMarker(place2);
//                    }



//                    }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        //Geofire to store data
        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.driver_tbl));

        updateFirebaseToken();
    }



    private void updateFirebaseToken() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
        Token token = new Token(FirebaseInstanceId.getInstance().getToken());
        reference.child(user.getUid()).setValue(token);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.tripHistory:
//                Toast.makeText(this, "Trip History selected", Toast.LENGTH_SHORT).show();
//                return true;
//            case R.id.wayBill:
//                Toast.makeText(this, "Way Bill selected", Toast.LENGTH_SHORT).show();
//                return true;
            case R.id.updateInformation:
                //Toast.makeText(this, "Update Information selected", Toast.LENGTH_SHORT).show();
                showDialogUpdateInformation();
                return true;
            case R.id.changePassword:
                //Toast.makeText(this, "Change Password selected", Toast.LENGTH_SHORT).show();
                showDialogChangePwd();
                return true;
//            case R.id.help:
//                Toast.makeText(this, "Help selected", Toast.LENGTH_SHORT).show();
//                return true;
//            case R.id.setting:
//                Toast.makeText(this, "Setting selected", Toast.LENGTH_SHORT).show();
//                return true;
            case R.id.signOut:
                //Toast.makeText(this, "Sign Out selected", Toast.LENGTH_SHORT).show();
                signOut();
                return true;
            default:
                return false;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.tripHistory:
//                Toast.makeText(this, "Trip History selected", Toast.LENGTH_SHORT).show();
//                return true;
//            case R.id.wayBill:
//                Toast.makeText(this, "Way Bill selected", Toast.LENGTH_SHORT).show();
//                return true;
            case R.id.updateInformation:
                //Toast.makeText(this, "Update Information selected", Toast.LENGTH_SHORT).show();
                showDialogUpdateInformation();
                return true;
            case R.id.changePassword:
                //Toast.makeText(this, "Change Password selected", Toast.LENGTH_SHORT).show();
                showDialogChangePwd();
                return true;
//            case R.id.help:
//                Toast.makeText(this, "Help selected", Toast.LENGTH_SHORT).show();
//                return true;
//            case R.id.setting:
//                Toast.makeText(this, "Setting selected", Toast.LENGTH_SHORT).show();
//                return true;
            case R.id.signOut:
                //Toast.makeText(this, "Sign Out selected", Toast.LENGTH_SHORT).show();
                signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDialogUpdateInformation() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Welcome.this);
        alertDialog.setTitle("CHANGE INFORMATION");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_pwd = inflater.inflate(R.layout.layout_update_info, null);
        //Toast.makeText(this, "g", Toast.LENGTH_SHORT).show();

        final MaterialEditText edtNameInfo = layout_pwd.findViewById(R.id.edtNameInfo);
        final MaterialEditText edtPhoneInfo = layout_pwd.findViewById(R.id.edtPhoneInfo);

        alertDialog.setView(layout_pwd);

        //setButton
        alertDialog.setPositiveButton("CHANGE INFORMATION", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {



                if(TextUtils.isEmpty(edtNameInfo.getText().toString())){
                    Toast.makeText(Welcome.this, "Please enter Name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(edtPhoneInfo.getText().toString())){
                    Toast.makeText(Welcome.this, "Please enter Phone Number", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialogInterface.dismiss();

                final android.app.AlertDialog waitingDialog= new SpotsDialog.Builder()
                        .setMessage("Please Wait for Information Reset")
                        .setCancelable(false)
                        .setContext(Welcome.this)
                        .build();
                waitingDialog.show();

                DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("name", edtNameInfo.getText().toString());
                reference.updateChildren(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Welcome.this, "name Updated Successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Welcome.this, "name Update UnSuccessful", Toast.LENGTH_SHORT).show();
                        }
                        waitingDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Welcome.this, "name Failed to Update", Toast.LENGTH_SHORT).show();
                        waitingDialog.dismiss();
                    }
                });

                DatabaseReference referenceNew = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid());

                HashMap<String, Object> hashMapNew = new HashMap<>();
                hashMapNew.put("phone", edtPhoneInfo.getText().toString());
                referenceNew.updateChildren(hashMapNew)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(Welcome.this, "phone Updated Successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(Welcome.this, "phone Update UnSuccessful", Toast.LENGTH_SHORT).show();
                                }
                                waitingDialog.dismiss();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(Welcome.this, "phone Failed to Update", Toast.LENGTH_SHORT).show();
                        waitingDialog.dismiss();
                    }
                });
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }

    private void showDialogChangePwd() {
        //Toast.makeText(this, "g", Toast.LENGTH_SHORT).show();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Welcome.this);
        alertDialog.setTitle("CHANGE PASSWORD");
        alertDialog.setMessage("Please fill all information");

        LayoutInflater inflater = this.getLayoutInflater();
        View layout_pwd = inflater.inflate(R.layout.layout_change_pwd, null);
        //Toast.makeText(this, "g", Toast.LENGTH_SHORT).show();

        final MaterialEditText edtPassword = layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword = layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatNewPassword = layout_pwd.findViewById(R.id.edtRepeatNewPassword);

        alertDialog.setView(layout_pwd);

        //setButton
        alertDialog.setPositiveButton("CHANGE PASSWORD", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {



                if(TextUtils.isEmpty(edtPassword.getText().toString())){
                    Toast.makeText(Welcome.this, "Please enter Old Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(edtNewPassword.getText().toString())){
                    Toast.makeText(Welcome.this, "Please enter New Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(edtRepeatNewPassword.getText().toString())){
                    Toast.makeText(Welcome.this, "Please enter Confirm Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                dialogInterface.dismiss();

                final android.app.AlertDialog waitingDialog= new SpotsDialog.Builder()
                        .setMessage("Please Wait for Password Reset")
                        .setCancelable(false)
                        .setContext(Welcome.this)
                        .build();
                waitingDialog.show();

                if(edtNewPassword.getText().toString().equals(edtRepeatNewPassword.getText().toString())){

                    String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                    //Get auth credentials from the user for re-authentication.
                    //Example with only email
                    AuthCredential credential = EmailAuthProvider.getCredential(email, edtPassword.getText().toString());
                    FirebaseAuth.getInstance().getCurrentUser()
                            .reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){

                                        FirebaseAuth.getInstance().getCurrentUser()
                                                .updatePassword(edtRepeatNewPassword.getText().toString())
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){

                                                            //Update Driver information password column
                                                            updateProfile(edtRepeatNewPassword.getText().toString());
                                                            waitingDialog.dismiss();

                                                        } else {
                                                            waitingDialog.dismiss();
                                                            Toast.makeText(Welcome.this, "Password doesn't change", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        waitingDialog.dismiss();
                                        Toast.makeText(Welcome.this, "Wrong Old Password", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }else {
                    waitingDialog.dismiss();
                    Toast.makeText(Welcome.this, "Password doesn't match", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        alertDialog.show();
    }

    private void updateProfile(String password){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("password", password);
        reference.updateChildren(hashMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(Welcome.this, "Password was Changes!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(Welcome.this, "Password was changed but not Updated in Database", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signOut() {

        //Reset remember value
        Paper.init(this);
        Paper.book().destroy();

        String uId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Toast.makeText(this, "Signing out....", Toast.LENGTH_SHORT).show();

        FirebaseAuth.getInstance().signOut();
//        FirebaseDatabase.getInstance().getReference(Common.driver_tbl)
//                .child(uId).removeValue();
        //Common.exitUser = true;
        Intent intent = new Intent(Welcome.this, MainActivity.class);
//        intent.putExtra("exit_user", uId);
        startActivity(intent);
        finish();
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

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        if (mapView != null && mapView.findViewById(Integer.parseInt("1")) != null) {
            View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
            layoutParams.setMargins(0, 200, 80, 0);
        }

        //check if gps is enabled or not and then request user to enable it
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(Welcome.this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(Welcome.this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                getDeviceLocation();
            }
        });

        task.addOnFailureListener(Welcome.this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    try {
                        resolvable.startResolutionForResult(Welcome.this, 51);
                    } catch (IntentSender.SendIntentException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

//        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
//            @Override
//            public boolean onMyLocationButtonClick() {
//                if (materialSearchBar.isSuggestionsVisible())
//                    materialSearchBar.clearSuggestions();
//                if (materialSearchBar.isSearchEnabled())
//                    materialSearchBar.disableSearch();
//                return false;
//            }
//        });
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
                                if(location_switch.isChecked()) {
                                    geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude()), new GeoFire.CompletionListener() {
                                        @Override
                                        public void onComplete(String key, DatabaseError error) {
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                            place1 = new MarkerOptions().position(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude())).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                                            markerOptionsList.add(place1);
                                            mMap.addMarker(place1);

                                        }
                                    });
                                }else {
                                    Log.d("ERROR","Cannot get Your location");
                                }
                                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
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
                                        if(location_switch.isChecked()) {
                                            geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude()), new GeoFire.CompletionListener() {
                                                @Override
                                                public void onComplete(String key, DatabaseError error) {
                                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                                    place1 = new MarkerOptions().position(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude())).title("You").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker));
                                                    markerOptionsList.add(place1);
                                                    marker1 = mMap.addMarker(place1);

                                                }
                                            });
                                        }else {
                                            Log.d("ERROR","Cannot get Your location");
                                        }
                                        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                                        mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
                                    }
                                };
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);

                            }
                        } else {
                            Toast.makeText(Welcome.this, "unable to get last location", Toast.LENGTH_SHORT).show();
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
}
