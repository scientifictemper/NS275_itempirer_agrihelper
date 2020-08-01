package com.example.agrihelper;

import android.location.Location;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface AccuweatherService {
    @GET("currentconditions/v1/2801585?")
    Call<ArrayList<AccuweatherResponse>> getWeatherData(@Query("apikey") String apiKey, @Query("details") boolean details);
}

class AccuweatherResponse {
    @SerializedName("Temperature")
    public Temperature temperature;
    @SerializedName("WeatherText")
    public String weatherText;
    @SerializedName("Wind")
    public AccuWind wind;
    @SerializedName("RelativeHumidity")
    public String humidity;
}


class Temperature {
    @SerializedName("Metric")
    public Metric metric;
}

class Metric {
    @SerializedName("Value")
    public float value;
    @SerializedName("Unit")
    public String unit;
}

class AccuWind {
    @SerializedName("Speed")
    public Speed speed;
}

class Speed {
    @SerializedName("Metric")
    public WindMetric windMetric;
}

class WindMetric {
    @SerializedName("Value")
    public float value;
    @SerializedName("Unit")
    public String unitType;
}

public class AccuWeather {

    public static String BaseUrl = "http://dataservice.accuweather.com/";

    public static Call<ArrayList<AccuweatherResponse>> getCurrentWeather(Location location) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        AccuweatherService service = retrofit.create(AccuweatherService.class);
        Call<ArrayList<AccuweatherResponse>> call = service.getWeatherData("y8OOUHecmJANNaQsm1RwiHyiknhGoM1v",true);
        return call;

    }
}

