package com.example.angelo.PokeSpeed;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
    private View overlayButtonsView;
    private View buttonPause, buttonPlay, buttonStop;
    private PokeSpeedStats stats;
    private WindowManager.LayoutParams params;
    private WindowManager.LayoutParams paramsButtons;
    SharedPreferences prefs;
    private static boolean overlayOn;
    private static boolean servicePlay;

    public SpeedOverlayService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        servicePlay = true;
        if(prefs.getBoolean("speedOverlay", true)) {
            wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.START;
            params.y = 100;

            paramsButtons = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            //paramsButtons.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

            overlayView = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.speed_overlay, null);
            overlayButtonsView = LayoutInflater.from(getApplicationContext())
                    .inflate(R.layout.overlay_buttons, null);
            overlayButtonsView.setVisibility(View.INVISIBLE);
            buttonStop = overlayButtonsView.findViewById(R.id.overlayStop);
            buttonPause = overlayButtonsView.findViewById(R.id.overlayPause);
            buttonPlay = overlayButtonsView.findViewById(R.id.overlayPlay);
            //buttonPlay.setVisibility(View.INVISIBLE);

            speedChart = (PieChart) overlayView.findViewById(R.id.pieChart);
            ViewGroup.LayoutParams pieParams = speedChart.getLayoutParams();
            int height, width;
            switch(prefs.getString("speedOverlaySize", "medium")) {
                case "small":
                    height = width = dpiToPx(75);
                    break;
                case "medium":
                    height = width = dpiToPx(100);
                    break;
                case "large":
                    height = width = dpiToPx(125);
                    break;
                default:
                    height = width = dpiToPx(100);
                    break;
            }
            pieParams.height = height;
            pieParams.width = width;
            speedChart.setLayoutParams(pieParams);

            wm.addView(overlayView, params);
            wm.addView(overlayButtonsView, paramsButtons);
            overlayOn = true;
            showStats();

            speedChart.setOnTouchListener(new View.OnTouchListener() {
                private int initialX;
                private int initialY;
                private float initialTouchX;
                private float initialTouchY;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();
                            return true;
                        case MotionEvent.ACTION_UP:
                            overlayButtonsView.setVisibility(View.INVISIBLE);
                            if (isViewOverlapping(overlayView, buttonStop))
                                stopSpeedService();
                            else if(isViewOverlapping(overlayView, buttonPause)) {
                                playOrPauseSpeedService();
                                params.x = initialX;
                                params.y = initialY;
                                wm.updateViewLayout(overlayView, params);
                            }
                            return true;
                        case MotionEvent.ACTION_MOVE:
                            updateButtonVisibilities();
                            overlayButtonsView.setVisibility(View.VISIBLE);
                            params.x = initialX + (int) (event.getRawX() - initialTouchX);
                            params.y = initialY + (int) (event.getRawY() - initialTouchY);
                            wm.updateViewLayout(overlayView, params);
                            return true;
                    }
                    return false;
                }
            });
        }
    }

    private void stopSpeedService() {
        Intent stopSpeedServiceIntent = new Intent(this, SpeedService.class);
        stopSpeedServiceIntent.setAction(SpeedService.STOP_SERVICE_ACTION);
        startService(stopSpeedServiceIntent);
    }

    private void playOrPauseSpeedService() {
        servicePlay = !servicePlay;
        Intent speedServiceIntent = new Intent(this, SpeedService.class);
        if(servicePlay)
            speedServiceIntent.setAction(SpeedService.PLAY_SERVICE_ACTION);
        else
            speedServiceIntent.setAction(SpeedService.PAUSE_SERVICE_ACTION);
        startService(speedServiceIntent);
        //updateButtonVisibilities();
    }

    private void updateButtonVisibilities() {
        if(servicePlay) {
            buttonPlay.setVisibility(View.INVISIBLE);
            buttonPause.setVisibility(View.VISIBLE);
        }
        else {
            buttonPause.setVisibility(View.INVISIBLE);
            buttonPlay.setVisibility(View.VISIBLE);
        }
    }

    private int dpiToPx(int dpi) {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        return (int)((dpi * displayMetrics.density) + 0.5);
    }

    private boolean isViewOverlapping(View firstView, View secondView) {
        int[] firstPosition = new int[2];
        int[] secondPosition = new int[2];

        firstView.getLocationOnScreen(firstPosition);
        secondView.getLocationOnScreen(secondPosition);

        // Rect constructor parameters: left, top, right, bottom
        Rect rectFirstView = new Rect(firstPosition[0], firstPosition[1],
                firstPosition[0] + firstView.getMeasuredWidth(), firstPosition[1] + firstView.getMeasuredHeight());
        Rect rectSecondView = new Rect(secondPosition[0], secondPosition[1],
                secondPosition[0] + secondView.getMeasuredWidth(), secondPosition[1] + secondView.getMeasuredHeight());
        return rectFirstView.intersect(rectSecondView);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction() != null && intent.getAction().equals("stop")) {
            stop();
        }
        else {
            LocalBroadcastManager.getInstance(this).registerReceiver(
                    mMessagereceiver,
                    new IntentFilter("SpeedRefreshed")
            );
        }
        return START_NOT_STICKY;
    }

    private void stop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessagereceiver);
        if(overlayOn) {
            wm.removeViewImmediate(overlayView);
            overlayOn = false;
        }
        stopSelf();
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
        if(overlayView.isShown() && overlayOn) {
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
                    speedChart.setHoleColor(getResources().getColor(R.color.colorPrimaryTransparent));
                }
                else if(dSpeed >= SpeedService.SPEED_YELLOW) {
                    //centerColor.setColor(getResources().getColor(R.color.colorPokeYellow));
                    speedChart.setHoleColor(getResources().getColor(R.color.colorPokeYellowTransparent));
                    speedChart.setCenterTextColor(Color.BLACK);
                }
                else {
                    //centerColor.setColor(getResources().getColor(R.color.colorAccent));
                    speedChart.setHoleColor(getResources().getColor(R.color.colorAccentTransparent));
                }
            }
            catch(NumberFormatException e) {
                speedChart.setCenterText(lastSpeed);
                speedChart.setHoleColor(getResources().getColor(R.color.whiteTransparent));
                speedChart.setCenterTextColor(Color.BLACK);
            }
            switch(prefs.getString("speedOverlaySize", "medium")) {
                case "small":
                    speedChart.setCenterTextSize(17f);
                    break;
                case "medium":
                    speedChart.setCenterTextSize(22f);
                    break;
                case "large":
                    speedChart.setCenterTextSize(25f);
                    break;
                default:
                    speedChart.setCenterTextSize(22f);
                    break;
            }
            //speedChart.setCenterTextSize(25f);
            speedChart.setHoleRadius(90);
            speedChart.getLegend().setEnabled(false);

            speedChart.invalidate();
            wm.updateViewLayout(overlayView, params);
        }
    }
}
