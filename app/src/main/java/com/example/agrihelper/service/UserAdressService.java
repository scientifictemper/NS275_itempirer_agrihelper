package com.example.agrihelper.service;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class UserAdressService extends IntentService {

    private ResultReceiver resultReceiver;

    public UserAdressService(String name) {
        super(name);
    }

    public UserAdressService(){
        super("UserAddressService");
    }
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            String errorMessage = "";
            resultReceiver = intent.getParcelableExtra("RECEIVER");
            Location location = intent.getParcelableExtra("LOCATION_DATA");
            Log.e("LOCATION DATA", location.toString());
            if (location == null){
                return;
            }
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
            }catch (Exception e) {
                errorMessage = e.getMessage();
                assert errorMessage != null;
                Log.e("Location Error", errorMessage);
            }
            if (addresses == null || addresses.isEmpty()) {
                deliverResultToReceiver(0, errorMessage);
            } else {
                Address address = addresses.get(0);
                ArrayList<String> addressFragments = new ArrayList<>();
                for (int i=0; i<=address.getMaxAddressLineIndex();i++) {
                    addressFragments.add(address.getLocality());
                }
                Log.e("ADDRESS", addressFragments.toString());
                deliverResultToReceiver(1,
                        TextUtils.join(Objects.requireNonNull(System.getProperty("line.separator"))
                                , addressFragments));
            }
        } else {
            Log.e("Intent", "is null");
        }
    }

    private void deliverResultToReceiver(int resultCode, String addressMessage) {
        Bundle bundle = new Bundle();
        bundle.putString("ADDRESS", addressMessage);
        resultReceiver.send(resultCode, bundle);
    }
}
