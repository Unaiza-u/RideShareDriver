package com.example.ridesharedriver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ridesharedriver.Common.Common;
import com.example.ridesharedriver.Direction.DirectionsJSONParser;
import com.example.ridesharedriver.Model.DataMessage;
import com.example.ridesharedriver.Model.FCMResponse;
import com.example.ridesharedriver.Model.Token;
import com.example.ridesharedriver.Remote.IFCMService;
import com.example.ridesharedriver.Remote.IGoogleAPI;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerCall extends AppCompatActivity {

    TextView txtTime, txtAddress, txtDistance, txtCountDown;

    TextView txtPrice, txtTimePS, txtStartAddress, txtEndAddress, txtDistancePS;
    Button btnCancel, btnAccept;

    MediaPlayer mediaPlayer;

    IGoogleAPI mService;
    IFCMService mFCMService;

    String customerId;

    double lat, lng, desti_lat, desti_lng;

    boolean check = false;
    GeoFire geoFire;
    CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_call);

        mService = Common.getGoogleAPI();
        mFCMService = Common.getFCMService();

        txtAddress = findViewById(R.id.txtAddress);
        txtDistance = findViewById(R.id.txtDistance);
        txtTime = findViewById(R.id.txtTime);

        txtStartAddress = findViewById(R.id.txtStartAddress);
        txtEndAddress = findViewById(R.id.txtEndAddress);
        txtDistancePS = findViewById(R.id.txtDistancePS);
        txtTimePS = findViewById(R.id.txtTimePS);
        txtPrice = findViewById(R.id.txtPrice);
        txtCountDown = findViewById(R.id.txt_count_down);

        btnAccept = findViewById(R.id.btnAccept);
        btnCancel = findViewById(R.id.btnDecline);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(customerId)){

                    cancelBooking(customerId);
                }
            }
        });

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(!TextUtils.isEmpty(customerId) && !TextUtils.isEmpty(String.valueOf(lat)) && !TextUtils.isEmpty(String.valueOf(lng)) && !TextUtils.isEmpty(String.valueOf(desti_lat)) && !TextUtils.isEmpty(String.valueOf(desti_lng))){
                    acceptBooking(customerId, lat, lng, desti_lat, desti_lng);
                } else {
                    Toast.makeText(CustomerCall.this, "accept not working", Toast.LENGTH_SHORT).show();
                }

