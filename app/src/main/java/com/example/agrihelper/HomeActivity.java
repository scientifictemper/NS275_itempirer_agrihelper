package com.example.agrihelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import com.example.agrihelper.Model.ForecastWeatherData;
import com.example.agrihelper.service.UserAdressService;
import com.example.agrihelper.utils.TextViewFactory;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;
    private ResultReceiver resultReceiver;
    private TextSwitcher tempText, humidityText, windText, descText;
    TextView current_location;
    Typeface typeface;
    RecyclerView recyclerView;
    int[] colors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initializeUI();
        resultReceiver = new UserAddressReceiver(new Handler());
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        getUserLocation();
    }

    private void initializeUI() {
        tempText = findViewById(R.id.temp_text_view);
        descText = findViewById(R.id.description_text_view);
        humidityText = findViewById(R.id.humidity_text_view);
        windText = findViewById(R.id.wind_text_view);
        recyclerView = findViewById(R.id.recycler_view);
        current_location = findViewById(R.id.current_location);
        typeface = Typeface.createFromAsset(getAssets(), "fonts/Vazir.ttf");
        setupTextSwitchers();
        colors = getResources().getIntArray(R.array.mdcolor_500);
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
                                int latestLocationIndex = locationResult.getLocations().size() - 1;
                                double latitude = locationResult.getLocations().get(latestLocationIndex).getLatitude();
                                double longitude = locationResult.getLocations().get(latestLocationIndex).getLongitude();
                                Location location = new Location("providerNA");
                                location.setLatitude(latitude);
                                location.setLongitude(longitude);
                                fetchUserAddress(location);
                                getWeatherData(location);
                                getForecastData(location);
                            }
                        }
                    }, Looper.getMainLooper());
        }
    }

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION_PERMISSION);
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
                current_location.setText(resultData.getString("ADDRESS"));
                Toast.makeText(HomeActivity.this, resultData.getString("ADDRESS"), Toast.LENGTH_SHORT).show();
                Log.e("Location", Objects.requireNonNull(resultData.getString("ADDRESS")));
            } else {
                Toast.makeText(HomeActivity.this, "ADDRESS", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getWeatherData(Location location) {
        Call<ArrayList<AccuweatherResponse>> call = AccuWeather.getCurrentWeather(location);
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
        AccuweatherResponse accuweatherResponse = response.get(0);
        tempText.setText(String.valueOf(accuweatherResponse.temperature.metric.value));
        descText.setText(accuweatherResponse.weatherText);
        humidityText.setText(String.valueOf(accuweatherResponse.humidity));
        windText.setText(String.valueOf(accuweatherResponse.wind.speed.windMetric.value));
    }

    private void getForecastData(Location location) {
        Call<AccuweatherForecastResponse> call = AccuWeatherForecast.getForecastData(location);
        call.enqueue(new Callback<AccuweatherForecastResponse>() {
            @Override
            public void onResponse(@NonNull Call<AccuweatherForecastResponse> call, @NonNull Response<AccuweatherForecastResponse> response) {
                if (response.code() == 200) {
                    AccuweatherForecastResponse accuweatherResponse = response.body();
                    assert accuweatherResponse != null;
                    convertToData(accuweatherResponse);
                    Log.e("Forecast Data", accuweatherResponse.data.get(0).date);
                    Log.e("Forecast Data", String.valueOf(accuweatherResponse.data.get(0).temperature.maximum.value));
                }
            }
            @Override
            public void onFailure(@NonNull Call<AccuweatherForecastResponse> call, @NonNull Throwable t) {
                Log.e("Forecast Data error", t.toString());
            }
        });
    }

    private void convertToData(AccuweatherForecastResponse response) {
        ArrayList<ForecastWeatherData> forecastData = new ArrayList<>();
        ArrayList<Data> dataArrayList = response.data;
        for (int i=0; i<dataArrayList.size();i++) {
            Data data = dataArrayList.get(i);
            ForecastWeatherData data1 = new ForecastWeatherData(data.date, data.temperature.minimum.value, data.temperature.maximum.value, colors[i]);
            forecastData.add(data1);
        }
        initRecyclerView(forecastData);
    }

    private void initRecyclerView(ArrayList<ForecastWeatherData> data) {
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        ForecastWeatherAdapter adapter = new ForecastWeatherAdapter(data, this);
        recyclerView.setAdapter(adapter);
    }

    private void setupTextSwitchers() {
        tempText.setFactory(new TextViewFactory(HomeActivity.this, R.style.TempTextView, true, typeface));
        tempText.setInAnimation(HomeActivity.this, R.anim.slide_in_right);
        tempText.setOutAnimation(HomeActivity.this, R.anim.slide_out_left);
        descText.setFactory(new TextViewFactory(HomeActivity.this, R.style.DescriptionTextView, true, typeface));
        descText.setInAnimation(HomeActivity.this, R.anim.slide_in_right);
        descText.setOutAnimation(HomeActivity.this, R.anim.slide_out_left);
        humidityText.setFactory(new TextViewFactory(HomeActivity.this, R.style.HumidityTextView, false, typeface));
        humidityText.setInAnimation(HomeActivity.this, R.anim.slide_in_bottom);
        humidityText.setOutAnimation(HomeActivity.this, R.anim.slide_out_top);
        windText.setFactory(new TextViewFactory(HomeActivity.this, R.style.WindSpeedTextView, false, typeface));
        windText.setInAnimation(HomeActivity.this, R.anim.slide_in_bottom);
        windText.setOutAnimation(HomeActivity.this, R.anim.slide_out_top);
    }

}