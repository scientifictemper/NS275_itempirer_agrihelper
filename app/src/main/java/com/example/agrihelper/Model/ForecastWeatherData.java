package com.example.agrihelper.Model;

public class ForecastWeatherData {
    private String date;
    private float minRainfall;
    private float maxRainfall;
    private int colorCode;

    public ForecastWeatherData() {
    }

    public ForecastWeatherData(String date, float minRainfall, float maxRainfall, int colorCode) {
        this.date = date;
        this.minRainfall = minRainfall;
        this.maxRainfall = maxRainfall;
        this.colorCode = colorCode;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getMinRainfall() {
        return minRainfall;
    }

    public void setMinRainfall(float minRainfall) {
        this.minRainfall = minRainfall;
    }

    public float getMaxRainfall() {
        return maxRainfall;
    }

    public void setMaxRainfall(float maxRainfall) {
        this.maxRainfall = maxRainfall;
    }

    public int getColorCode() {
        return colorCode;
    }

    public void setColorCode(int colorCode) {
        this.colorCode = colorCode;
    }
}
