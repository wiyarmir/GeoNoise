package es.guillermoorellana.geonoise.service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import au.com.bytecode.opencsv.CSVWriter;
import es.guillermoorellana.geonoise.utils.RecordAudioPermission;
import es.guillermoorellana.geonoise.utils.Utils;

/**
 * Created by Guillermo Orellana on 10/08/14.
 */
public class RecordService extends Service implements LocationListener {
    public static String LOCATION_NOISE_UPDATE = "es.guillermoorellana.geonoise.action.update";
    private static String TAG = "RecordService";
    private final IBinder mBinder = new RecordBinder();
    protected LocationRequest locationRequest = null;
    protected CSVWriter wr = null;
    private boolean recording;
    private LocationService mService;
    private boolean mBound;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.d(TAG, "RecordService connected to LocationService");
            // We've bound to RecordService, cast the IBinder and get RecordService instance
            LocationService.LocationBinder binder = (LocationService.LocationBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.d(TAG, "RecordService disconnected from LocationService");
            mBound = false;
        }
    };

    private int bufferSize;
    private long runnableDelay = 250;
    private int sampleRate = 8000;
    private AudioRecord audio;
    private Handler mHandler = new Handler();
    private Location lastLocation;
    private float samplePeriod;
    private double splAdjustment = +8;

    @Override
    public void onCreate() {
        super.onCreate();

        bindService(new Intent(RecordService.this, LocationService.class), mConnection, Context.BIND_AUTO_CREATE);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(500);
        locationRequest.setFastestInterval(500);

        prepareAudio();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        stopRecording();
        unbindService(mConnection);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    // Constructing AudioRecord without RECORD_AUDIO does not throw; it yields an instance in
    // STATE_UNINITIALIZED. startRecording() gates actual use behind RecordAudioPermission, so the
    // recorder is never read/started without the grant.
    @SuppressLint("MissingPermission")
    private void prepareAudio() {
        try {
            bufferSize = 10 * AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
            samplePeriod = 1.0f / ((float) sampleRate / (float) bufferSize);
            Log.d(TAG, String.format("Creating recorder, sample rate of %d, buffer size %d. Should get full every %.2f s",
                    sampleRate, bufferSize, samplePeriod));

            try {
                audio = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION, sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

                // Check if AudioRecord was initialized properly
                if (audio.getState() != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioRecord initialization failed");
                    audio = null;
                }
            } catch (SecurityException e) {
                Log.e(TAG, "Permission denied for AudioRecord: " + e.getMessage());
                audio = null;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating audioRecorder: " + e.getMessage());
            audio = null;
        }
    }

    public void readAudioBuffer() {
        try {
            short[] buffer = new short[bufferSize];
            int resultSize = -1;
            if (audio != null) {
                resultSize = audio.read(buffer, 0, bufferSize);
                double sum = 0;
                // p_rms = sqrt(1/T Integral 0->T p^2) =sqrt( 1/T Sum 0->T p^2 )
                for (int i = 0; i < resultSize; i++) {
                    sum += buffer[i] * buffer[i] / 51805.5336 / 51805.5336;
                }
                double p_rms = Math.sqrt(sum / resultSize);
                Location location = lastLocation;

                if (location != null) {
                    Intent i = new Intent(LOCATION_NOISE_UPDATE);

                    double db = getDecibels(p_rms);
                    i.putExtra("Location", location);
                    i.putExtra("Noise", db);
                    sendBroadcast(i);
                    //Log.i(TAG, "a:" + db + " l:" + location.toString());
                    wr.writeNext(
                            new String[]{
                                    String.valueOf(db), String.valueOf(location.getLatitude()),
                                    String.valueOf(location.getLongitude()), String.valueOf(location.getAccuracy()),
                                    new SimpleDateFormat("dd/MM/yyyy hh:mm:ss").format(new Date(location.getTime()))
                            }
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // audio.startRecording() is reached only after the canStartAudio() guard below confirms
    // RECORD_AUDIO is granted and the recorder is initialised.
    @SuppressLint("MissingPermission")
    public void startRecording() {
        if (audio == null) {
            prepareAudio();
        }

        // Gate the recorder: only start when RECORD_AUDIO is granted and the AudioRecord
        // initialised. Otherwise never touch audio.startRecording() or the location client.
        boolean audioInitialized = audio != null && audio.getState() == AudioRecord.STATE_INITIALIZED;
        if (!RecordAudioPermission.canStartAudio(RecordAudioPermission.isGranted(this), audioInitialized)) {
            Log.w(TAG, "Not starting recording: permission denied or AudioRecord uninitialised");
            Toast.makeText(this, "Failed to initialize audio recorder. Please check app permissions in Settings.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            audio.startRecording();
            mHandler.post(recorderRunnable);

            recording = true;
            try {
                LocationServices.FusedLocationApi.requestLocationUpdates(mService.getLocationClient(), locationRequest, this);
            } catch (SecurityException e) {
                Log.e(TAG, "Location permission denied: " + e.getMessage());
            }

            Toast.makeText(this, "Saving session to " + getFilePathForSession(), Toast.LENGTH_LONG).show();

            try {
                // Create directory structure
                File directory = new File(Utils.getSaveDirPath(this));
                boolean dirCreated = directory.exists() || directory.mkdirs();

                if (!dirCreated) {
                    Log.e(TAG, "Failed to create directory: " + directory.getAbsolutePath());
                    Toast.makeText(this, "Failed to create directory for saving data", Toast.LENGTH_LONG).show();
                    return;
                }

                // Verify directory exists before creating file
                if (!directory.exists()) {
                    Log.e(TAG, "Directory does not exist after creation attempt: " + directory.getAbsolutePath());
                    Toast.makeText(this, "Storage directory unavailable", Toast.LENGTH_LONG).show();
                    return;
                }

                String path = getFilePathForSession();
                Log.d(TAG, "Creating file at: " + path);
                wr = new CSVWriter(new FileWriter(path));
                wr.writeNext(new String[]{"magnitude", "latitude", "longitude", "accuracy", "timestamp"});
            } catch (IOException e) {
                Log.e(TAG, "Error creating file: " + e.getMessage(), e);
                e.printStackTrace();
                Toast.makeText(this, "Error creating file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } catch (IllegalStateException e) {
            Log.e(TAG, "Error starting recording: " + e.getMessage());
            e.printStackTrace();
            audio = null;
            recording = false;
            Toast.makeText(this, "Error starting recording", Toast.LENGTH_LONG).show();
        }
    }

    public String getFilePathForSession() {
        return new StringBuilder()
                .append(Utils.getSaveDirPath(this))
                .append(File.separator)
                .append("dump")
                .append("-")
                .append(
                        new SimpleDateFormat("yyyyMMddhhmmss")
                                .format(new Date())
                )
                .append(".csv")
                .toString();
    }

    public void stopRecording() {
        if (audio != null) {
            try {
                audio.stop();
                audio.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            audio = null;
        }

        recording = false;
        try {
            if (wr != null) {
                wr.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getDecibels(double level) {
        return Math.abs(20.0 * Math.log10(level / 0.00002)) + splAdjustment;
        //return (20.0 * Math.log10(level / 32767.0) + 120.0 + splAdjustment);
    }

    public boolean isRecording() {
        return recording;
    }

    @Override
    public void onLocationChanged(@androidx.annotation.NonNull Location location) {
        lastLocation = location;
        Log.d(TAG, "Location change: " + location);
    }

    public boolean isBound() {
        return mBound;
    }

    public class RecordBinder extends Binder {
        public RecordService getService() {
            return RecordService.this;
        }
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
