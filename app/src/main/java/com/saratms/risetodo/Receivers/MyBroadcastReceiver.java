package com.saratms.risetodo.Receivers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;

import com.saratms.risetodo.Activities.MainActivity;
import com.saratms.risetodo.R;
import com.saratms.risetodo.Utilities.DateUtils;

import java.util.Date;

import static com.saratms.risetodo.App.CHANNEL_1_ID;

public class MyBroadcastReceiver extends BroadcastReceiver {
    private NotificationManagerCompat notificationManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        notificationManager = NotificationManagerCompat.from(context);

        String todoBody = intent.getStringExtra("todo_body");
        String todoNotes = intent.getStringExtra("todo_notes");
        String todoVoiceNotePath = intent.getStringExtra("todo_voice");
        String todoDueDateString = intent.getStringExtra("todo_duedate");
        int todoId = intent.getIntExtra("todo_id", 0);
        sendOnChannel1(context, todoBody, todoNotes, todoVoiceNotePath, todoDueDateString, todoId);
    }

    public void sendOnChannel1(Context context, String todoBody, String todoNotes, String todoVoiceNote, String todoDueDateString, int notificationId) {

        Intent openAppIntent = new Intent(context, MainActivity.class);
        PendingIntent openAppPendingIntent = PendingIntent.getActivity(context, 000, openAppIntent, 0);

        Intent dismissIntent = new Intent(context, NotificationActionReceiver.class);
        dismissIntent.setAction("dismiss");
        dismissIntent.putExtra("notification_id", notificationId);
        PendingIntent dismissIntentPendingIntent = PendingIntent.getBroadcast(context, notificationId, dismissIntent, 0);

        Intent playIntent = new Intent(context, NotificationActionReceiver.class);
        playIntent.setAction("play");
        playIntent.putExtra("notification_voice_path", todoVoiceNote);
        PendingIntent playVoiceNotePendingIntent = PendingIntent.getBroadcast(context, notificationId, playIntent, 0);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_1_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentText(todoBody)
                .setContentIntent(openAppPendingIntent)
                .setOngoing(true)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.drawable.sun_logo))
                .setAutoCancel(true)
                .addAction(0, "DISMISS", dismissIntentPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        if (!TextUtils.isEmpty(todoNotes)) {
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(todoBody + "\n" + "Notes: " + todoNotes));
        } else {
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(todoBody));
        }

        if (!TextUtils.isEmpty(todoDueDateString)) {
            Date todoDueDate = DateUtils.getDateFromString(todoDueDateString);
            String month = DateUtils.getLargeMonthFromDate(todoDueDate);
            String day = DateUtils.getDayFromDate(todoDueDate);
            notificationBuilder.setContentTitle("Upcoming Task: " + day + " " + month);
        } else {
            notificationBuilder.setContentTitle("Upcoming Task:");
        }

        if (!TextUtils.isEmpty(todoVoiceNote)) {
            notificationBuilder.addAction(0, "Play Voice Note", playVoiceNotePendingIntent);
        }

        Notification notification = notificationBuilder.build();
        notificationManager.notify(notificationId, notification);
    }
}  