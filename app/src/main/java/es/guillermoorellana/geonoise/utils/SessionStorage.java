package es.guillermoorellana.geonoise.utils;

import java.io.File;

/**
 * Pure save-directory path logic for recorded sessions. No Android calls, so it
 * can be unit-tested on the JVM (unlike {@link Utils}, whose static initializer
 * touches {@code android.graphics.Color}). Framework glue lives in
 * {@link Utils#getSaveDirPath(android.content.Context)}, which feeds the
 * app-specific base directory into {@link #saveDirPathFor(File)}.
 */
public final class SessionStorage {

    private SessionStorage() {
        // no instances
    }

    /**
     * Composes the session save-directory path under a given base directory.
     *
     * @param baseDir the base directory the sessions folder lives under
     *                (e.g. {@code context.getExternalFilesDir(null)})
     * @return the absolute path to the {@link Utils#SAVEFILE_DIR} directory under {@code baseDir}
     */
    public static String saveDirPathFor(File baseDir) {
        return baseDir.getAbsolutePath() + File.separator + Utils.SAVEFILE_DIR;
    }
}
