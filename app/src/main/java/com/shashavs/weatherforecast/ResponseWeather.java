package com.shashavs.weatherforecast;

import android.util.Log;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import org.json.JSONException;

public class ResponseWeather {
    private final String TAG = "ResponseWeather";

    public ResponseWeather() {
    }

    @SerializedName("name")
    private String cityName;

    @SerializedName("dt")
    private long time;

    @SerializedName("main")
    private JsonObject main;

    @SerializedName("weather")
    private JsonArray weather ;

    @SerializedName("rain")
    private JsonObject rain;

    @SerializedName("snow")
    private JsonObject snow;

    @SerializedName("wind")
    private JsonObject wind;

    public String getCity() {
        return cityName;
    }

    /*
    * Time of data calculation, unix, UTC
    */
    public long getTime() {
        return time;
    }

    public JsonObject getMain() {
        return main;
    }

    public JsonArray getWeather() {
        return weather;
    }

    public JsonElement getWeatherDescription() {
        JsonObject item = (JsonObject) weather.get(0);
        return item.get("description");
    }

    public JsonObject getWind() {
        return wind;
    }

    public JsonElement getWindSpeed () {
        return wind.get("speed");
    }

    public JsonElement getRain() {
        return rain.get("3h");
    }

    public JsonElement getSnow() {
        return snow.get("3h");
    }

    /*
    *  Unit Default: Kelvin
    */
    public float getTemperature() {
        return main.get("temp").getAsFloat() - 273.15f;
    }

    /*
    * Atmospheric pressure (on the sea level, if there is no sea_level or grnd_level data), hPa
    */
    public int getPressure () {
        return (int) (main.get("pressure").getAsFloat() * 0.75006375541921f);
    }

    public JsonElement getHumidity () {
        return main.get("humidity");
    }
}


