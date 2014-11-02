package es.wiyarmir.geonoise;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

    public RecordFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        serviceIntent = new Intent(getActivity(), ARRecordService.class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_record, container, false);

        final Button startStopButton = (Button) view.findViewById(R.id.button);
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startStopButton.getText().toString().equals(getText(R.string.button_start_rec).toString())) {
                    getActivity().startService(serviceIntent);
                    startStopButton.setText(getText(R.string.button_stop_rec));
                } else {
                    try {
                        getActivity().stopService(serviceIntent);
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    startStopButton.setText(getText(R.string.button_start_rec));

                }
            }
        });

        tDecibels = (TextView) view.findViewById(R.id.text_db);

        tUpdate = (TextView) view.findViewById(R.id.text_update);

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
