package com.example.angelo.testgps;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity implements LocationListener {

    LocationManager locationManager;
    TextView kmh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        kmh = (TextView)findViewById(R.id.kmh);
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        getPermissions();
        requestLocation();
    }

    private void requestLocation() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    500l,
                    0.5f,
                    this);
        }
        catch(SecurityException e) {
            kmh.setText(e.getMessage());
        }
    }

    private void removeLocation() {
        try {
            locationManager.removeUpdates(this);
        }
        catch(SecurityException e) {
            kmh.setText(e.getMessage());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeLocation();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location.hasSpeed()) {
            float speed = location.getSpeed(); // m/s
            speed = speed * 60 * 60 / 1000; // km/h
            kmh.setText(new Float(speed).toString());
        }
        else
            kmh.setText("No speed");
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private void getPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1616);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
