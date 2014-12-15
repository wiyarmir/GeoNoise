package es.wiyarmir.geonoise;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import java.util.Date;

import es.wiyarmir.geonoise.utils.LocationNoiseUpdatesListener;


public class RecordFragment extends Fragment implements LocationNoiseUpdatesListener {

    private static final String TAG = "RecordFragment";
    private static final int HISTORY_SIZE = 50;
    private static Intent serviceIntent = null;
    private TextView tDecibels;
    private TextView tUpdate;
    private XYPlot xyPlot;
    private SimpleXYSeries mySeries;
    private RecordService mService;
    private boolean mBound;
    private Button startStopButton = null;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d(TAG, "RecordFragment connected to RecordService");
            // We've bound to RecordService, cast the IBinder and get RecordService instance
            RecordService.RecordBinder binder = (RecordService.RecordBinder) service;
            mService = binder.getService();
            mBound = true;
            if (startStopButton != null) {
                startStopButton.setEnabled(true);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "RecordFragment disconnected from RecordService");
            mBound = false;
            if (startStopButton != null) {
                startStopButton.setEnabled(false);
            }
        }
    };

    public RecordFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        serviceIntent = new Intent(getActivity(), RecordService.class);

    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unbindService(mConnection);
        mService = null;
        mBound = false;

    }

    @Override
    public void onResume() {
        super.onResume();
        if (startStopButton != null && !mBound) {
            startStopButton.setEnabled(false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        startStopButton = (Button) view.findViewById(R.id.button);
        tDecibels = (TextView) view.findViewById(R.id.text_db);
        tUpdate = (TextView) view.findViewById(R.id.text_update);

        if (!mBound) {
            startStopButton.setEnabled(false);
        }
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mService.isRecording()) {
                    mService.startRecording();
                    clearSeries();
                    startStopButton.setText(getText(R.string.button_stop_rec));
                } else {
                    mService.stopRecording();
                    startStopButton.setText(getText(R.string.button_start_rec));

                }
            }
        });

        xyPlot = (XYPlot) view.findViewById(R.id.xyplot);
        xyPlot.setDomainBoundaries(0, HISTORY_SIZE, BoundaryMode.FIXED);
        xyPlot.setRangeBoundaries(0, 140, BoundaryMode.FIXED);

        LineAndPointFormatter formatter = new LineAndPointFormatter(
                Color.rgb(0, 0, 0), null, null, null);
        formatter.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
        formatter.getLinePaint().setStrokeWidth(10);

        mySeries = new SimpleXYSeries("Noise Level");
        mySeries.useImplicitXVals();

        xyPlot.addSeries(mySeries, formatter);

        return view;
    }

    private void clearSeries() {
        while (mySeries.size() > 0) {
            mySeries.removeFirst();
        }
    }


    @Override
    public void onLocationNoiseUpdate(@Nullable Location location, double noise) {

        tDecibels.setText(String.format("%.2f db", noise));
        tUpdate.setText(String.format("Last updated: %s", location != null ? new Date(location.getTime()).toString() : null));
        if (mySeries.size() > HISTORY_SIZE) {
            mySeries.removeFirst();
        }
        mySeries.addLast(null, noise);

        xyPlot.redraw();
    }

    @Override
    public boolean isInterestedInLocationNoiseUpdates() {
        return true;
    }


}
