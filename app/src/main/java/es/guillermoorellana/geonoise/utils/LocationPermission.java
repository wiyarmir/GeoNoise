package es.guillermoorellana.geonoise.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

/**
 * Permission policy for location access
 * ({@link Manifest.permission#ACCESS_FINE_LOCATION} / {@link Manifest.permission#ACCESS_COARSE_LOCATION}).
 *
 * <p>The decision core ({@link #isGranted(boolean, boolean)}) is pure — it takes only primitives
 * and returns a boolean, with no Android calls — so it is unit-testable without a device.
 * {@link #isGranted(Context)} is the thin framework wrapper the callers use to feed the current
 * grant state into the core. Mirrors the seam established by {@link RecordAudioPermission}.
 *
 * <p>Either the fine or the coarse grant is enough to consider location available: the app requests
 * both and any granted precision lets location updates flow.
 */
public final class LocationPermission {

    private LocationPermission() {
    }

    /**
     * Whether location access may be considered granted.
     *
     * @param fineGranted   whether {@link Manifest.permission#ACCESS_FINE_LOCATION} is granted
     * @param coarseGranted whether {@link Manifest.permission#ACCESS_COARSE_LOCATION} is granted
     * @return {@code true} if either the fine or the coarse permission is granted
     */
    public static boolean isGranted(boolean fineGranted, boolean coarseGranted) {
        return fineGranted || coarseGranted;
    }

    /**
     * Thin wrapper over {@link ContextCompat#checkSelfPermission}. Framework passthrough — not unit
     * tested — feeding the live fine/coarse grant state into {@link #isGranted(boolean, boolean)}.
     */
    public static boolean isGranted(Context context) {
        boolean fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        return isGranted(fine, coarse);
    }
}
