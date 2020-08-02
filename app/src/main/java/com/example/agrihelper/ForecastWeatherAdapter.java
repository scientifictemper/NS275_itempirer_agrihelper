package com.example.agrihelper;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agrihelper.Model.ForecastWeatherData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ForecastWeatherAdapter extends RecyclerView.Adapter<ForecastWeatherAdapter.ForecastViewHolder> {

    private Context context;
    private ArrayList<ForecastWeatherData> weatherData;

    public ForecastWeatherAdapter(ArrayList<ForecastWeatherData> weatherData, Context context) {
        this.weatherData = weatherData;
        this.context = context;
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.weather_day_item, parent,false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ForecastViewHolder holder, int position) {
        holder.maxTemp.setText(String.valueOf(weatherData.get(position).getMaxRainfall()));
        holder.minTemp.setText(String.valueOf(weatherData.get(position).getMinRainfall()));
        holder.tempText.setText(String.valueOf(weatherData.get(position).getMaxRainfall()));
        SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");
        try {
            Date date = format.parse(weatherData.get(position).getDate());
            holder.date.setText(date.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.bg.setBackgroundColor(weatherData.get(position).getColorCode());
    }

    @Override
    public int getItemCount() {
        return 4;
    }


    public class ForecastViewHolder extends RecyclerView.ViewHolder
    {
        TextView maxTemp, minTemp, tempText, date;
        View bg;

        public ForecastViewHolder(View view) {
            super(view);
            maxTemp = view.findViewById(R.id.max_temp_text_view);
            minTemp = view.findViewById(R.id.min_temp_text_view);
            tempText = view.findViewById(R.id.temp_text_view);
            date = view.findViewById(R.id.day_name_text_view);
            bg = view.findViewById(R.id.card_view);
        }
    }
}
