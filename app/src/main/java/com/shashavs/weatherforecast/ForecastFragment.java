package com.shashavs.weatherforecast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForecastFragment extends Fragment {

    private final String TAG = "ForecastFragment";

    private ImageButton btnLocation;
    private ProgressBar progressBar;

    private GoogleApiClient mClient;
    private double latitude;
    private double longitude;
    private String APIKEY;

    private TextView city;
    private TextView time;
    private TextView temperature;
    private TextView wind;
    private TextView pressure;
    private TextView description;

    public static ForecastFragment newInstance() {
        return new ForecastFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        APIKEY = getString(R.string.APIKEY);

        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        getActivity().invalidateOptionsMenu();
                    }
                    @Override
                    public void onConnectionSuspended(int i) {
                    }
                })
                .build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_forecast, container, false);

        progressBar = (ProgressBar) v.findViewById(R.id.pr_bar);
        btnLocation = (ImageButton) v.findViewById(R.id.btn_location);
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnLocation.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);

                getWeatherFromLocation();
            }
        });

        city = (TextView) v.findViewById(R.id.city);
        time = (TextView) v.findViewById(R.id.time);
        temperature = (TextView) v.findViewById(R.id.temperature);
        wind = (TextView) v.findViewById(R.id.wind);
        pressure = (TextView) v.findViewById(R.id.pressure);
        description = (TextView) v.findViewById(R.id.description);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mClient.disconnect();
    }

    private void getWeatherFromLocation() {

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            btnLocation.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);

            return;
        }

        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mClient);
        latitude = mLastLocation.getLatitude();
        longitude = mLastLocation.getLongitude();

        Call<ResponseWeather> weatherCall = RestFactory.getWeatherAPI().getForecast(latitude, longitude, APIKEY);
        weatherCall.enqueue(callback);
    }

    //async request
    Callback<ResponseWeather> callback = new Callback<ResponseWeather>() {

        @Override
        public void onResponse(Call<ResponseWeather> call, Response<ResponseWeather> response) {

            ResponseWeather respWeather = response.body();

            city.setText(getString(R.string.city) + " " + respWeather.getCity());

            Calendar mydate = Calendar.getInstance();
            mydate.setTimeInMillis(respWeather.getTime()*1000);

            time.setText(getString(R.string.time) + " "
                    + mydate.get(Calendar.DAY_OF_MONTH) + "/" + (mydate.get(Calendar.MONTH) + 1) + "/" + mydate.get(Calendar.YEAR) + " "
                    + mydate.get(Calendar.HOUR_OF_DAY) + ":" + mydate.get(Calendar.MINUTE)
            );
            temperature.setText(getString(R.string.temperature) + " " + respWeather.getTemperature() + " °C");
            wind.setText(getString(R.string.wind) + " " + respWeather.getWindSpeed() + " meter/sec");
            pressure.setText(getString(R.string.pressure) + " " + respWeather.getPressure() + " mm");
            description.setText(getString(R.string.description) + " " + respWeather.getWeatherDescription());

            btnLocation.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onFailure(Call<ResponseWeather> call, Throwable t) {
            Log.d(TAG, "onFailure: " + call.request() + "| Throwable: "+ t);
        }
    };
}
