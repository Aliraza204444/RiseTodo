package com.saratms.risetodo.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v4.app.NotificationManagerCompat;
import android.widget.Toast;

import com.saratms.risetodo.R;

import java.io.File;
import java.io.IOException;

public class NotificationActionReceiver extends BroadcastReceiver {

    MediaPlayer mediaPlayer;

    @Override
    public void onReceive(Context context, Intent intent) {

        switch (intent.getAction()){
            case "dismiss":
            {
                int notificationId = intent.getIntExtra("notification_id", 0);
                cancelNotification(context, notificationId);
                break;
            }
            case "play":
            {
                String voiceNotePath = intent.getStringExtra("notification_voice_path");
                if(voiceNotePath!= null){
                    File file = new File(voiceNotePath);
                    if (file.exists()){
                        playVoiceNote(voiceNotePath);
                        break;
                    }
                    else{
                        Toast.makeText(context, R.string.file_not_found_toast, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

    }

    public void cancelNotification(Context context, int notificationId){
        //Cancel notification
        NotificationManagerCompat manager = (NotificationManagerCompat) NotificationManagerCompat.from(context);
        manager.cancel(notificationId);
    }

    public void playVoiceNote(String filePath){
        releaseSound();
        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                releaseSound();
            }
        });
    }

    public void releaseSound() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}