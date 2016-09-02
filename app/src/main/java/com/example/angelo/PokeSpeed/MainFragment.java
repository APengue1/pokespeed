package com.example.angelo.PokeSpeed;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;


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

    private SharedPreferences prefs;

    private TextView mainSpeed;
    private ToggleButton speedToggle;
    private boolean mBound = false;
    private SpeedService speedService;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        return fragment;
    }
    public MainFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(
                R.layout.fragment_main, container, false);

        mainSpeed = (TextView)view.findViewById(R.id.mainSpeed);

        speedToggle = (ToggleButton)view.findViewById(R.id.toggleSpeedService);
        refreshToggle();
        speedToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(speedToggle.isChecked()) {
                    startSpeedService();
                    toggleOn();
                }
                else {
                    mainSpeed.setText("");
                    stopSpeedService();
                    toggleOff();
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        if(MainActivity.permissionGranted != null && MainActivity.permissionGranted) {
            MainActivity.permissionGranted = null;
            toggleOn();
        }
        else
            refreshToggle();
        if(mBound) setMainSpeed();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessagereceiver,
                new IntentFilter("SpeedServiceStop")
        );
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mMessagereceiver,
                new IntentFilter("SpeedRefreshed")
        );
        super.onResume();
    }

    @Override
    public void onStop() {
        refreshToggle();
        super.onStop();
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessagereceiver);
        super.onPause();
    }

    private BroadcastReceiver mMessagereceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getBooleanExtra("SpeedServiceStop", false))
                refreshToggle();
            if(intent.getBooleanExtra("SpeedRefreshed", false))
                setMainSpeed();
        }
    };

    private void setMainSpeed() {
        String lastMessage = SpeedService.getLastSpeed();
        if(lastMessage != null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String unit = prefs.getBoolean("imperial", false) ? "mi/h" : "km/h";
            try {
                Float.parseFloat(lastMessage);
                mainSpeed.setText(String.format("%s %s", lastMessage, unit));
            } catch (NumberFormatException e) {
                mainSpeed.setText(lastMessage);
            }
        }
    }
//    private void refreshSpeed() {
//        Timer speedTimer = new Timer();
//        speedTimer.schedule(
//            new TimerTask() {
//                @Override
//                public void run() {
//                if(mBound)
//                    getActivity().runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            mainSpeed.setText(speedService.getLastSpeed());
//                            if(prefs.getBoolean("imperial", false))
//                                speedUnit.setText("mph");
//                            else
//                                speedUnit.setText("kmh");
//                        }
//                    });
//                else
//                    cancel();
//
//                }
//            },
//            0,
//            1000);
//    }

    private void refreshToggle() {
        if(SpeedService.serviceOn)
            toggleOn();
        else
            toggleOff();
    }

    private void toggleOn() {
        speedToggle.setChecked(true);
        speedToggle.setBackgroundColor(getResources().getColor(R.color.colorPokeYellow));
    }

    private void toggleOff() {
        speedToggle.setChecked(false);
        speedToggle.setBackgroundColor(getResources().getColor(R.color.white));
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

        if(SpeedService.serviceOn)
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

    public void stopSpeedService() {
        _unbindService();
        getActivity().stopService(new Intent(getActivity(), SpeedService.class));
        toggleOff();
    }

    public void startSpeedService() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            toggleOff();
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
        toggleOn();
        _bindService();
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            speedService = ((SpeedService.LocalBinder) service).getService();
            MainActivity.stats = speedService.getStatsObj();
            mBound = true;
            //refreshSpeed();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBound = false;
            speedService = null;
        }
    };
}
