package com.umar.ahmed.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.umar.ahmed.presenter.WeatherPresenter;
import com.umar.ahmed.weatherapp.R;

/**
 * Created by ahmed on 11/6/17.
 */
@SuppressLint("MissingPermission")
@SuppressWarnings("deprecation")
public class WeatherActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private WeatherPresenter presenter;
    private GoogleApiClient client;
    private static final int permissionReqCode = 21;
    private boolean isFirst;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);
        presenter = new WeatherPresenter(this);

        if (client == null) {
            client = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    public void gotWeather() {

    }

    public void noWeather() {

    }

//  location callbacks
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, permissionReqCode);
        }else {
            Location userLocation = LocationServices
                    .FusedLocationApi.getLastLocation(client);
            retrieveLocation(userLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d("TAG", "Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("TAG", "Connection failed");
    }

    @Override
    protected void onStart() {
        client.connect();
        super.onStart();
    }

    @Override
    protected void onStop() {
        client.disconnect();
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case permissionReqCode:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Location userLocation = LocationServices
                            .FusedLocationApi.getLastLocation(client);
                    retrieveLocation(userLocation);
                }else {
                    Toast.makeText(this, "Unable to get current location",
                            Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void retrieveLocation(Location userLocation) throws SecurityException{
        if (userLocation == null){
            Log.d("TAG", "Location is null creating request");
            LocationRequest request = LocationRequest.create();
            request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(10 * 1000)
                    .setFastestInterval(1000);
            LocationServices.FusedLocationApi.requestLocationUpdates
                    (client, request , this);
        }else {
            onLocationChanged(userLocation);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!isFirst){
            Log.d("WeatherActivity", "Received location longitude - " +
                    location.getLongitude() + " Latitude - " + location.getLatitude());
            presenter.getWeather(location.getLatitude(), location.getLongitude());
        }
        isFirst = true;
    }
}
