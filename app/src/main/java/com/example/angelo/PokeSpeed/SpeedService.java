package com.example.angelo.PokeSpeed;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;


public class SpeedService extends Service implements LocationListener{

    static final int SPEED_RED = 17, SPEED_YELLOW = 14;

    private SharedPreferences prefs;
    private NotificationCompat.Builder mBuilder;
    private LocationManager locationManager;
    //private Location lastLocation;
    //private Long lastTime;
    private NotificationManagerCompat notificationManager;
    private static final long [] VIBRATE_YELLOW = new long[]{100, 100};
    private static final long[] VIBRATE_RED = new long[]{500, 500};
    private static final int NOTIFY_ID = 1;
    private static final String STOP_SERVICE_ACTION = "Stop Service Action";
    private final IBinder mBinder = new LocalBinder();
    private static final PokeSpeedStats STATS = new PokeSpeedStats();
    private static Integer lastSpeed;

    @Override
    public void onCreate() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        notificationManager = NotificationManagerCompat.from(this);
        lastSpeed = 0;
        initNotification();
        requestLocation();
        super.onCreate();
    }

    public class LocalBinder extends Binder {
        SpeedService getService() {
            return SpeedService.this;
        }
    }

    public PokeSpeedStats getStatsObj() {
        return STATS;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction() != null &&
                intent.getAction().compareTo(SpeedService.STOP_SERVICE_ACTION) == 0) {
            stopForeground(true);
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    private void initNotification() {
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent stopIntent = new Intent(this, MainActivity.class);
        stopIntent.setAction(SpeedService.STOP_SERVICE_ACTION);
        PendingIntent resultStopIntent= PendingIntent.getService(
                this,
                0,
                stopIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );

        mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.poke_speed)
                .setContentTitle("Pokespeed")
                //.addAction(android.R.drawable.ic_media_pause, "Stop", resultStopIntent)
                .setContentIntent(resultPendingIntent);
        startForeground(SpeedService.NOTIFY_ID, mBuilder.build());
    }

    private void requestLocation() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    500L,
                    0.5F,
                    this);
        }
        catch(SecurityException e) {
        }
    }

    private void removeLocation() {
        try {
            locationManager.removeUpdates(this);
        }
        catch(SecurityException e) {
        }
    }

    public static String getLastSpeed() {
        return lastSpeed.toString();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location.getAccuracy() <= 10 && location.hasSpeed()) {
            Float speed = location.getSpeed(); // m/s
            speed = speed * 60 * 60 / 1000; // km/h
            setSpeed(speed);
            STATS.giveLocation(location, speed);
//            } else if (this.lastLocation == null || this.lastTime == null) {
//                this.lastLocation = location;
//                this.lastTime = location.getTime(); // ms
//            } else {
//                float distanceCovered = location.distanceTo(this.lastLocation); // m
//                long timeElapsed = location.getTime() - this.lastTime; //ms
//                this.lastLocation = location;
//                this.lastTime = location.getTime();
//                speed = distanceCovered / timeElapsed; // m/ms
//                speed = speed * 60 * 60 * 60 / 1000; // km/h
//                setSpeed(speed);
        }
        else {
            setInaccurate();
        }
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

    private void setInaccurate() {
        mBuilder.setVibrate(null);
        mBuilder.setContentTitle("Waiting for accurate location...");
        mBuilder.setColor(Color.TRANSPARENT);
        notificationManager.notify(SpeedService.NOTIFY_ID, mBuilder.build());
    }

    private void setSpeed(Float speed) {
        boolean bVibrate = prefs.getBoolean("vibrate", true);
        Integer speedInt = getSpeedForLocale(speed);
        int argb;
        if(speedInt > SPEED_RED) {
            argb = Color.RED;
            if(bVibrate)
                mBuilder.setVibrate(SpeedService.VIBRATE_RED);
        }
        else if(speedInt > SPEED_YELLOW) {
            argb = Color.YELLOW;
            if(bVibrate)
                mBuilder.setVibrate(SpeedService.VIBRATE_YELLOW);
        }
        else {
            argb = Color.GREEN;
            mBuilder.setVibrate(null);
        }
        mBuilder.setContentTitle(speedInt.toString());
        mBuilder.setColor(argb);
        notificationManager.notify(SpeedService.NOTIFY_ID, mBuilder.build());
        lastSpeed = speedInt;
    }

    private int getSpeedForLocale(float i) {
        boolean bImperial = prefs.getBoolean("imperial", false);
        if(bImperial)
            return Math.round(i * 0.621371f);
        else
            return Math.round(i);
    }

    @Override
    public void onDestroy() {
        removeLocation();
        super.onDestroy();
    }

}
