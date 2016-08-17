package com.example.angelo.testgps;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends Activity {

    private ToggleButton speedToggle;
    private Button btn_stats;
    private static boolean toggledOff = true;
    private static boolean mBound = false;
    private SpeedService speedService;
    public static PokeSpeedStats stats;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        stats = null;
        speedToggle = (ToggleButton)findViewById(R.id.toggleSpeedService);
        speedToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(toggledOff)
                    startSpeedService();
                else
                    stopSpeedService();
            }
        });
        btn_stats = (Button)findViewById(R.id.button_stats);
        btn_stats.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(stats != null) {
                    double[] statsValues = stats.getStats();
                    TextView distanceValid = (TextView)findViewById(R.id.validDistance);
                    TextView distanceCovered = (TextView)findViewById(R.id.distanceCovered);
                    TextView percentDistance = (TextView)findViewById(R.id.percentDistance);
                    TextView averageSpeed = (TextView)findViewById(R.id.averageSpeed);
                    TextView maxSpeed = (TextView)findViewById(R.id.maxSpeed);

                    distanceValid.setText(new Double(statsValues[0]).toString());
                    distanceCovered.setText(new Double(statsValues[1]).toString());
                    percentDistance.setText(new Double(statsValues[2]).toString());
                    averageSpeed.setText(new Double(statsValues[3]).toString());
                    maxSpeed.setText(new Double(statsValues[4]).toString());
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        speedToggle.setChecked(!toggledOff);
        if(!toggledOff && !MainActivity.mBound)
            _bindService();
        //speedService.test();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //int t = speedService.test();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (MainActivity.mBound) {
            _unbindService();
        }
    }

    private void stopSpeedService() {
        if(MainActivity.mBound) {
            _unbindService();
        }
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

    private void _bindService() {
        bindService(new Intent(this, SpeedService.class), this.mConnection, Context.BIND_AUTO_CREATE);
        MainActivity.mBound = true;
    }

    private void _unbindService() {
        unbindService(this.mConnection);
        MainActivity.mBound = false;
    }

    private void _startSpeedService() {
        startService(new Intent(this, SpeedService.class));
        _bindService();
        toggledOff = false;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            SpeedService.LocalBinder binder = (SpeedService.LocalBinder) service;
            speedService = binder.getService();
            stats = speedService.getStatsObj();
            MainActivity.mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            MainActivity.mBound = false;
            speedService = null;
        }
    };
}
