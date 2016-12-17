package com.shashavs.weatherforecast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

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

    private final String KEY_CITY = "city";
    private final String KEY_TIME = "time";
    private final String KEY_TEMP = "temperature";
    private final String KEY_WIND = "wind";
    private final String KEY_PRESS = "pressure";
    private final String KEY_DESC = "description";

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

                if(!isOnline()) {
                    setForecastFromPref();
                    Toast.makeText(getContext(), getString(R.string.error_msg), Toast.LENGTH_SHORT).show();
                } else {
                    btnLocation.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                    getWeatherFromLocation();
                }
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

            if(respWeather != null) {
                QueryPreferences.setString(getActivity(), KEY_CITY, respWeather.getCity());
                Calendar mDate = Calendar.getInstance();
                mDate.setTimeInMillis(respWeather.getTime()*1000);
                QueryPreferences.setString(getActivity(), KEY_TIME,
                        mDate.get(Calendar.DAY_OF_MONTH) + "/" + (mDate.get(Calendar.MONTH) + 1) + "/" + mDate.get(Calendar.YEAR) + " "
                                + mDate.get(Calendar.HOUR_OF_DAY) + ":" + mDate.get(Calendar.MINUTE));
                QueryPreferences.setString(getActivity(), KEY_TEMP, respWeather.getTemperature() + " °C");
                QueryPreferences.setString(getActivity(), KEY_WIND, respWeather.getWindSpeed() + " meter/sec");
                QueryPreferences.setString(getActivity(), KEY_PRESS, respWeather.getPressure() + " mm");
                QueryPreferences.setString(getActivity(), KEY_DESC, respWeather.getWeatherDescription());

                Toast.makeText(getContext(), getString(R.string.success_msg), Toast.LENGTH_SHORT).show();
            }

            setForecastFromPref();
            btnLocation.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onFailure(Call<ResponseWeather> call, Throwable t) {
            Log.d(TAG, "onFailure: " + call.request() + "| Throwable: "+ t);
            Toast.makeText(getActivity(), getString(R.string.error_msg), Toast.LENGTH_SHORT).show();

            setForecastFromPref();
            btnLocation.setEnabled(true);
            progressBar.setVisibility(View.INVISIBLE);
        }
    };

    private void setForecastFromPref() {
        city.setText(getString(R.string.city) + " " + QueryPreferences.getSrting(getActivity(), KEY_CITY));
        time.setText(getString(R.string.time) + " " + QueryPreferences.getSrting(getActivity(), KEY_TIME));
        temperature.setText(getString(R.string.temperature) + " " + QueryPreferences.getSrting(getActivity(), KEY_TEMP));
        wind.setText(getString(R.string.wind) + " " + QueryPreferences.getSrting(getActivity(), KEY_WIND));
        pressure.setText(getString(R.string.pressure) + " " + QueryPreferences.getSrting(getActivity(), KEY_PRESS));
        description.setText(getString(R.string.description) + " " + QueryPreferences.getSrting(getActivity(), KEY_DESC));
    }

    private boolean isOnline(){
        ConnectivityManager connMgr = (ConnectivityManager) getActivity().getSystemService(getActivity().CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }
}
