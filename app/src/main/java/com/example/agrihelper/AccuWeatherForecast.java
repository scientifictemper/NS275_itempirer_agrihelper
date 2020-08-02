package com.example.agrihelper;

import android.location.Location;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

interface AccuweatherForecastService {
    @GET("forecasts/v1/daily/5day/2801585?")
    Call<AccuweatherForecastResponse> getWeatherData(@Query("apikey") String apiKey, @Query("details") boolean details);
}

class AccuweatherForecastResponse {
    @SerializedName("DailyForecasts")
    public ArrayList<Data> data = new ArrayList<>();
}

class Data {
    @SerializedName("Date")
    public String date;
    @SerializedName("Temperature")
    public TemperatureData temperature;
}

class TemperatureData {
    @SerializedName("Minimum")
    public Metric minimum;
    @SerializedName("Maximum")
    public Metric maximum;
}

public class AccuWeatherForecast {

    public static String BaseUrl = "http://dataservice.accuweather.com/";

    public static Call<AccuweatherForecastResponse> getForecastData(Location location) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        AccuweatherForecastService service = retrofit.create(AccuweatherForecastService.class);
        return service.getWeatherData("y8OOUHecmJANNaQsm1RwiHyiknhGoM1v",true);

    }
}

