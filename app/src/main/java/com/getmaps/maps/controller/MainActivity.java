package com.getmaps.maps.controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.getmaps.maps.service.Constant;
import com.getmaps.maps.R;
import com.getmaps.maps.service.FetchAddressIntentService;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity {
    private static int REQUEST_CODE_LOCATION_PERMISSION = 1;

    //    private Button openLoc;
    private TextView textLatLong,textAddress;
    private ProgressBar progressBar;
    private ResultReceiver resultReceiver;
    private Button openLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textLatLong = findViewById(R.id.text_lat_long);
        progressBar = findViewById(R.id.progress_bar);
        textAddress = findViewById(R.id.text_address);
        openLoc =findViewById(R.id.open_location);

        resultReceiver = new AddressResultReceiver(new Handler());

         openLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(MainActivity.this, new String
                            []{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION
                    );

                } else {
                    getCurrentLocation();
                }

            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();

            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void getCurrentLocation() {
        progressBar.setVisibility(View.VISIBLE);
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {

                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                .removeLocationUpdates(this);

                        if(locationResult != null && locationResult.getLocations().size() > 0 ){
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            double latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            double longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();

                            textLatLong.setText(
                                    String.format("Latitude: %s\nLongitude: %s"
                                    ,latitude,longitude

                                    )
                            );

                            Location location = new Location("providerNA");
                            location.setLatitude(latitude);
                            location.setLongitude(longitude);
                            fetchAddressFromLatLong(location);
                        }
                        else{
                            progressBar.setVisibility(View.GONE);
                        }


                    }
                }, Looper.getMainLooper());
    }

    private void fetchAddressFromLatLong(Location location){
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(Constant.RECEIVER,resultReceiver);
        intent.putExtra(Constant.LOCATION_DATA_EXTRA, location);
        startService(intent);


    }

    private class AddressResultReceiver extends ResultReceiver{
         AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);

            if(resultCode == Constant.SUCCESS_RESULT){
                textAddress.setText(resultData.getString(Constant.RESULT_DATA_KEY) );

            }
            else{
                Toast.makeText(MainActivity.this, resultData.getString(Constant.RESULT_DATA_KEY),Toast.LENGTH_SHORT).show();
            }
            progressBar.setVisibility(View.GONE);

        }
    }

}