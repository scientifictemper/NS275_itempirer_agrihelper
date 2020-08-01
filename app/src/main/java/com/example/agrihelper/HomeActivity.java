package com.example.agrihelper;

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
import android.util.Log;
import android.widget.Toast;

import com.example.agrihelper.service.UserAdressService;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private ResultReceiver resultReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        resultReceiver = new UserAddressReceiver(new Handler());
        getUserLocation();
    }

    private void getUserLocation() {
        if (checkLocationPermission()) {
            final LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(2000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.getFusedLocationProviderClient(HomeActivity.this)
                    .requestLocationUpdates(locationRequest, new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            super.onLocationResult(locationResult);
                            LocationServices.getFusedLocationProviderClient(HomeActivity.this)
                                    .removeLocationUpdates(this);
                            if (locationResult != null && locationResult.getLocations().size() > 0) {
                                int latestLocationIndex = locationResult.getLocations().size()-1;
                                double latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                                double longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                                Location location = new Location("providerNA");
                                location.setLatitude(latitude);
                                location.setLongitude(longitude);
                                fetchUserAddress(location);
                            }
                        }
                    }, Looper.getMainLooper());
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeActivity.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
            return false;

        } else {
            return true;
        }
    }

    private void fetchUserAddress(Location location) {
        Intent intent = new Intent(this, UserAdressService.class);
        intent.putExtra("RECEIVER", resultReceiver);
        intent.putExtra("LOCATION_DATA", location);
        startService(intent);
    }

    private class UserAddressReceiver extends ResultReceiver {

        public UserAddressReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            if (resultCode == 1) {
                Toast.makeText(HomeActivity.this, resultData.getString("ADDRESS"), Toast.LENGTH_SHORT).show();
                Log.e("Location", Objects.requireNonNull(resultData.getString("ADDRESS")));
            } else {
                Toast.makeText(HomeActivity.this, "ADDRESS", Toast.LENGTH_SHORT).show();
            }
        }
    }
}