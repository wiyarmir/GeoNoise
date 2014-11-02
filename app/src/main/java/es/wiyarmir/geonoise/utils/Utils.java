package es.wiyarmir.geonoise.utils;

import android.location.Location;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by wiyarmir on 10/08/14.
 */
public class Utils {

    public static final String SAVEFILE_DIR = "GeoNoise_Sessions";

    public static String getSaveDirPath() {
        File dir = Environment.getExternalStorageDirectory();
        return dir.getAbsolutePath() + File.separator + SAVEFILE_DIR;
    }

    public static String[] listFilesInPath(File mPath) {
        try {
            mPath.mkdirs();
        } catch (SecurityException e) {
            Log.e("", "unable to write on the sd card " + e.toString());
        }
        if (mPath.exists()) {
            FilenameFilter filter = new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return filename.contains(".csv") || sel.isDirectory();
                }
            };
             return mPath.list(filter);
        } else {
            return new String[0];
        }
    }

}
