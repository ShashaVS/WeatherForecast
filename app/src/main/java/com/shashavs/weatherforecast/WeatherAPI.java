package com.shashavs.weatherforecast;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherAPI {

//    @GET("data/2.5/forecast") // 5 day / 3 hour forecast
    @GET("data/2.5/weather")   // Current weather data
    Call<ResponseWeather> getForecast (@Query("lat") double lat, @Query("lon") double lon, @Query("APPID") String APIKEY);
}
