package es.guillermoorellana.geonoise.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ServiceCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import es.guillermoorellana.geonoise.R;
import es.guillermoorellana.geonoise.utils.ForegroundNotifications;

/**
 * Created by Guillermo Orellana on 02/11/14.
 */
public class LocationService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LocationService";
    private static final int NOTIF_ID_LOCATION = 2002;
    private static LocationService sInstance;
    private final IBinder mBinder = new LocationBinder();
    private GoogleApiClient locationClient;
    private boolean tracking;

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

    /**
     * Promote this service to a {@code location}-typed foreground service with an ongoing
     * notification while location is actively tracked. Called by {@code RecordService} only when a
     * location runtime permission is granted, so the location-typed {@code startForeground} is never
     * reached without its permission.
     */
    public void startTracking() {
        ForegroundNotifications.ensureChannel(this);
        ServiceCompat.startForeground(
                this,
                NOTIF_ID_LOCATION,
                ForegroundNotifications.ongoing(this, R.string.notification_location_text),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        tracking = true;
    }

    /** Leave the foreground state, remove the notification, and end the Started state. */
    public void stopTracking() {
        if (tracking) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
            tracking = false;
        }
        stopSelf();
    }

    @Override
    public void onDestroy() {
        if (tracking) {
            ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE);
            tracking = false;
        }
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
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        Toast.makeText(this, "Connection failed", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Connection to GMS failed");

    }


    static class LocationBinder extends Binder {
        LocationService getService() {
            return getInstance();
        }
    }
}
