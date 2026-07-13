package es.guillermoorellana.geonoise.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.content.ContextCompat;

/**
 * Permission policy for the microphone ({@link Manifest.permission#RECORD_AUDIO}).
 *
 * <p>The decision core ({@link #onStartPressed}, {@link #onRequestResult}, {@link #canStartAudio})
 * is pure — it takes only primitives and returns an {@link Outcome}/boolean, with no Android calls —
 * so it is unit-testable without a device. {@link #isGranted(Context)} is the thin framework
 * wrapper the callers use to feed the current grant state into the core.
 */
public final class RecordAudioPermission {

    /** The runtime permission this policy governs. */
    public static final String PERMISSION = Manifest.permission.RECORD_AUDIO;

    /** What a caller should do next, given the current grant/rationale state. */
    public enum Outcome {
        /** Permission is granted — proceed to record. */
        START,
        /** Permission is missing — request it via the Activity Result API. */
        REQUEST,
        /** Denied, but the system will still show the prompt again — a rationale can be shown. */
        DENIED_CAN_RETRY,
        /** Denied with "don't ask again" — the user must enable it from Settings. */
        DENIED_DONT_ASK_AGAIN
    }

    private RecordAudioPermission() {
    }

    /**
     * Decide what to do when the user presses the start-recording action.
     *
     * @param granted whether {@link #PERMISSION} is currently granted
     * @return {@link Outcome#START} if granted, otherwise {@link Outcome#REQUEST}
     */
    public static Outcome onStartPressed(boolean granted) {
        return granted ? Outcome.START : Outcome.REQUEST;
    }

    /**
     * Decide what to do once the permission request returns.
     *
     * @param granted         whether the user granted the permission
     * @param canShowRationale the value of {@code shouldShowRequestPermissionRationale} after the
     *                         denial — {@code true} means the prompt can be shown again,
     *                         {@code false} (after a denial) means "don't ask again"
     * @return the resulting {@link Outcome}
     */
    public static Outcome onRequestResult(boolean granted, boolean canShowRationale) {
        if (granted) {
            return Outcome.START;
        }
        return canShowRationale ? Outcome.DENIED_CAN_RETRY : Outcome.DENIED_DONT_ASK_AGAIN;
    }

    /**
     * Thin wrapper over {@link ContextCompat#checkSelfPermission}. Framework passthrough — not unit
     * tested.
     */
    public static boolean isGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, PERMISSION)
                == PackageManager.PERMISSION_GRANTED;
    }
}
