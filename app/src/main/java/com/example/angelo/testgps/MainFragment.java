package com.example.angelo.testgps;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MainFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class MainFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ToggleButton speedToggle;
    private Button btn_stats;
    private static boolean toggledOff = true;
    private boolean mBound = false;
    private SpeedService speedService;
    private static PokeSpeedStats stats;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public MainFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final RelativeLayout r = (RelativeLayout)inflater.inflate(
                R.layout.fragment_main, container, false);

        stats = null;
        speedToggle = (ToggleButton)r.findViewById(R.id.toggleSpeedService);
        speedToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(toggledOff)
                    startSpeedService();
                else
                    stopSpeedService();
            }
        });
        btn_stats = (Button)r.findViewById(R.id.button_stats);
        btn_stats.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(stats != null) {
                    double[] statsValues = stats.getStats();
                    TextView distanceValid = (TextView)r.findViewById(R.id.validDistance);
                    TextView distanceCovered = (TextView)r.findViewById(R.id.distanceCovered);
                    TextView percentDistance = (TextView)r.findViewById(R.id.percentDistance);
                    TextView averageSpeed = (TextView)r.findViewById(R.id.averageSpeed);
                    TextView maxSpeed = (TextView)r.findViewById(R.id.maxSpeed);

                    Locale l = Locale.getDefault();
                    distanceValid.setText(String.format(l,"%.2f", statsValues[0]));
                    distanceCovered.setText(String.format(l,"%.2f", statsValues[0]));
                    percentDistance.setText(String.format(l,"%d", Double.valueOf(statsValues[2]*100).intValue()));
                    averageSpeed.setText(String.format(l,"%d", Double.valueOf(statsValues[3]).intValue()));
                    maxSpeed.setText(String.format(l,"%d", Double.valueOf(statsValues[4]).intValue()));
                }
            }
        });

        speedToggle.setChecked(!toggledOff);
        return r;
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

        if(!toggledOff)
            _bindService();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        _unbindService();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void stopSpeedService() {
        _unbindService();
        getActivity().stopService(new Intent(getActivity(), SpeedService.class));
        toggledOff = true;
    }

    private void startSpeedService() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }
        else {
            _startSpeedService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] != -1) {
            _startSpeedService();
        }
    }

    private void _bindService() {
        if(!mBound) {
            getActivity().bindService(new Intent(getActivity(), SpeedService.class), this.mConnection, Context.BIND_AUTO_CREATE);
            mBound = true;
        }
    }

    private void _unbindService() {
        if(mBound) {
            getActivity().unbindService(this.mConnection);
            mBound = false;
        }
    }

    private void _startSpeedService() {
        getActivity().startService(new Intent(getActivity(), SpeedService.class));
        _bindService();
        toggledOff = false;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            speedService = ((SpeedService.LocalBinder) service).getService();
            stats = speedService.getStatsObj();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
            speedService = null;
        }
    };
}
