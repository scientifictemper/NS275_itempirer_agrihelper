package com.example.agrihelper;

import android.location.Location;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;


interface UserLocationService {
    @GET("locations/v1/cities/search?")
    Call<ArrayList<UserLocationResponse>> getUserLocationId(@Query("apikey") String apiKey, @Query("q") String cityName);
}

class UserLocationResponse {
    @SerializedName("Key")
    public String locationId;
}

public class CurrentUserLocationId {
    public static String BaseUrl = "http://dataservice.accuweather.com/";

    public static Call<ArrayList<UserLocationResponse>> getLocationId(String cityName) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BaseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        UserLocationService service = retrofit.create(UserLocationService.class);
        Call<ArrayList<UserLocationResponse>> call = service.getUserLocationId("xETxc89QjxNIzmMmNONisEjsVmHDKjGG",cityName);
        return call;

    }
}
