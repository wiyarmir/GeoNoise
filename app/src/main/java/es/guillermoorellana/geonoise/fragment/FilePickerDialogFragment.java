package es.guillermoorellana.geonoise.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.io.File;

import es.guillermoorellana.geonoise.R;
import es.guillermoorellana.geonoise.utils.AsyncHeatmapFileLoader;
import es.guillermoorellana.geonoise.utils.HeatmapCapable;
import es.guillermoorellana.geonoise.utils.Utils;

/**
 * Created by Guillermo Orellana on 25/08/14.
 */
public class FilePickerDialogFragment extends DialogFragment {

    private final HeatmapCapable instance;
    public File mPath = null;
    public String[] mFileList;

    public FilePickerDialogFragment(HeatmapCapable hc) {
        mPath = new File(Utils.getSaveDirPath());
        mFileList = Utils.listFilesInPath(mPath);
        this.instance = hc;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_pick_file));
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

}
