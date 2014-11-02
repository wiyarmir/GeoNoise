package es.wiyarmir.geonoise;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Outline;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Date;


public class RecordFragment extends Fragment implements MainActivity.LocationNoiseUpdatesListener {

    private static final String TAG = "RecordFragment";
    private static Intent serviceIntent = null;
    private TextView tDecibels;
    private TextView tUpdate;

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

        final ImageButton startStopButton = (ImageButton) view.findViewById(R.id.button);
        int size = getResources().getDimensionPixelSize(R.dimen.fab_size);
        if (Build.VERSION.SDK_INT >= 21) {
            Outline outline = new Outline();
            outline.setOval(0, 0, size, size);
        }
        startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (startStopButton.getText().toString().equals(getText(R.string.button_start_rec).toString())) {
                    getActivity().startService(serviceIntent);
                    startStopButton.setText(getText(R.string.button_stop_rec));
                } else {
                    try {
                        getActivity().stopService(serviceIntent);
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    startStopButton.setText(getText(R.string.button_start_rec));

                }*/
            }
        });

        tDecibels = (TextView) view.findViewById(R.id.text_db);

        tUpdate = (TextView) view.findViewById(R.id.text_update);

        return view;
    }


    @Override
    public void onLocationNoiseUpdate(@Nullable Location location, double noise) {
        tDecibels.setText(String.format("%.2f db", noise));
        tUpdate.setText(String.format("Last updated: %s", location != null ? new Date(location.getTime()).toString() : null));
    }

    @Override
    public boolean isInterestedInLocationNoiseUpdates() {
        return true;
    }
}
