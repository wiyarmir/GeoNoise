package es.wiyarmir.geonoise;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import au.com.bytecode.opencsv.CSVWriter;
import es.wiyarmir.geonoise.utils.Utils;

/**
 * Created by wiyarmir on 10/08/14.
 */
public abstract class RecordService extends Service implements LocationListener {
    public static String LOCATION_NOISE_UPDATE = "es.wiyarmir.geonoise.update";
    private static String TAG = "RecordService";
    private final IBinder mBinder = new RecordBinder();
    protected LocationRequest locationRequest = null;
    protected CSVWriter wr = null;
    private boolean recording;
    private LocationService mService;
    private boolean mBound;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d(TAG, "RecordService connected to LocationService");
            // We've bound to RecordService, cast the IBinder and get RecordService instance
            LocationService.LocationBinder binder = (LocationService.LocationBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "RecordService disconnected from LocationService");
            mBound = false;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        bindService(new Intent(RecordService.this, LocationService.class), mConnection, Context.BIND_AUTO_CREATE);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(500);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        stopRecording();
        unbindService(mConnection);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    protected void startRecording() {
        recording = true;
        mService.getLocationClient().requestLocationUpdates(locationRequest, this);

        Toast.makeText(this, "Saving session to " + getFilePathForSession(), Toast.LENGTH_LONG).show();

        try {
            String path = getFilePathForSession();
            new File(Utils.getSaveDirPath()).mkdirs(); // create dirs by if the flies
            wr = new CSVWriter(new FileWriter(path));
            wr.writeNext(new String[]{"magnitude", "latitude", "longitude", "accuracy", "timestamp"});
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getFilePathForSession() {
        return Utils.getSaveDirPath()
                + File.separator + "dump - "
                + new SimpleDateFormat("yyyyMMddhhmmss").format(new Date())
                + ".csv";
    }

    protected void stopRecording() {
        recording = false;
        try {
            wr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getDecibels(double level) {
        //return Math.abs(20.0 * Math.log10(level / 51805.5336 / 0.00002));
        return (20.0 * Math.log10(level / 32767.0) + 120.0);
    }

    public boolean isRecording() {
        return recording;
    }

    public class RecordBinder extends Binder {
        RecordService getService() {
            return RecordService.this;
        }
    }

}
