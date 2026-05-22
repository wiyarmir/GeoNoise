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
        this.instance = hc;
    }

    @Override
    public void onAttach(@NonNull android.content.Context context) {
        super.onAttach(context);
        // Initialize path and file list here to ensure context is available
        mPath = new File(Utils.getSaveDirPath(context));
        mFileList = Utils.listFilesInPath(mPath);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.dialog_pick_file));
        Utils.listFilesInPath(mPath);

        // Check if there are any recordings available
        if (mFileList == null || mFileList.length == 0) {
            // Show a message when there are no recordings
            builder.setMessage(getString(R.string.no_recordings))
                  .setNegativeButton(getString(R.string.cancel), null);
        } else {
            // Show the list of recordings
            builder.setNegativeButton(getString(R.string.cancel), null)
                  .setItems(mFileList, new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialogInterface, int i) {
                          Toast.makeText(getActivity(), "Chose " + mFileList[i], Toast.LENGTH_LONG).show();
                          new AsyncHeatmapFileLoader(instance).execute(mPath + File.separator + mFileList[i]);
                      }
                  });
        }
        return builder.create();
    }

}
