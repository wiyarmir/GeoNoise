package es.guillermoorellana.geonoise.utils;

import android.app.Notification;
import android.content.Context;

import androidx.annotation.StringRes;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import es.guillermoorellana.geonoise.R;

/**
 * Framework glue for the ongoing foreground-service notification shared by
 * {@code RecordService} and {@code LocationService}.
 *
 * <p>All calls go through the AndroidX compat surface
 * ({@link NotificationChannelCompat}, {@link NotificationManagerCompat}, {@link NotificationCompat})
 * so they are safe from {@code minSdk 21} up: the notification channel is a no-op below API 26 and
 * a real channel from API 26 on. There is no pure decision here to unit-test — this is intentionally
 * thin framework wiring, per the repo's decision-core convention.
 */
public final class ForegroundNotifications {

    /** Id of the low-importance channel the ongoing recording/tracking notification posts to. */
    public static final String CHANNEL_ID = "geonoise_recording";

    private ForegroundNotifications() {
    }

    /**
     * Create (or update) the low-importance notification channel. Idempotent, and a no-op below
     * API 26 where channels do not exist. Call before {@code startForeground(...)}.
     */
    public static void ensureChannel(Context context) {
        NotificationChannelCompat channel =
                new NotificationChannelCompat.Builder(
                        CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                        .setName(context.getString(R.string.notification_channel_name))
                        .build();
        NotificationManagerCompat.from(context).createNotificationChannel(channel);
    }

    /**
     * Build the ongoing, low-priority notification shown while a service is in the foreground.
     *
     * @param context context used to resolve strings/resources
     * @param textRes the body text describing what the service is doing (e.g. recording vs tracking)
     */
    public static Notification ongoing(Context context, @StringRes int textRes) {
        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(textRes))
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }
}
