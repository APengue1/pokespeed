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
        showStats(view);
        lastView = view;
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
            showStats(lastView);
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

    private void showStats(View view) {
        PieChart pie = (PieChart) view.findViewById(R.id.pieChart);
        if(stats == null) {
            pie.setNoDataText("Turn on PokeSpeed and start moving to see some stats!");
        }
        else {
            String units = prefs.getBoolean("imperial", false) ? "mi" : "km";

            double[] statsValues = stats.getStats();
//            TextView distanceValid = (TextView)view.findViewById(R.id.validDistance);
//            TextView distanceCovered = (TextView)view.findViewById(R.id.distanceCovered);
//            TextView percentDistance = (TextView)view.findViewById(R.id.percentDistance);
//            TextView averageSpeed = (TextView)view.findViewById(R.id.averageSpeed);
//            TextView maxSpeed = (TextView)view.findViewById(R.id.maxSpeed);

            float fDistanceValid = Double.valueOf(statsValues[0]).floatValue();
            float fdistanceCovered = Double.valueOf(statsValues[1]).floatValue();
            Integer fpercentDistance = Double.valueOf(statsValues[2]*100).intValue();
            float faverageSpeed = Double.valueOf(statsValues[3]).floatValue();
            float fmaxSpeed = Double.valueOf(statsValues[4]).floatValue();

            Locale l = Locale.getDefault();
//            distanceValid.setText(String.format(l,"%.2f", fDistanceValid));
//            distanceCovered.setText(String.format(l,"%.2f", fdistanceCovered));
//            percentDistance.setText(String.format(l,"%d", fpercentDistance));
//            averageSpeed.setText(String.format(l,"%d", Double.valueOf(faverageSpeed).intValue()));
//            maxSpeed.setText(String.format(l,"%d", fmaxSpeed));

            List<PieEntry> pieEntries = new ArrayList<>();
            pieEntries.add(new PieEntry(fDistanceValid,
                    String.format(l,"%.3f", fDistanceValid) + units));
            if(fdistanceCovered - fDistanceValid > 0.001)
                pieEntries.add(new PieEntry(fdistanceCovered - fDistanceValid,
                        String.format(l,"%.3f", fdistanceCovered - fDistanceValid) + units));

            PieDataSet pieSet = new PieDataSet(pieEntries, "Distances");
            pieSet.setColors(new int[] {getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary)});

            PieData pieData = new PieData(pieSet);
            pieData.setValueFormatter(new PercentFormatter());
            pieData.setValueTextSize(20f);
            pieData.setValueTextColor(Color.WHITE);
            pie.setData(pieData);

            pie.setUsePercentValues(true);
            pie.setDescription("Distance Summary");
            pie.setDescriptionTextSize(25f);
            pie.setCenterText(
                    String.format("Avg Speed: %s %s%nMax Speed: %s %s",
                            String.format(l, "%.2f", faverageSpeed), units +"/h",
                            String.format(l, "%.2f", fmaxSpeed), units+"/h"));
            pie.setCenterTextSize(15f);
            Legend pieLegend = pie.getLegend();
            pieLegend.setCustom(
                    new int[] {getResources().getColor(R.color.colorAccent), getResources().getColor(R.color.colorPrimary)},
                    new String[] {"Valid", "Invalid"}
            );
            pieLegend.setPosition(Legend.LegendPosition.BELOW_CHART_RIGHT);
            pieLegend.setForm(Legend.LegendForm.CIRCLE);
            pieLegend.setTextSize(15f);
            pie.invalidate();
        }
    }

//    private void refreshStats(final View recentView) {
//        Timer speedTimer = new Timer();
//        speedTimer.schedule(
//                new TimerTask() {
//                    @Override
//                    public void run() {
//                        if(lastView == recentView && getActivity() != null)
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    showStats(recentView);
//                                }
//                            });
//                        else
//                            ;//cancel();
//
//                    }
//                },
//                0,
//                2500);
//    }
}
