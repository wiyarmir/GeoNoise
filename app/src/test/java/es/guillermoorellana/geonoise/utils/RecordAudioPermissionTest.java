package es.guillermoorellana.geonoise.utils;

import static es.guillermoorellana.geonoise.utils.RecordAudioPermission.Outcome.DENIED_CAN_RETRY;
import static es.guillermoorellana.geonoise.utils.RecordAudioPermission.Outcome.DENIED_DONT_ASK_AGAIN;
import static es.guillermoorellana.geonoise.utils.RecordAudioPermission.Outcome.REQUEST;
import static es.guillermoorellana.geonoise.utils.RecordAudioPermission.Outcome.START;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for the pure permission-gate decision core. No Android runtime needed.
 */
public class RecordAudioPermissionTest {

    @Test
    public void onStartPressed_granted_starts() {
        assertEquals(START, RecordAudioPermission.onStartPressed(true));
    }

    @Test
    public void onStartPressed_denied_requests() {
        assertEquals(REQUEST, RecordAudioPermission.onStartPressed(false));
    }

    @Test
    public void onRequestResult_granted_starts() {
        assertEquals(START, RecordAudioPermission.onRequestResult(true, false));
        assertEquals(START, RecordAudioPermission.onRequestResult(true, true));
    }

    @Test
    public void onRequestResult_deniedWithRationale_canRetry() {
        assertEquals(DENIED_CAN_RETRY, RecordAudioPermission.onRequestResult(false, true));
    }

    @Test
    public void onRequestResult_deniedWithoutRationale_dontAskAgain() {
        assertEquals(DENIED_DONT_ASK_AGAIN, RecordAudioPermission.onRequestResult(false, false));
    }
}
