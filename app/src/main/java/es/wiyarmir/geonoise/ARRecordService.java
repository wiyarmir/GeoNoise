package es.wiyarmir.geonoise;

import android.content.Intent;
import android.location.Location;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wiyarmir on 10/08/14.
 */
public class ARRecordService extends RecordService {
    private static String TAG = "ARRecordService";
    private int bufferSize;
    private long runnableDelay = 750;
    private int sampleRate = 8000;
    private AudioRecord audio;
    private Handler mHandler = new Handler();
    private Location lastLocation;
    private float samplePeriod;

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate");
        super.onCreate();

        try {
            bufferSize = 10 * AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            samplePeriod = 1.0f / ((float) sampleRate / (float) bufferSize);
            Log.d(TAG, String.format("Starting recorder, sample rate of %d, buffer size %d. Should get full every %f s",
                    sampleRate, bufferSize, samplePeriod));
            audio = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);
        } catch (Exception e) {
            Log.d(TAG, "Error creating audioRecorder");
        }

    }

    public void readAudioBuffer() {
        try {
            short[] buffer = new short[bufferSize];
            int resultSize = -1;
            if (audio != null) {
                resultSize = audio.read(buffer, 0, bufferSize);
                double sum = 0;
                for (int i = 0; i < resultSize; i++) {
                    sum += Math.abs(buffer[i]);
                }
                double level = (sum / resultSize);
                Location location = lastLocation;

                if (location != null) {
                    Intent i = new Intent(LOCATION_NOISE_UPDATE);

                    double db = getDecibels(level);
                    i.putExtra("Location", location);
                    i.putExtra("Noise", db);
                    sendBroadcast(i);
                    //Log.i(TAG, "a:" + db + " l:" + location.toString());
                    wr.writeNext(new String[]{String.valueOf(db), String.valueOf(location.getLatitude()),
                            String.valueOf(location.getLongitude()), String.valueOf(location.getAccuracy()),
                            new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(new Date(location.getTime()))});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void startRecording() {
        audio.startRecording();
        mHandler.post(recorderRunnable);
        super.startRecording();
    }

    @Override
    protected void stopRecording() {
        if (audio != null) {
            try {
                audio.stop();
                audio.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            audio = null;
        }
        super.stopRecording();
    }

    @Override
    public int getAmplitude() {
        return 0;

    }
    @Override
    public void onLocationChanged(Location location) {

        lastLocation = location;
        //Log.d(TAG, "Location change: " + location);


    }

    Runnable recorderRunnable = new Runnable() {
        @Override
        public void run() {
            readAudioBuffer();
            if (audio != null)
                mHandler.postDelayed(recorderRunnable, runnableDelay);
        }
    };


}
