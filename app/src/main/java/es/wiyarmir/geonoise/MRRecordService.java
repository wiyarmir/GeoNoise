package es.wiyarmir.geonoise;

import android.content.Intent;
import android.location.Location;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MRRecordService extends RecordService {
    private static String TAG = "MRRecordService";
    private MediaRecorder recorder;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        try {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_RECOGNITION);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            recorder.setOutputFile("/dev/null");
            recorder.prepare();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void startRecording() {
        if (recorder != null) {
            recorder.start();
            recorder.getMaxAmplitude(); // first one will be 0
        }

        super.startRecording();

    }

    @Override
    protected void stopRecording() {
        if (recorder != null) {
            try {

                recorder.stop();
                recorder.reset();
                recorder.release();
                recorder = null;
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        super.stopRecording();
    }

    @Override
    public int getAmplitude() {
        if (recorder != null)
            return recorder.getMaxAmplitude();
        else
            return 0;

    }


    @Override
    public void onLocationChanged(Location location) {
        Intent i = new Intent(LOCATION_NOISE_UPDATE);
        double db = getDecibels(getAmplitude());

        i.putExtra("Location", location);
        i.putExtra("Noise", db);
        sendBroadcast(i);
        // Log.i("Location", "a:" + db + " l:" + location.toString());
        wr.writeNext(new String[]{String.valueOf(db), String.valueOf(location.getLatitude()),
                String.valueOf(location.getLongitude()), String.valueOf(location.getAccuracy()),
                new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(new Date(location.getTime()))});
    }
}
