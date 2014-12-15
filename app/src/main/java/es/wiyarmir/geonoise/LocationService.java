package es.wiyarmir.geonoise;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by wiyarmir on 02/11/14.
 */
public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LocationService";
    private static LocationService sInstance;
    private final IBinder mBinder = new LocationBinder();
    private GoogleApiClient locationClient;

    public static LocationService getInstance() {
        return sInstance;
    }

    public GoogleApiClient getLocationClient() {
        return locationClient;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        locationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
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

    @Override
    public void onConnectionSuspended(int i) {

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
