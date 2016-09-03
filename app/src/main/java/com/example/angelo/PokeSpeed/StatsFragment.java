package com.example.angelo.PokeSpeed;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StatsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatsFragment extends Fragment {

    private static PokeSpeedStats stats;
    private OnFragmentInteractionListener mListener;
    private SharedPreferences prefs;
    private View lastView;
    private Button btn_resetStats;

    public StatsFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment StatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StatsFragment newInstance() {
        StatsFragment fragment = new StatsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(
                R.layout.fragment_stats, container, false);
        stats = MainActivity.stats;
        showStats(view, false);
        lastView = view;
        refreshStats(view);
        btn_resetStats = (Button)view.findViewById(R.id.btn_statsReset);
        btn_resetStats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetStats(view);
            }
        });
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onResume() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessagereceiver,
                new IntentFilter("StatsRefreshed")
        );
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessagereceiver,
                new IntentFilter("ServiceStatusChanged")
        );
        showStats(getView(), false);
        super.onResume();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessagereceiver);
        super.onPause();
    }

    private BroadcastReceiver mMessagereceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals("StatsRefreshed"))
                showStats(lastView, false);
            else if(intent.getAction().equals("ServiceStatusChanged")) {
                if(intent.getBooleanExtra("status", true))
                    ;//showResetButton(true);
                else
                    ;//showResetButton(false);
            }
        }
    };

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void showStats(View view, boolean reset) {
        if(isAdded()) {
            PieChart pie = (PieChart) view.findViewById(R.id.pieChart);

            float fDistanceValid = 0;
            float fdistanceCovered = 0;
            Integer fpercentDistance = 0;
            float faverageSpeed = 0;
            float fmaxSpeed = 0;

            if (stats != null && !reset) {
                pie.setNoDataText("Turn on PokeSpeed and start moving to see some stats!");
                double[] statsValues = stats.getStats();
                fDistanceValid = Double.valueOf(statsValues[0]).floatValue();
                fdistanceCovered = Double.valueOf(statsValues[1]).floatValue();
                fpercentDistance = Double.valueOf(statsValues[2] * 100).intValue();
                faverageSpeed = Double.valueOf(statsValues[3]).floatValue();
                fmaxSpeed = Double.valueOf(statsValues[4]).floatValue();
            }
            String units = prefs.getBoolean("imperial", false) ? "mi" : "km";

//        double[] statsValues = stats.getStats();
//            TextView distanceValid = (TextView)view.findViewById(R.id.validDistance);
//            TextView distanceCovered = (TextView)view.findViewById(R.id.distanceCovered);
//            TextView percentDistance = (TextView)view.findViewById(R.id.percentDistance);
//            TextView averageSpeed = (TextView)view.findViewById(R.id.averageSpeed);
//            TextView maxSpeed = (TextView)view.findViewById(R.id.maxSpeed);


            Locale l = Locale.getDefault();
//            distanceValid.setText(String.format(l,"%.2f", fDistanceValid));
//            distanceCovered.setText(String.format(l,"%.2f", fdistanceCovered));
//            percentDistance.setText(String.format(l,"%d", fpercentDistance));
//            averageSpeed.setText(String.format(l,"%d", Double.valueOf(faverageSpeed).intValue()));
//            maxSpeed.setText(String.format(l,"%d", fmaxSpeed));

            List<PieEntry> pieEntries = new ArrayList<>();
            pieEntries.add(new PieEntry(fDistanceValid,
                    String.format(l, "%.3f", fDistanceValid) + units));
            if (significantDifference(fdistanceCovered, fDistanceValid))
                pieEntries.add(new PieEntry(fdistanceCovered - fDistanceValid,
                        String.format(l, "%.3f", fdistanceCovered - fDistanceValid) + units));

            PieDataSet pieSet = new PieDataSet(pieEntries, "Distances");
            pieSet.setColors(new int[]{getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary)});
            pieSet.setSliceSpace(2);

            PieData pieData = new PieData(pieSet);
            pieData.setValueFormatter(new PercentFormatter());
            pieData.setValueTextSize(20f);
            pieData.setValueTextColor(Color.WHITE);
            pie.setData(pieData);

            pie.setUsePercentValues(true);
            pie.setDescription("Distance Summary");
            pie.setDescriptionTextSize(25f);
            pie.setCenterText(
                    String.format("Avg Speed: %.2f %s%nMax Speed: %.2f %s",
                            faverageSpeed, units + "/h",
                            fmaxSpeed, units + "/h"));
            pie.setCenterTextSize(15f);
            pie.setHoleRadius(55);
            Legend pieLegend = pie.getLegend();
            pieLegend.setCustom(
                    new int[]{getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary)},
                    new String[]{"Valid", "Invalid"}
            );
            pieLegend.setPosition(Legend.LegendPosition.BELOW_CHART_RIGHT);
            pieLegend.setForm(Legend.LegendForm.CIRCLE);
            pieLegend.setTextSize(15f);
            pie.invalidate();
        }
    }

    private boolean significantDifference(Float distanceCovered, float distanceValid) {
        return distanceCovered - distanceValid > 0.001;
    }

    private void refreshStats(final View recentView) {
        Timer speedTimer = new Timer();
        speedTimer.schedule(
                new TimerTask() {
                    @Override
                    public void run() {
                        if(lastView == recentView && getActivity() != null)
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showStats(recentView, false);
                                    cancel();
                                }
                            });
                        else
                            cancel();
                        cancel();
                    }
                },
                0,
                60000);
    }

    private void resetStats(View v) {
        if(stats != null)
            stats.reset();
        if(isAdded())
            showStats(getView(), true);
    }

    private void showResetButton(boolean show) {
        if(show)
            btn_resetStats.setVisibility(View.VISIBLE);
        else
            btn_resetStats.setVisibility(View.INVISIBLE);
    }
}
