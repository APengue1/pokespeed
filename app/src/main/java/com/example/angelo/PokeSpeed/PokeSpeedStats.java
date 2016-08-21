package com.example.angelo.PokeSpeed;

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

    public double[] getStats() {
        double percentDistanceValid = 0, averageSpeed = 0;
        if(distanceCovered != 0)
            percentDistanceValid = distanceValid / distanceCovered;
        if(numberRecordings != 0)
            averageSpeed = speedTotal / numberRecordings;
        return new double[] {
                distanceValid,
                distanceCovered,
                percentDistanceValid,
                averageSpeed,
                maxSpeed
        };
    }
}
