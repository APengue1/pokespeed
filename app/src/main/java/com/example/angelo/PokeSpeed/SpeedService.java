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
import android.location.LocationProvider;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;


public class SpeedService extends Service implements LocationListener{

    private SharedPreferences prefs;
    private double SPEED_RED, SPEED_YELLOW;

    private NotificationCompat.Builder mBuilder;
    private LocationManager locationManager;
    //private Location lastLocation;
    //private Long lastTime;
    private NotificationManagerCompat notificationManager;
    private static final long [] VIBRATE_YELLOW = new long[]{100, 100};
    private static final long[] VIBRATE_RED = new long[]{500, 500};
    private static final int NOTIFY_ID = 1;
    private static final String STOP_SERVICE_ACTION = "Stop Service Action";
    private static final String PAUSE_SERVICE_ACTION = "Pause Service Action";
    private static final String PLAY_SERVICE_ACTION = "Play Service Action";
    private final IBinder mBinder = new LocalBinder();
    private PokeSpeedStats stats;
    private static String lastSpeed;
    private int lowSpeedCount;
    static boolean serviceOn = false;

    private static final String LOCATION_WAIT = "Waiting for location...";
    private static final String TURN_ON_GPS = "Turn on gps";

    @Override
    public void onCreate() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SPEED_RED = Double.parseDouble(prefs.getString("maxSpeed", "10.5"));
        SPEED_YELLOW = SPEED_RED - 3;
        stats =  new PokeSpeedStats(prefs);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        notificationManager = NotificationManagerCompat.from(this);
        lastSpeed = "0.00";
        lowSpeedCount = 0;

