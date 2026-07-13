package es.guillermoorellana.geonoise.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.os.Build;

import org.junit.Test;

/**
 * Unit tests for the pure POST_NOTIFICATIONS decision core. No Android runtime needed.
 */
public class PostNotificationsPermissionTest {

    @Test
    public void shouldRequest_below33_never() {
        assertFalse(PostNotificationsPermission.shouldRequest(Build.VERSION_CODES.S, false));
        assertFalse(PostNotificationsPermission.shouldRequest(Build.VERSION_CODES.S, true));
        assertFalse(PostNotificationsPermission.shouldRequest(21, false));
    }

    @Test
    public void shouldRequest_33OrAbove_granted_false() {
        assertFalse(PostNotificationsPermission.shouldRequest(Build.VERSION_CODES.TIRAMISU, true));
        assertFalse(PostNotificationsPermission.shouldRequest(34, true));
    }

    @Test
    public void shouldRequest_33OrAbove_denied_true() {
        assertTrue(PostNotificationsPermission.shouldRequest(Build.VERSION_CODES.TIRAMISU, false));
        assertTrue(PostNotificationsPermission.shouldRequest(34, false));
    }
}
