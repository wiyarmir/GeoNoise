package es.guillermoorellana.geonoise.service;

import android.app.Service;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.IBinder;

import androidx.core.app.ServiceCompat;

import es.guillermoorellana.geonoise.R;
import es.guillermoorellana.geonoise.utils.ForegroundNotifications;

/**
 * Created by Guillermo Orellana on 02/11/14.
 *
 * <p>Location updates are owned by {@code RecordService} via its own
 * {@code FusedLocationProviderClient}; this service exists solely to host the
 * {@code location}-typed foreground service (an ongoing notification while tracking) and expose a
 * binder so {@code RecordService} can toggle it.
 */
public class LocationService extends Service {

    private static final int NOTIF_ID_LOCATION = 2002;
    private static LocationService sInstance;
    private final IBinder mBinder = new LocationBinder();
    private boolean tracking;

    public static LocationService getInstance() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
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
        super.onDestroy();
    }

    static class LocationBinder extends Binder {
        LocationService getService() {
            return getInstance();
        }
    }
}
