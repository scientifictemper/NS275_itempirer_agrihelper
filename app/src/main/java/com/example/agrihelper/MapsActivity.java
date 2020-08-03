package com.example.agrihelper;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.agrihelper.service.UserAdressService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TextView currentLocationText, humidityText, windText, temperatureText;
    private View view, detailsView;
    private ResultReceiver resultReceiver;
    private ProgressBar progressBar;
    private String UserLocationId;
    private Button moreBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initViews();
        resultReceiver = new UserAddressReceiver(new Handler());
    }

    private void initViews() {
        currentLocationText = findViewById(R.id.current_location);
        humidityText = findViewById(R.id.humidity_text);
        temperatureText = findViewById(R.id.temperature_text);
        progressBar = findViewById(R.id.progressBar);
        view = findViewById(R.id.card_view);
        detailsView = findViewById(R.id.details);
        moreBtn = findViewById(R.id.moreBtn);
        moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.snapshot(new GoogleMap.SnapshotReadyCallback() {
                    @Override
                    public void onSnapshotReady(Bitmap bitmap) {
//                        mapPreview.setImageBitmap(bitmap);
                        Intent intent = new Intent(MapsActivity.this, DetailsActivity.class);
                        saveImage(bitmap);
//                        intent.putExtra("BITMAP", bitmap);
                        startActivity(intent);
                    }
                });
            }
        });
    }

    public void saveImage(Bitmap bitmap) {
        String fileName = "myImage";//no .png or .jpg needed
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            FileOutputStream fo = openFileOutput(fileName, Context.MODE_PRIVATE);
            fo.write(bytes.toByteArray());
            // remember close file output
            fo.close();
        } catch (Exception e) {
            e.printStackTrace();
            fileName = null;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng bihar = new LatLng(25.0961, 85.3131);
        mMap.addMarker(new MarkerOptions().position(bihar).title("Marker in Bihar"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(bihar));
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Location location = new Location("providerNA");
                location.setLatitude(latLng.latitude);
                location.setLongitude(latLng.longitude);
                view.setVisibility(View.VISIBLE);
                fetchUserAddress(location);
                Toast.makeText(MapsActivity.this,
                        "Lat: "+String.valueOf(latLng.latitude)+"\n Lon: "+String.valueOf(latLng.longitude), Toast.LENGTH_SHORT).show();
                Log.e("LAT LON", String.valueOf(latLng.latitude)+" "+String.valueOf(latLng.longitude));
            }
        });
    }

    private void fetchUserAddress(Location location) {
        progressBar.setVisibility(View.VISIBLE);
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
                currentLocationText.setText(resultData.getString("ADDRESS"));
                progressBar.setVisibility(View.INVISIBLE);
//                getUserLocationId(resultData.getString("ADDRESS"));
                Log.e("Location", resultData.getString("ADDRESS") + " ");
            } else {
                Toast.makeText(MapsActivity.this, "ADDRESS", Toast.LENGTH_SHORT).show();
            }
        }

        private void getUserLocationId(String city) {
            Call<ArrayList<UserLocationResponse>> call = CurrentUserLocationId.getLocationId(city);
            call.enqueue(new Callback<ArrayList<UserLocationResponse>>() {
                @Override
                public void onResponse(@NonNull Call<ArrayList<UserLocationResponse>> call, @NonNull Response<ArrayList<UserLocationResponse>> response) {
                    if (response.code() == 200) {
                        ArrayList<UserLocationResponse> accuweatherResponse = response.body();
                        assert accuweatherResponse != null;
                        if (accuweatherResponse.size() > 0) {
                            UserLocationId = accuweatherResponse.get(0).locationId;
                            getWeatherData(UserLocationId);
                            Log.e("Location Res", accuweatherResponse.get(0).locationId + " ");
                        }
                    }
                }
                @Override
                public void onFailure(@NonNull Call<ArrayList<UserLocationResponse>> call, @NonNull Throwable t) {
                    Log.e("Weather Data error", t.toString());
                }
            });
        }

        private void getWeatherData(String CityId) {
            Call<ArrayList<AccuweatherResponse>> call = AccuWeather.getCurrentWeather(CityId);
            call.enqueue(new Callback<ArrayList<AccuweatherResponse>>() {
                @Override
                public void onResponse(@NonNull Call<ArrayList<AccuweatherResponse>> call, @NonNull Response<ArrayList<AccuweatherResponse>> response) {
                    if (response.code() == 200) {
                        ArrayList<AccuweatherResponse> accuweatherResponse = response.body();
                        assert accuweatherResponse != null;
                        setData(accuweatherResponse);
                    }
                }
                @Override
                public void onFailure(@NonNull Call<ArrayList<AccuweatherResponse>> call, @NonNull Throwable t) {
                    Log.e("Weather Data error", t.toString());
                }
            });
        }

        private void setData(ArrayList<AccuweatherResponse> response) {
            temperatureText.setText(String.valueOf(response.get(0).temperature.metric.value)+ " C");
            humidityText.setText(String.valueOf(response.get(0).humidity));
            detailsView.setVisibility(View.VISIBLE);
        }
    }


}