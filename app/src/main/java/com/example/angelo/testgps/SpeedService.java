package com.example.angelo.testgps;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;

public class SpeedService extends Service {

    private final class speedRunnable implements Runnable, LocationListener {

        private LocationManager locationManager;
        private Location lastLocation;
        private Long lastTime;
        private static final int NOTIFY_ID = 1;

        @Override
        public void run() {

        }

        private void requestLocation() {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        500l,
                        0.5f,
                        this);
            }
            catch(SecurityException e) {
                //kmh.setText(e.getMessage());
            }
        }

        private void removeLocation() {
            try {
                locationManager.removeUpdates(this);
            }
            catch(SecurityException e) {
                // kmh.setText(e.getMessage());
            }
        }

        @Override
        public void onLocationChanged(Location location) {
            if(location.hasSpeed()) {
                Float speed = location.getSpeed(); // m/s
                speed = speed * 60 * 60 / 1000; // km/h
                //kmh.setText(speed.toString());
                setSpeed(speed);
            }
            else if(this.lastLocation == null || this.lastTime == null) {
                this.lastLocation = location;
                this.lastTime = location.getTime(); // ms
            }
            else {
                float distanceCovered = location.distanceTo(this.lastLocation); // m
                long timeElapsed = location.getTime() - this.lastTime; //ms
                this.lastLocation = location;
                this.lastTime = location.getTime();
                Float speed = distanceCovered / timeElapsed; // m/ms
                speed = speed * 60 * 60 * 60 / 1000; // km/h
                //kmh.setText(speed.toString());
                setSpeed(speed);
            }
        }

        private void setSpeed(Float speed) {
            String speedStr = speed.toString();
//        kmh.setText(speedStr);
//        mBuilder.setContentText(speedStr);
//        notificationManager.notify(this.NOTIFY_ID, mBuilder.build());
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
    }

    public SpeedService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return START_NOT_STICKY;
        //return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
