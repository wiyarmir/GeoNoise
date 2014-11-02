package es.wiyarmir.geonoise;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
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
public abstract class RecordService extends Service implements LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {
    public static String LOCATION_NOISE_UPDATE = "es.wiyarmir.geonoise.update";
    protected LocationClient lc = null;
    protected LocationRequest lr = null;
    protected CSVWriter wr = null;

    @Override
    public void onCreate() {
        super.onCreate();
        lc = new LocationClient(this, this, this);
        lr = LocationRequest.create();


    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopRecording();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startRecording();

        lr.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        lr.setInterval(500);
        lr.setFastestInterval(500);
        return START_STICKY;
    }

    protected void startRecording() {
        lc.connect();

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

        lc.disconnect();
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

    @Override
    public void onLocationChanged(Location location) {

    }

    /*
   * Called by Location Services when the request to connect the
   * client finishes successfully. At this point, you can
   * request the current location or start periodic updates
   */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();
        lc.requestLocationUpdates(lr, this);
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Toast.makeText(this, "Disconnected. Please re-connect.",
                Toast.LENGTH_SHORT).show();
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
    }
}
