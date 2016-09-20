package com.example.angelo.PokeSpeed;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;

public class SpeedOverlayService extends Service {

    private WindowManager wm;
    private PieChart speedChart;
    private View overlayView;
    private PokeSpeedStats stats;
    private WindowManager.LayoutParams params;
    SharedPreferences prefs;

    public SpeedOverlayService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.START;
        //params.x = 0;
        params.y = 100;

        overlayView = LayoutInflater.from(getApplicationContext())
                .inflate(R.layout.speed_overlay, null);
        speedChart = (PieChart) overlayView.findViewById(R.id.pieChart);
        wm.addView(overlayView, params);
        showStats();

        speedChart.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_UP:
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        wm.updateViewLayout(overlayView, params);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction() != null && intent.getAction().equals("stop")) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessagereceiver);
            wm.removeViewImmediate(overlayView);
            stopSelf();
        }
        else {
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    mMessagereceiver,
                    new IntentFilter("SpeedRefreshed")
            );
        }
        return START_NOT_STICKY;
    }



    private BroadcastReceiver mMessagereceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("SpeedRefreshed"))
                showStats();
        }
    };

    @Override
    public void onDestroy() {
        stopSelf();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getSpeed() {
        String unit = prefs.getBoolean("imperial", false) ? "mi/h" : "km/h";
        if(overlayView.isShown()) {
            String lastMessage = SpeedService.getLastSpeed();
            if (lastMessage != null) {
                //prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                try {
                    Float.parseFloat(lastMessage);
                    return String.format("%s %s", lastMessage, unit);
                } catch (NumberFormatException e) {
                    return lastMessage;
                }
            }
        }
        return "0.00 " + unit;
    }

    private void showStats() {
        if(overlayView.isShown()) {
            String units = prefs.getBoolean("imperial", false) ? "mi" : "km";

            float fDistanceValid = 0;
            float fdistanceCovered = 0;

            stats = MainActivity.stats;
            if (stats != null) {
                speedChart.setNoDataText("Turn on GO Speed and start moving to see some stats!");
                double[] statsValues = stats.getStats();
                fDistanceValid = Double.valueOf(statsValues[0]).floatValue();
                fdistanceCovered = Double.valueOf(statsValues[1]).floatValue();
            }

            List<PieEntry> pieEntries = new ArrayList<>();
            pieEntries.add(new PieEntry(fDistanceValid));
//            pieEntries.add(new PieEntry(fDistanceValid,
//                    String.format(l, "%.3f", fDistanceValid) + units));
            if (StatsFragment.significantDifference(fdistanceCovered, fDistanceValid))
                pieEntries.add(new PieEntry(fdistanceCovered - fDistanceValid));
//                pieEntries.add(new PieEntry(fdistanceCovered - fDistanceValid,
//                        String.format(l, "%.3f", fdistanceCovered - fDistanceValid) + units));

            PieDataSet pieSet = new PieDataSet(pieEntries, "");
            pieSet.setColors(new int[]{getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary)});
            pieSet.setSliceSpace(1);
            pieSet.setDrawValues(false);

            PieData pieData = new PieData(pieSet);
            speedChart.setData(pieData);
            speedChart.setDescription("");
            speedChart.setDescriptionTextSize(0f);

            String lastSpeed = SpeedService.getLastSpeed();
            speedChart.setCenterTextColor(Color.WHITE);
            try {
                Double dSpeed = Double.valueOf(lastSpeed);
                String speed = String.format("%.2f", dSpeed);
                speedChart.setCenterText(String.format("%s %n%s/h", speed, units));
                if(dSpeed >= SpeedService.SPEED_RED) {
                    //centerColor.setColor(getResources().getColor(R.color.colorPrimaryDark));
                    speedChart.setHoleColor(getResources().getColor(R.color.colorPrimaryDark));
                }
                else if(dSpeed >= SpeedService.SPEED_YELLOW) {
                    //centerColor.setColor(getResources().getColor(R.color.colorPokeYellow));
                    speedChart.setHoleColor(getResources().getColor(R.color.colorPokeYellow));
                    speedChart.setCenterTextColor(Color.BLACK);
                }
                else {
                    //centerColor.setColor(getResources().getColor(R.color.colorAccent));
                    speedChart.setHoleColor(getResources().getColor(R.color.colorAccent));
                }
            }
            catch(NumberFormatException e) {
                speedChart.setCenterText(lastSpeed);
                speedChart.setHoleColor(Color.WHITE);
                speedChart.setCenterTextColor(Color.BLACK);
            }
            speedChart.setCenterTextSize(25f);
            speedChart.setHoleRadius(90);
            speedChart.getLegend().setEnabled(false);

            speedChart.invalidate();
            wm.updateViewLayout(overlayView, params);
        }
    }
}