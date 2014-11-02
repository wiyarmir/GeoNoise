package es.wiyarmir.geonoise;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

/**
 * Created by wiyarmir on 02/11/14.
 */
public class LocationService extends Service implements
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private static final String TAG = "LocationService";
    private static LocationService sInstance;
    private final IBinder mBinder = new LocationBinder();
    private LocationClient locationClient;

    public static LocationService getInstance() {
        return sInstance;
    }

    public LocationClient getLocationClient() {
        return locationClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        locationClient = new LocationClient(this, this, this);
        locationClient.connect();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        locationClient.disconnect();
        super.onDestroy();
    }

    /*
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle dataBundle) {
        // Display the connection status
        Log.d(TAG, "Connected to GMS");
        //locationClient.requestLocationUpdates(locationRequest, this);
    }

    /*
     * Called by Location Services if the connection to the
     * location client drops because of an error.
     */
    @Override
    public void onDisconnected() {
        // Display the connection status
        Log.d(TAG, "Disconnected from GMS");
    }

    /*
     * Called by Location Services if the attempt to
     * Location Services fails.
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Connection to GMS failed");

    }


    public class LocationBinder extends Binder {
        LocationService getService() {
            return getInstance();
        }
    }
}
