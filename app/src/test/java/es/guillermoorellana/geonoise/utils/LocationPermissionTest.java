package es.guillermoorellana.geonoise.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for the pure location-permission decision core. No Android runtime needed.
 */
public class LocationPermissionTest {

    @Test
    public void isGranted_bothDenied_false() {
        assertFalse(LocationPermission.isGranted(false, false));
    }

    @Test
    public void isGranted_fineOnly_true() {
        assertTrue(LocationPermission.isGranted(true, false));
    }

    @Test
    public void isGranted_coarseOnly_true() {
        assertTrue(LocationPermission.isGranted(false, true));
    }

    @Test
    public void isGranted_bothGranted_true() {
        assertTrue(LocationPermission.isGranted(true, true));
    }
}
