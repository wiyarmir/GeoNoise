package es.wiyarmir.geonoise;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
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
    private final IBinder mBinder = new RecordBinder();
    protected LocationClient locationClient = null;
    protected LocationRequest locationRequest = null;
    protected CSVWriter wr = null;
    private ComponentName locSrv;
    private boolean recording;


    @Override
    public void onCreate() {
        super.onCreate();
        locSrv = startService(new Intent(RecordService.this, LocationService.class));
        //locationClient = new LocationClient(this, this, this);
        locationRequest = LocationRequest.create();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        stopRecording();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(500);
        return START_STICKY;
    }

    protected void startRecording() {
        recording = true;
        locationClient.connect();

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
        locationClient.disconnect();
        try {
            wr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getAmplitude() {
        return 0;
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