//                FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl)
//                        .child(customerId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        Toast.makeText(CustomerCall.this, "pick up deleted" , Toast.LENGTH_SHORT).show();
//                    }
//                });
//
//
//                Intent intent = new Intent(CustomerCall.this, DriverTracking.class);
//                //send customer location to new activity
//                intent.putExtra("lat", lat);
//                intent.putExtra("lng", lng);
//
//                intent.putExtra("desti_lat", desti_lat);
//                intent.putExtra("desti_lng", desti_lng);
//
//                intent.putExtra("customerId", customerId);
//
//                startActivity(intent);
//                finish();
            }
        });

        mediaPlayer = MediaPlayer.create(this, R.raw.ringtone);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();

        if(getIntent() != null){
            lat = getIntent().getDoubleExtra("lat", -1.0);
            lng = getIntent().getDoubleExtra("lng", -1.0);

            desti_lat = getIntent().getDoubleExtra("desti_lat", -1.0);
            desti_lng = getIntent().getDoubleExtra("desti_lng", -1.0);

            customerId = getIntent().getStringExtra("customer");

            Log.d("NEW_TOKEN", String.valueOf(lat));
            Log.d("NEW_TOKEN", String.valueOf(lng));

            getDirection(lat, lng);

            fromPassengerToDestination(lat, lng, desti_lat,desti_lng);

        }
        
        startTimer();
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(31000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                txtCountDown.setText(String.valueOf(millisUntilFinished/1000));
            }

            @Override
            public void onFinish() {
                if(!TextUtils.isEmpty(customerId)){

                    cancelBooking(customerId);
                }
            }
        }.start();
    }

    private void fromPassengerToDestination(double lat, double lng, double desti_lat, double desti_lng) {
        String requestApi = null;
        try{

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+ lat+","+lng+"&"+
                    "destination="+desti_lat+","+desti_lng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("EDMTDEV", requestApi);//print url for debug
            //Toast.makeText(CustomerCall.this, "han g 1", Toast.LENGTH_LONG).show();

            DownloadTask downloadTask = new DownloadTask();
            // Start downloading json data from Google Directions API
            downloadTask.execute(requestApi);


        } catch (Exception e){
            e.printStackTrace();
            Log.d("NEW_TOKEN", "ERROR: 3");
            //Toast.makeText(CustomerCall.this, "han g 3", Toast.LENGTH_LONG).show();
        }
    }

    private void acceptBooking(final String customerId, final double lat, final double lng, final double desti_lat, final double desti_lng) {
        countDownTimer.cancel();

        FirebaseDatabase.getInstance().getReference(Common.pickup_request_tbl)
                .child(customerId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //Toast.makeText(CustomerCall.this, "pick up deleted from driver" , Toast.LENGTH_SHORT).show();
            }
        });
        FirebaseDatabase.getInstance().getReference(Common.driver_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //Toast.makeText(CustomerCall.this, "driver deleted from driver tbl", Toast.LENGTH_SHORT).show();
            }
        });

        geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(Common.booked_driver_tbl));
        geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(Common.mLastKnownLocation.getLatitude(), Common.mLastKnownLocation.getLongitude()), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                //Toast.makeText(CustomerCall.this, "Driver added to BookedDrivers", Toast.LENGTH_SHORT).show();

            }
        });

        Token token = new Token(customerId);

        Map<String, String> content = new HashMap<>();
        content.put("title", "Accepted");
        content.put("message", String.format("The driver %s has accepted your request", Common.currentUser.getName()));
        DataMessage dataMessage = new DataMessage(token.getToken(), content);

        mFCMService.sendMessage(dataMessage)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if(response.body().success == 1){
                            Toast.makeText(CustomerCall.this, "Accepted", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(CustomerCall.this, DriverTracking.class);
                            //send customer location to new activity
                            intent.putExtra("lat", lat);
                            intent.putExtra("lng", lng);

                            intent.putExtra("desti_lat", desti_lat);
                            intent.putExtra("desti_lng", desti_lng);

                            intent.putExtra("customerId", customerId);

                            startActivity(intent);
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
    }

    private void cancelBooking(String customerId) {
        Token token = new Token(customerId);

//        Data data = new Data("Cancel", "Driver has cancelled your request", "empty");
//        Sender sender = new Sender(token.getToken(), data);

        Map<String, String> content = new HashMap<>();
        content.put("title", "Cancel");
        content.put("message", String.format("The driver %s has cancelled your request", Common.currentUser.getName()));
        DataMessage dataMessage = new DataMessage(token.getToken(), content);

        mFCMService.sendMessage(dataMessage)
                .enqueue(new Callback<FCMResponse>() {
                    @Override
                    public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                        if(response.body().success == 1){
                            Toast.makeText(CustomerCall.this, "Cancelled", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<FCMResponse> call, Throwable t) {

                    }
                });
    }


    private void getDirection(double lat, double lng) {


        String requestApi = null;
        try{

            requestApi = "https://maps.googleapis.com/maps/api/directions/json?"+
                    "mode=driving&"+
                    "transit_routing_preference=less_driving&"+
                    "origin="+ Common.mLastKnownLocation.getLatitude()+","+Common.mLastKnownLocation.getLongitude()+"&"+
                    "destination="+lat+","+lng+"&"+
                    "key="+getResources().getString(R.string.google_direction_api);
            Log.d("EDMTDEV", requestApi);//print url for debug
            //Toast.makeText(CustomerCall.this, "han g 1", Toast.LENGTH_LONG).show();

            DownloadTask downloadTask = new DownloadTask();
            // Start downloading json data from Google Directions API
            downloadTask.execute(requestApi);


        } catch (Exception e){
            e.printStackTrace();
            Log.d("NEW_TOKEN", "ERROR: 3");
            //Toast.makeText(CustomerCall.this, "han g 3", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onStop() {
        mediaPlayer.release();
        super.onStop();
    }

    @Override
    protected void onPause() {
        mediaPlayer.release();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayer.start();
    }

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

                    String  distanceok = point.get("distance");
                    String  duration = point.get("duration");
                    String  end_address = point.get("end_address");
                    String  start_address = point.get("start_address");

                    if(!check) {

                        txtDistance.setText(distanceok);
                        txtAddress.setText("Location : " + end_address);
                        txtTime.setText(duration);

                        check = true;

                    } else {

                        String substr = distanceok.substring(distanceok.length()-2, distanceok.length());

                        String distance = "";

                        if(substr.equals("km") || substr.equals("Km")){
                            distance = distanceok.substring(0,distanceok.length()-3);
                        } else {
                            distance = distanceok.substring(0,distanceok.length()-2);
                        }

//                        String dur = "";
//
//                        if(duration.length() < 6){
//                            dur = duration.substring(0,duration.length()-4);
//                        } else {
//                            dur = duration.substring(0,duration.length()-5);
//                        }
//
//                        Log.d("LAPTOP", distanceok);
//                        Log.d("LAPTOP", duration);
//                        Log.d("LAPTOP", substr);
//                        Log.d("LAPTOP", distance);
//                        Log.d("LAPTOP", dur);

                        String dur = "";

                        if(duration.length() < 6){
                            dur = duration.substring(0,duration.length()-4);
                        } else if(duration.length() == 6) {
                            dur = duration.substring(0,duration.length()-5);
                        }else if(duration.length() == 7) {
                            dur = duration.substring(0,duration.length()-5);
                        } else {
                            long h=0,t_h=0,m=0,t_m=0;
                            if(duration.length() == 12){
                                t_h = Long.parseLong(duration.substring(0,duration.length()-11));
                                t_m = Long.parseLong(duration.substring(7,duration.length()-4));

                                h = t_h*60;
                                m = t_m+h;
                                dur = String.valueOf(m);
                            } else if(duration.length() == 13){
                                t_h = Long.parseLong(duration.substring(0,duration.length()-12));
                                if(t_h==1){
                                    t_m = Long.parseLong(duration.substring(7,duration.length()-5));

                                    h = t_h*60;
                                    m = t_m+h;
                                    dur = String.valueOf(m);
                                } else {
                                    t_m = Long.parseLong(duration.substring(8,duration.length()-4));

                                    h = t_h*60;
                                    m = t_m+h;
                                    dur = String.valueOf(m);
                                }

                            } else if(duration.length() == 14){
                                String tem = duration.substring(1,duration.length()-12);
                                if(tem.equals(" ")) {
                                    t_h = Long.parseLong(duration.substring(0, duration.length() - 13));
                                    if (t_h == 1) {
                                        t_m = Long.parseLong(duration.substring(7, duration.length() - 5));

                                        h = t_h * 60;
                                        m = t_m + h;
                                        dur = String.valueOf(m);
                                    } else {
                                        t_m = Long.parseLong(duration.substring(8, duration.length() - 5));

                                        h = t_h * 60;
                                        m = t_m + h;
                                        dur = String.valueOf(m);
                                    }
                                } else {
                                    t_h = Long.parseLong(duration.substring(0, duration.length() - 12));
                                    t_m = Long.parseLong(duration.substring(9, duration.length() - 4));

                                    h = t_h * 60;
                                    m = t_m + h;
                                    dur = String.valueOf(m);
                                }

                            }  else if(duration.length() == 15){
                                String tem = duration.substring(1,duration.length()-13);
                                if(tem.equals(" ")) {
                                    t_h = Long.parseLong(duration.substring(0,duration.length()-14));
                                    t_m = Long.parseLong(duration.substring(8,duration.length()-5));

                                    h = t_h*60;
                                    m = t_m+h;
                                    dur = String.valueOf(m);
                                } else {
                                    t_h = Long.parseLong(duration.substring(0,duration.length()-13));
                                    t_m = Long.parseLong(duration.substring(9,duration.length()-5));

                                    h = t_h*60;
                                    m = t_m+h;
                                    dur = String.valueOf(m);
                                }
                            } else if (duration.length() == 16) {
                                t_h = Long.parseLong(duration.substring(0, duration.length() - 14));
                                t_m = Long.parseLong(duration.substring(9, duration.length() - 5));

                                h = t_h * 60;
                                m = t_m + h;
                                dur = String.valueOf(m);
                            }

                        }



                        Log.d("LAPTOP", String.valueOf(duration.length()));

                        Log.d("LAPTOP", distanceok);
                        Log.d("LAPTOP", duration);
                        Log.d("LAPTOP", String.valueOf(duration.length()));
                        Log.d("LAPTOP", substr);
                        Log.d("LAPTOP", distance);
                        Log.d("LAPTOP", dur);

                        double rate = Common.getPrice(Double.parseDouble(distance), Integer.parseInt(dur));
                        //double rate = 40.0;
                        Log.d("LAPTOP", String.valueOf(rate));

                        txtPrice.setText("Rs. "+rate);
                        txtTimePS.setText(duration);
                        txtDistancePS.setText(distanceok);
                        txtStartAddress.setText("Start : " + start_address);
                        txtEndAddress.setText("Destination : "+ end_address);



                    }

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
