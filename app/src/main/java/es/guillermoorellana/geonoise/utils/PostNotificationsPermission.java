package es.guillermoorellana.geonoise.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

/**
 * Permission policy for the notifications permission
 * ({@link Manifest.permission#POST_NOTIFICATIONS}), required at runtime on API 33+ to show the
 * foreground-service notification.
 *
 * <p>The decision core ({@link #shouldRequest(int, boolean)}) is pure — it takes only primitives
 * and returns a boolean, with no Android calls — so it is unit-testable without a device.
 * {@link #isGranted(Context)} is the thin framework wrapper the callers use to feed the current
 * grant state into the core. Mirrors the seam established by
 * {@link RecordAudioPermission}.
 *
 * <p>Unlike the microphone permission, a denial here is <em>non-blocking</em>: the foreground
 * service still starts; only the ongoing notification is suppressed by the system.
 */
public final class PostNotificationsPermission {

    /** The runtime permission this policy governs. */
    public static final String PERMISSION = Manifest.permission.POST_NOTIFICATIONS;

    private PostNotificationsPermission() {
    }

    /**
     * Whether the app should request {@link #PERMISSION} at runtime.
     *
     * @param sdkInt  the running platform's API level (e.g. {@code Build.VERSION.SDK_INT})
     * @param granted whether {@link #PERMISSION} is currently granted
     * @return {@code true} only when the platform is API 33+ (where the permission exists and must
     * be requested) and it is not already granted; {@code false} below API 33 (implicitly granted)
     * or when already granted
     */
    public static boolean shouldRequest(int sdkInt, boolean granted) {
        return sdkInt >= Build.VERSION_CODES.TIRAMISU && !granted;
    }

    /**
     * Thin wrapper over {@link ContextCompat#checkSelfPermission}. Below API 33 the permission does
     * not exist and notifications are implicitly allowed, so this reports {@code true}. Framework
     * passthrough — not unit tested.
     */
    public static boolean isGranted(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(context, PERMISSION)
                == PackageManager.PERMISSION_GRANTED;
    }
}
