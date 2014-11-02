package es.wiyarmir.geonoise.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.DialogFragment;
import android.widget.Toast;

import java.io.File;

/**
 * Created by wiyarmir on 25/08/14.
 */
public class FilePickerDialogFragment extends DialogFragment {

    private HeatmapCapable instance;
    public File mPath = null;
    public String[] mFileList;

    public FilePickerDialogFragment() {
        mPath = new File(Utils.getSaveDirPath());
        mFileList = Utils.listFilesInPath(mPath);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick file");
        Utils.listFilesInPath(mPath);
        builder.setNegativeButton("Cancel", null)
                .setItems(mFileList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getActivity(), "Chose " + mFileList[i], Toast.LENGTH_LONG).show();
                        new AsyncHeatmapFileLoader(instance).execute(mPath + File.separator + mFileList[i]);
                    }
                });
        return builder.create();

    }

    public void setInstance(HeatmapCapable instance) {
        this.instance = instance;
    }
}
