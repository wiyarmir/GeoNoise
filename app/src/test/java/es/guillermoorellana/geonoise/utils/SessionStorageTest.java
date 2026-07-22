package es.guillermoorellana.geonoise.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

/**
 * Unit tests for the pure save-directory path composition. No Android runtime needed.
 */
public class SessionStorageTest {

    @Test
    public void saveDirPathFor_appendsSaveDirUnderBaseDir() {
        String base = "/data/user/0/es.guillermoorellana.geonoise/files";
        assertEquals(
                base + File.separator + "GeoNoise_Sessions",
                SessionStorage.saveDirPathFor(new File(base)));
    }

    @Test
    public void saveDirPathFor_endsWithSaveDirConstant() {
        String path = SessionStorage.saveDirPathFor(new File("/tmp/example"));
        assertTrue(path.endsWith(File.separator + Utils.SAVEFILE_DIR));
    }
}
