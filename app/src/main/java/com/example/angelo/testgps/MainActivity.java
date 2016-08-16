package com.example.angelo.testgps;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

    private ToggleButton speedToggle;
    private static boolean toggledOff = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        speedToggle = (ToggleButton)findViewById(R.id.toggleSpeedService);
        speedToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(toggledOff)
                    startSpeedService();
                else
                    stopSpeedService();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        speedToggle.setChecked(!toggledOff);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void stopSpeedService() {
        stopService(new Intent(this, SpeedService.class));
        toggledOff = true;
    }

    private void startSpeedService() {
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
            _startSpeedService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] != -1) {
            _startSpeedService();
        }

    }

    private void _startSpeedService() {
        startService(new Intent(this, SpeedService.class));
        toggledOff = false;
    }
}
