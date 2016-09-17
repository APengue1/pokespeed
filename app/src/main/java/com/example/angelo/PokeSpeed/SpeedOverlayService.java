package com.example.angelo.PokeSpeed;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.zip.Inflater;

public class SpeedOverlayService extends Service {

    private WindowManager wm;
    private PieChart speedOverlay;
    private PokeSpeedStats stats;
    private WindowManager.LayoutParams params;

    public SpeedOverlayService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.CENTER_VERTICAL;
        params.x = 0;
        params.y = 100;

        speedOverlay = new PieChart(this);
        wm.addView(speedOverlay, params);
        showStats();

        speedOverlay.setOnTouchListener(new View.OnTouchListener() {
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
                        wm.updateViewLayout(speedOverlay, params);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction() != null && intent.getAction().equals("stop")) {
            wm.removeViewImmediate(speedOverlay);
            stopSelf();
        }
        return START_NOT_STICKY;
    }



    private BroadcastReceiver mMessagereceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("StatsRefreshed"))
                showStats();
            else if(intent.getAction().equals("ServiceStatusChanged")) {
                if(intent.getBooleanExtra("status", true))
                    ;//showResetButton(true);
                else
                    ;//showResetButton(false);
            }
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

    private void showStats() {
        if(speedOverlay.isShown()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

            float fDistanceValid = 0;
            float fdistanceCovered = 0;
            float faverageSpeed = 0;
            float fmaxSpeed = 0;

            stats = MainActivity.stats;
            if (stats != null) {
                speedOverlay.setNoDataText("Turn on GO Speed and start moving to see some stats!");
                double[] statsValues = stats.getStats();
                fDistanceValid = Double.valueOf(statsValues[0]).floatValue();
                fdistanceCovered = Double.valueOf(statsValues[1]).floatValue();
                faverageSpeed = Double.valueOf(statsValues[3]).floatValue();
                fmaxSpeed = Double.valueOf(statsValues[4]).floatValue();
            }
            String units = prefs.getBoolean("imperial", false) ? "mi" : "km";
            Locale l = Locale.getDefault();

            List<PieEntry> pieEntries = new ArrayList<>();
            pieEntries.add(new PieEntry(fDistanceValid,
                    String.format(l, "%.3f", fDistanceValid) + units));
            if (StatsFragment.significantDifference(fdistanceCovered, fDistanceValid))
                pieEntries.add(new PieEntry(fdistanceCovered - fDistanceValid,
                        String.format(l, "%.3f", fdistanceCovered - fDistanceValid) + units));

            PieDataSet pieSet = new PieDataSet(pieEntries, "Distances");
            pieSet.setColors(new int[]{getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary)});
            pieSet.setSliceSpace(2);

            PieData pieData = new PieData(pieSet);
            pieData.setValueFormatter(new PercentFormatter());
            pieData.setValueTextSize(20f);
            pieData.setValueTextColor(Color.WHITE);
            speedOverlay.setData(pieData);

            speedOverlay.setUsePercentValues(true);
            speedOverlay.setDescription("Distance Summary");
            speedOverlay.setDescriptionTextSize(25f);
            speedOverlay.setCenterText(
                    String.format("Avg Speed: %.2f %s%nMax Speed: %.2f %s",
                            faverageSpeed, units + "/h",
                            fmaxSpeed, units + "/h"));
            speedOverlay.setCenterTextSize(15f);
            speedOverlay.setHoleRadius(55);
            Legend pieLegend = speedOverlay.getLegend();
            pieLegend.setCustom(
                    new int[]{getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary)},
                    new String[]{"Valid", "Invalid"}
            );
            pieLegend.setPosition(Legend.LegendPosition.BELOW_CHART_RIGHT);
            pieLegend.setForm(Legend.LegendForm.CIRCLE);
            pieLegend.setTextSize(15f);
            speedOverlay.invalidate();
            wm.updateViewLayout(speedOverlay, params);
        }
    }
}
