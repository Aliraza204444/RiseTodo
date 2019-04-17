package com.saratms.risetodo.Receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;

import com.saratms.risetodo.Database.Todo;
import com.saratms.risetodo.Database.TodoDAO;
import com.saratms.risetodo.Database.TodoDatabase;

import java.util.List;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by Sarah Al-Shamy on 31/03/2019.
 */

public class BootCompletedReciever extends BroadcastReceiver {
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            this.context = context;
            TodoDatabase database = TodoDatabase.getDatabase(context);
            TodoDAO todoDAO = database.getTodoDAO();

            GetTodosAsyncTask getTodosAsyncTask = new GetTodosAsyncTask(todoDAO);
            getTodosAsyncTask.execute();
        }
    }

    private class GetTodosAsyncTask extends AsyncTask<Void, Void, List<Todo>> {

        private TodoDAO mAsyncTaskDao;

        GetTodosAsyncTask(TodoDAO dao) {
            mAsyncTaskDao = dao;
        }

        @Override
        protected List<Todo> doInBackground(Void... voids) {
            return mAsyncTaskDao.getTodos();
        }

        @Override
        protected void onPostExecute(List<Todo> allTodos) {
            super.onPostExecute(allTodos);

            //Reset alarms after device reboot
            //Loop through all the to-dos, and set reminder for the ones with reminderTime larger than currentTime
            for(Todo todo : allTodos){
                if(!TextUtils.isEmpty(todo.getReminderTime()) && Long.parseLong(todo.getReminderTime()) > System.currentTimeMillis()){
                    setReminderAlarm(context, todo.getId(), todo);
                }
            }

        }
    }

    public void setReminderAlarm(Context context, int todoId, Todo todoAdded) {

        String todoBody = todoAdded.getDescription();
        String todoNotes = todoAdded.getAdditionalNotes();
        String todoVoiceNotePath = todoAdded.getVoiceNotePath();
        String todoDueDate = todoAdded.getDate();
        String milliseconds = todoAdded.getReminderTime();

        Intent intent = new Intent(context, MyBroadcastReceiver.class);

        intent.putExtra("todo_body", todoBody);
        intent.putExtra("todo_notes", todoNotes);
        intent.putExtra("todo_duedate", todoDueDate);
        intent.putExtra("todo_voice", todoVoiceNotePath);


        //We pass the to-do id to broadcast receiver to act as unique character in setting up notifications
        //To set separate notification for each to-do reminder
        intent.putExtra("todo_id", todoId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context.getApplicationContext(), todoId, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, Long.parseLong(milliseconds), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, Long.parseLong(milliseconds), pendingIntent);
        }
    }
}
