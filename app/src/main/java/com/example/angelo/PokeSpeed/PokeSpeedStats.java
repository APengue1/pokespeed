package com.example.angelo.PokeSpeed;

import android.content.SharedPreferences;
import android.location.Location;

public class PokeSpeedStats {

    private double distanceCovered;
    private double distanceValid;
    private double speedTotal;
    private double maxSpeed;
    private int numberRecordings;
    private Location lastLocation;

    public PokeSpeedStats() {
        distanceCovered = 0;
        distanceValid = 0;
        numberRecordings = 0;
        speedTotal = 0;
        maxSpeed = 0;
        lastLocation = null;
    }

    public void giveLocation(Location location, Float speed) {
        if(lastLocation == null)
            lastLocation = location;
        else {
            double distance = lastLocation.distanceTo(location) / 1000;
            distanceCovered += distance;
            if(speed != null) {
                speedTotal += speed;
                if(speed > maxSpeed)
                    maxSpeed = speed;
                if(speed < SpeedService.SPEED_RED)
                    distanceValid += distance;
            }
            numberRecordings++;
            lastLocation = location;
        }
    }

    public double[] getStats(SharedPreferences prefs) {
        double percentDistanceValid = 0,
                averageSpeed = 0,
                _distanceValid = distanceValid,
                _distanceCovered = distanceCovered,
                _maxSpeed = maxSpeed;
        if(_distanceCovered != 0)
            percentDistanceValid = _distanceValid / _distanceCovered;
        if(numberRecordings != 0)
            averageSpeed = speedTotal / numberRecordings;
        if (prefs.getBoolean("imperial", false)) {
            _distanceValid *= 0.621371;
            _distanceCovered *= 0.621371;
            averageSpeed *= 0.621371;
            _maxSpeed *= 0.621371;
        }
        return new double[] {
                _distanceValid,
                _distanceCovered,
                percentDistanceValid,
                averageSpeed,
                _maxSpeed
        };
    }
}
