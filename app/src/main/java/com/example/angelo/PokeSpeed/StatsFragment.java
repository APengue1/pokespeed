package com.example.angelo.PokeSpeed;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StatsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatsFragment extends Fragment {

    private Button btn_stats;
    private static PokeSpeedStats stats;
    private OnFragmentInteractionListener mListener;
    private SharedPreferences prefs;

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
        btn_stats = (Button)view.findViewById(R.id.button_stats);
        btn_stats.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showStats(view);
            }
        });        return view;
    }

    private void showStats(View view) {
        if(stats != null) {
            double[] statsValues = stats.getStats();
            TextView distanceValid = (TextView)view.findViewById(R.id.validDistance);
            TextView distanceCovered = (TextView)view.findViewById(R.id.distanceCovered);
            TextView percentDistance = (TextView)view.findViewById(R.id.percentDistance);
            TextView averageSpeed = (TextView)view.findViewById(R.id.averageSpeed);
            TextView maxSpeed = (TextView)view.findViewById(R.id.maxSpeed);

            Locale l = Locale.getDefault();
            distanceValid.setText(String.format(l,"%.2f", statsValues[0]));
            distanceCovered.setText(String.format(l,"%.2f", statsValues[1]));
            percentDistance.setText(String.format(l,"%d", Double.valueOf(statsValues[2]*100).intValue()));
            averageSpeed.setText(String.format(l,"%d", Double.valueOf(statsValues[3]).intValue()));
            maxSpeed.setText(String.format(l,"%d", Double.valueOf(statsValues[4]).intValue()));
        }
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
}