        sendServiceStatusChanged(false);
        super.onCreate();
    }

    private void addPauseAction(NotificationCompat.Builder mBuilder) {
        Intent pauseIntent = new Intent(this, SpeedService.class);
        pauseIntent.setAction(SpeedService.PAUSE_SERVICE_ACTION);
        PendingIntent resultPauseIntent= PendingIntent.getService(
                this,
                0,
                pauseIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );
        mBuilder.addAction(R.drawable.ic_pause_black_24dp, "Pause", resultPauseIntent);
    }

    private void addPlayAction(NotificationCompat.Builder mBuilder) {
        Intent playIntent = new Intent(this, SpeedService.class);
        playIntent.setAction(SpeedService.PLAY_SERVICE_ACTION);
        PendingIntent resultPlayIntent= PendingIntent.getService(
                this,
                0,
                playIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );
        mBuilder.addAction(R.drawable.ic_play_arrow_black_24dp, "Play", resultPlayIntent);
    }

    private NotificationCompat.Builder getDefaultBuilder() {
        Intent stopIntent = new Intent(this, SpeedService.class);
        stopIntent.setAction(SpeedService.STOP_SERVICE_ACTION);
        PendingIntent resultStopIntent= PendingIntent.getService(
                this,
                0,
                stopIntent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );
        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        setLastSpeed(LOCATION_WAIT);
        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_pokespeed_notification)
                .setContentTitle("PokeSpeed")
                .addAction(R.drawable.ic_clear_black_24dp, "Stop", resultStopIntent)
                .setContentIntent(resultPendingIntent);
    }

    public class LocalBinder extends Binder {
        SpeedService getService() {
            return SpeedService.this;
        }
    }

    public PokeSpeedStats getStatsObj() {
        return stats;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendServiceStatusChanged(true);
        if(intent.getAction() != null) {
            if (intent.getAction().equals(SpeedService.STOP_SERVICE_ACTION)) {
                removeLocation();
                sendServiceStatusChanged(false);
                LocalBroadcastManager.getInstance(this).sendBroadcast(
                        new Intent("SpeedServiceStop").putExtra("SpeedServiceStop", true)
                );
                setLastSpeed("");
                stopForeground(true);
                stopSelf();
            }
            else if(intent.getAction().equals(SpeedService.PAUSE_SERVICE_ACTION)) {
                removeLocation();
                mBuilder = getDefaultBuilder();
                mBuilder.setContentText("Paused");
                addPlayAction(mBuilder);
                setLastSpeed("Paused");
                initNotification();
            }
            else if(intent.getAction().equals(SpeedService.PLAY_SERVICE_ACTION)) {
                requestLocation();
                mBuilder = getDefaultBuilder();
                mBuilder.setContentText(LOCATION_WAIT);
                addPauseAction(mBuilder);
                setLastSpeed(LOCATION_WAIT);
                initNotification();
            }
        }
        else {
            mBuilder = getDefaultBuilder();
            mBuilder.setContentText(LOCATION_WAIT);
            addPauseAction(mBuilder);
            initNotification();
            requestLocation();
        }
        return START_NOT_STICKY;
    }

    private void initNotification() {
        startForeground(SpeedService.NOTIFY_ID, mBuilder.build());
    }

    private void requestLocation() {
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    500L,
                    0F,
                    this);
        }
        catch(IllegalArgumentException e) {
            setLastSpeed(TURN_ON_GPS);
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
        return lastSpeed;
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location.getAccuracy() <= 15 && location.hasSpeed()) {
            Float speed = location.getSpeed(); // m/s
            speed = speed * 60 * 60 / 1000; // km/h
            setSpeed(speed);
            if(speed < 0.5)
                if(lowSpeedCount >= 2)
                    return;
                else
                    lowSpeedCount++;
            else
                lowSpeedCount = 0;
            stats.giveLocation(location, speed);
            LocalBroadcastManager.getInstance(this).sendBroadcast(
                    new Intent("StatsRefreshed")
            );
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
        if(s.equals(LocationManager.GPS_PROVIDER) &&
                (i == LocationProvider.OUT_OF_SERVICE || i == LocationProvider.TEMPORARILY_UNAVAILABLE)) {
            setLastSpeed(LOCATION_WAIT);
        }
    }

    @Override
    public void onProviderEnabled(String s) {
        if(s.equals(LocationManager.GPS_PROVIDER)) {
            setLastSpeed(LOCATION_WAIT);
        }
    }

    @Override
    public void onProviderDisabled(String s) {
        if(s.equals(LocationManager.GPS_PROVIDER)) {
            setLastSpeed(TURN_ON_GPS);
        }
    }

    private void setInaccurate() {
        mBuilder.setVibrate(null);
        mBuilder.setContentTitle("PokeSpeed");
        mBuilder.setContentText(LOCATION_WAIT);
        mBuilder.setColor(Color.TRANSPARENT);
        notificationManager.notify(SpeedService.NOTIFY_ID, mBuilder.build());
        setLastSpeed(LOCATION_WAIT);
    }

    private void setSpeed(Float speed) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean bVibrate = prefs.getBoolean("vibrate", true);
        String unit = prefs.getBoolean("imperial", false) ? "mi/h" : "km/h";
        Double fSpeed = getSpeedForLocale(speed);
        int argb;
        String contentText;
        if(fSpeed >= SPEED_RED) {
            argb = Color.RED;
            mBuilder.setSmallIcon(R.drawable.ic_stat_stop);
            contentText = "Difficult to hatch eggs at this speed";
            if(bVibrate)
                mBuilder.setVibrate(SpeedService.VIBRATE_RED);
        }
        else if(fSpeed >= SPEED_YELLOW) {
            argb = getResources().getColor(R.color.colorPokeYellow);
            mBuilder.setSmallIcon(R.drawable.ic_stat_slow);
            contentText = "Getting close to the speed limit..";
            if(bVibrate)
                mBuilder.setVibrate(SpeedService.VIBRATE_YELLOW);
        }
        else {
            mBuilder.setSmallIcon(R.drawable.ic_pokespeed_notification);
            argb = getResources().getColor(R.color.colorAccent);
            contentText = "Well under the speed limit :)";
            mBuilder.setVibrate(null);
        }
        mBuilder.setContentTitle(String.format("Pokespeed is %.2f %s", fSpeed, unit));
        mBuilder.setContentText(contentText);
        mBuilder.setColor(argb);
        notificationManager.notify(SpeedService.NOTIFY_ID, mBuilder.build());
        setLastSpeed(String.format("%.2f", fSpeed));
    }

    private double getSpeedForLocale(double i) {
        boolean bImperial = prefs.getBoolean("imperial", false);
        if(bImperial)
            i *= 0.621371f;
        return i;
    }

    @Override
    public void onDestroy() {
        removeLocation();
        sendServiceStatusChanged(false);
        stopSelf();
        super.onDestroy();
    }

    private void setLastSpeed(String speed) {
        lastSpeed = speed;
        LocalBroadcastManager.getInstance(this).sendBroadcast(
                new Intent("SpeedRefreshed").putExtra("SpeedRefreshed", true)
        );
    }

    private void sendServiceStatusChanged(boolean status) {
        serviceOn = status;
        LocalBroadcastManager.getInstance(this).sendBroadcast(
                new Intent("ServiceStatusChanged").putExtra("status", status)
        );
    }

}
