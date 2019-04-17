package com.saratms.risetodo;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.Toast;

import com.saratms.risetodo.Utilities.SharedPrefUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sarah Al-Shamy on 12/03/2019.
 */

public class RecorderAsset {

    private static final String AUDIO_RECORDER_FOLDER = "RiseToDo";
    private MediaRecorder recorder;
    private String fileFinalName;
    MediaPlayer mPlayer;
    Context context;

    public RecorderAsset(Context context) {
        this.context = context;
    }

    // Named according to current time, so we only called it once at creation to set file name
    // If we want to get file name later, we use getFileName() method
    public String getFilenameAtCreation() {
        String filepath = Environment.getExternalStorageDirectory().getPath();
        File file = new File(filepath, AUDIO_RECORDER_FOLDER);

        if (!file.exists()) {
            file.mkdirs();
        }

        return (file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".mp3");
    }

    //To set it manually from outside class
    public void setFileName(String fileName) {
        this.fileFinalName = fileName;
    }

    public void startRecording(Chronometer recordChronometer) {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        fileFinalName = getFilenameAtCreation();
        recorder.setOutputFile(fileFinalName);

        if (SharedPrefUtils.getSoundSharedPref(context).equals("yes")) {
            //In order to not have the recorder sound recorded on the voice note
            //We should start recording after the sound finish playing
            //This is why we called the record() from inside the playRecordWithSound() method
            recordWithSound(R.raw.record_voice, recordChronometer);
        } else {
            //If the sound is off, start recording directly without playing sound
            record(recordChronometer);
        }
    }

    public void record(Chronometer recordChronometer) {
        try {
            //Start the chronometer
            recordChronometer.setBase(SystemClock.elapsedRealtime());
            recordChronometer.setVisibility(View.VISIBLE);
            recordChronometer.start();

            //Start the recorder
            recorder.prepare();
            recorder.start();

        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopRecording(Chronometer recordChronometer) {
        try {
            recorder.stop();
            recorder.release();

            //stop the chronometer
            recordChronometer.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        recorder = null;

        if (SharedPrefUtils.getSoundSharedPref(context).equals("yes")) {
            endRecordSound(R.raw.end_record_beep);
        }
    }


    public String getFileName() {
        return fileFinalName;
    }

    //Deletes the file instantiated inside this class
    public void deleteFileFromStorage() {
        if (fileFinalName != null) {
            File deletedFile = new File(fileFinalName);
            if (deletedFile.exists()) {
                deletedFile.delete();
            }
            fileFinalName = null;
        }
    }

    //Deletes the file which is passed to the method through its path
    public void deleteFileFromStorage(String filePath) {
        if (filePath != null) {
            File deletedFile = new File(filePath);
            if (deletedFile.exists()) {
                deletedFile.delete();
            }
        }
    }


    public void animateAndVibrateButton(ImageButton recordIv) {

        final Animation myAnim = AnimationUtils.loadAnimation(context, R.anim.bounce);

        // Use bounce interpolator with amplitude 0.2 and frequency 20
        MyBounceInterpolator interpolator = new MyBounceInterpolator(0.2, 20);
        myAnim.setInterpolator(interpolator);

        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        // Vibrate for 500 milliseconds
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            //deprecated in API 26
            v.vibrate(100);
        }

        recordIv.startAnimation(myAnim);
    }

    public void playVoiceNote(String filePath, Button playButton) {

        releaseSound();

        if (filePath != null) {
            File file = new File(filePath);
            if (file.exists()) {
                mPlayer = new MediaPlayer();
                try {
                    mPlayer.setDataSource(filePath);
                    mPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (mPlayer != null) {
                    mPlayer.start();
                    mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mp) {
                            releaseSound();
                            playButton.setText("PLAY");
                            playButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow_blue_24dp, 0, 0, 0);

                        }
                    });
                }
            } else {
                playButton.setText("PLAY");
                playButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow_blue_24dp, 0, 0, 0);
                Toast.makeText(context, R.string.file_not_found_toast, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getDurationOfVoiceNote() {
        mPlayer = new MediaPlayer();
        try {
            if (!TextUtils.isEmpty(fileFinalName)) {
                File file = new File(fileFinalName);
                if (file.exists()) {
                    mPlayer.setDataSource(fileFinalName);
                    mPlayer.prepare();
                } else {
                    mPlayer = null;
                }
            } else {
                mPlayer = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mPlayer != null) {
            int duration = mPlayer.getDuration();
            return String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1)
                    , TimeUnit.MILLISECONDS.toSeconds(duration) % TimeUnit.MINUTES.toSeconds(1));
        } else {
            return null;
        }
    }

    public void releaseSound() {
        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void recordWithSound(int rawResId, Chronometer recordChronometer) {
        releaseSound();
        mPlayer = MediaPlayer.create(context, rawResId);
        if (mPlayer != null) {
            mPlayer.start();
            mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    releaseSound();
                    record(recordChronometer);
                }
            });
        } else {
            record(recordChronometer);
        }
    }

    public void endRecordSound(int rawResId) {
        releaseSound();
        mPlayer = MediaPlayer.create(context, rawResId);
        if(mPlayer!= null){
        mPlayer.start();
        mPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                releaseSound();
            }
        });
    }}
}
