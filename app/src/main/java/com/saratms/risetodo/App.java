package com.saratms.risetodo;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import java.util.List;

public class App extends Application {
    public static final String CHANNEL_1_ID = "Reminder Notifications";
 
    @Override
    public void onCreate() {
        super.onCreate();
 
        createNotificationChannels();
    }
 
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "Reminder Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is Channel for App Reminder notifications");

            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();

            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            channel1.enableVibration(true);
            channel1.setSound(alarmSound, attributes);
 
            NotificationManager manager = getSystemService(NotificationManager.class);

            if (manager != null) {
                List<NotificationChannel> channelList = manager.getNotificationChannels();

                for (int i = 0; channelList != null && i < channelList.size(); i++) {
                    manager.deleteNotificationChannel(channelList.get(i).getId());
                }
            }
            manager.createNotificationChannel(channel1);
        }
    }
}