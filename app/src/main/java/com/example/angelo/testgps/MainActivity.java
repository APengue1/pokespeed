package com.example.angelo.testgps;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {


    private TextView kmh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        kmh = (TextView)findViewById(R.id.kmh);
        getPermissions();
    }

    @Override
    protected void onResume() {
        getPermissions();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void setKm(Float speed) {
        String speedStr = speed.toString();
        kmh.setText(speedStr);
    }

    private void startSpeedService() {
        startService(new Intent(this, SpeedService.class));
    }

    private void getPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
            }
        else {
            startSpeedService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] != -1)
            startSpeedService();

    }
}
