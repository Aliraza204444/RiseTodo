package com.saratms.risetodo.Fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.saratms.risetodo.Communicators.AddTodoListener;
import com.saratms.risetodo.Communicators.DeleteTodoListener;
import com.saratms.risetodo.Communicators.TodoChangeCountListener;
import com.saratms.risetodo.Communicators.TodoCheckListener;
import com.saratms.risetodo.Communicators.TodoClickListener;
import com.saratms.risetodo.Communicators.VoiceNoteClickListener;
import com.saratms.risetodo.Database.Todo;
import com.saratms.risetodo.Database.TodoViewModel;
import com.saratms.risetodo.R;
import com.saratms.risetodo.Receivers.MyBroadcastReceiver;
import com.saratms.risetodo.Adapters.TodoRecyclerAdapter;
import com.saratms.risetodo.Utilities.SharedPrefUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by Sarah Al-Shamy on 20/11/2018.
 */

public class TodoListFragment extends Fragment implements AddTodoBottomSheetFragment.SubmitTodoListener {

    @BindView(R.id.recycler_with_due_date)
    RecyclerView dateRecyclerView;
    @BindView(R.id.recycler_no_due_date)
    RecyclerView noDateRecyclerView;
    @BindView(R.id.empty_view)
    View emptyView;
    @BindView(R.id.parent_layout)
    ConstraintLayout parentLayout;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    TodoRecyclerAdapter todoRecyclerAdapter;
    TodoRecyclerAdapter noDatedTodoRecyclerAdapter;
    List<Todo> mDatedTodoList;
    List<Todo> mNoDateTodoList;

    DeleteTodoListener deleteListener;
    TodoClickListener todoClickListener;
    TodoChangeCountListener countListener;
    TodoCheckListener todoCheckListener;
    AddTodoListener addTodoListener;
    VoiceNoteClickListener voiceNoteClickListener;

    RequestScrollListener requestScrollListener;

    TodoViewModel todoViewModel;
    MediaPlayer mp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.todo_list_fragment, container, false);
        ButterKnife.bind(this, view);

        setupListeners();
        setupAdapters();

        //Setup ViewModels and their observers
        todoViewModel = ViewModelProviders.of(this).get(TodoViewModel.class);
        todoViewModel.getAllTodos().observe(this, todos -> todoRecyclerAdapter.setData(todos, true));
        todoViewModel.getAllNoDateTodos().observe(this, todos -> noDatedTodoRecyclerAdapter.setData(todos, false));

        dateRecyclerView.setVisibility(View.VISIBLE);
        noDateRecyclerView.setVisibility(View.VISIBLE);
        return view;
    }

    public void setupAdapters() {
        //Setup adapter and layout manager for to-do list with due date
        //Set ItemAnimator to null, in order to implement our own animation
        todoRecyclerAdapter = new TodoRecyclerAdapter(getContext(), mDatedTodoList, addTodoListener, deleteListener, todoClickListener, countListener, todoCheckListener, voiceNoteClickListener);
        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        dateRecyclerView.setLayoutManager(manager);
        dateRecyclerView.setAdapter(todoRecyclerAdapter);
        dateRecyclerView.setItemAnimator(null);

        //Setup adapter and layout manager for to-do list with no due date
        //Set ItemAnimator to null, in order to implement our own animation
        noDatedTodoRecyclerAdapter = new TodoRecyclerAdapter(getContext(), mNoDateTodoList, addTodoListener, deleteListener, todoClickListener, countListener, todoCheckListener, voiceNoteClickListener);
        noDateRecyclerView.setAdapter(noDatedTodoRecyclerAdapter);
        LinearLayoutManager manager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        noDateRecyclerView.setLayoutManager(manager2);
        noDateRecyclerView.setItemAnimator(null);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        progressBar.setVisibility(View.GONE);
    }

    public void setupListeners() {
        deleteListener = new DeleteTodoListener() {
            @Override
            public void onDelete(Todo todo) {
                if (todo.getVoiceNotePath() != null) {
                    File deletedFile = new File(todo.getVoiceNotePath());
                    if (deletedFile.exists()) {
                        releaseSound();
                        deletedFile.delete();
                    }
                }

                //Plays delete sound if the sound state in setting is set to :yes
                if (SharedPrefUtils.getSoundSharedPref(getContext()).equals("yes")) {
                    releaseSound();
                    playSound(R.raw.delete_todo_sound);
                }

                //In case there's reminder set for this deleted to-do, cancel it
                if (!TextUtils.isEmpty(todo.getReminderTime())) {
                    cancelReminderAlarm(todo.getId());
                }

                todoViewModel.delete(todo);
            }
        };

        todoClickListener = new TodoClickListener() {
            @Override
            public void onTodoClick(Todo todo) {
                showBottomSheet(todo);
            }
        };

        //Listens for any change happen to the lists (the one with due date and the other with no due date)
        //To check their count, if both sizes are 0, means they are both empty, therefore show the empty view
        countListener = new TodoChangeCountListener() {
            @Override
            public void onCountChange() {
                int datedTodoCount = dateRecyclerView.getAdapter().getItemCount();
                int noDateTodoCount = noDateRecyclerView.getAdapter().getItemCount();
                if (datedTodoCount == 0 && noDateTodoCount == 0) {
                    emptyView.setVisibility(View.VISIBLE);
                } else {
                    emptyView.setVisibility(View.GONE);
                }
            }
        };

        //Listens for when the checkbox is clicked to update its state in the database
        todoCheckListener = new TodoCheckListener() {
            @Override
            public void onCheck(Todo todo) {
                todoViewModel.update(todo);
            }
        };

        //Listens for voice notes clicks to play the clicked voice
        voiceNoteClickListener = new VoiceNoteClickListener() {
            @Override
            public void onVoiceNoteClick(String filePath, Button playButton, boolean isAlreadyPlaying) {
                if (!isAlreadyPlaying) {
                    //Checks if the file exists in storage, and not deleted for some reason
                    File file = new File(filePath);
                    if (file.exists()) {
                        playVoiceNote(filePath, playButton);
                    } else {
                        releaseSound();
                        Toast.makeText(getContext(), R.string.file_not_found_toast, Toast.LENGTH_SHORT).show();
                        playButton.clearFocus();
                    }
                } else {
                    releaseSound();
                }
            }
        };

        //Listens for when a new to-do is added to trigger scrolling from the MainActivity to the added to-do position
        addTodoListener = new AddTodoListener() {
            @Override
            public void onAdd(boolean isDated, int position) {
                Log.d("todo added", "onAdd: " + position);
                if (isDated) {
                    dateRecyclerView.getParent().requestChildFocus(dateRecyclerView, dateRecyclerView);
                    dateRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            float y = dateRecyclerView.getY() + dateRecyclerView.getChildAt(position).getY();
                            requestScrollListener.onNeedScroll(y);
                            if (SharedPrefUtils.getSoundSharedPref(getContext()).equals("yes")) {
                                releaseSound();
                                playSound(R.raw.add_todo_sound);
                            }
                        }
                    }, 200);
                } else {
                    dateRecyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            requestScrollListener.onNeedScroll(-1);
                            if (SharedPrefUtils.getSoundSharedPref(getContext()).equals("yes")) {
                                releaseSound();
                                playSound(R.raw.add_todo_sound);
                            }
                        }
                    }, 200);
                }
            }
        };
    }

    public void showBottomSheet(Todo todo) {
        AddTodoBottomSheetFragment addTodoBottomSheetFragment = new AddTodoBottomSheetFragment();
        addTodoBottomSheetFragment.setTodo(todo);
        addTodoBottomSheetFragment.show(this.getChildFragmentManager(), "add_fragment_dialog");
    }


    public void addNewTodo(Todo todo) {
        todoViewModel.insert(todo);

        if (todo.getReminderTime() != null) {

            //This handler to give time to the to-do to be inserted before getting its id
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Long todoIdLong = todoViewModel.getLastId();
                    int todoId = todoIdLong.intValue();
                    setReminderAlarm(todoId, todo);
                }
            }, 1000);
        }

    }

    public void setReminderAlarm(int todoId, Todo todoAdded) {

        String todoBody = todoAdded.getDescription();
        String todoNotes = todoAdded.getAdditionalNotes();
        String todoVoiceNotePath = todoAdded.getVoiceNotePath();
        String todoDueDate = todoAdded.getDate();
        String milliseconds = todoAdded.getReminderTime();

        Intent intent = new Intent(getContext(), MyBroadcastReceiver.class);

        intent.putExtra("todo_body", todoBody);
        intent.putExtra("todo_notes", todoNotes);
        intent.putExtra("todo_duedate", todoDueDate);
        intent.putExtra("todo_voice", todoVoiceNotePath);


        //We pass the to-do id to broadcast receiver to act as unique character in setting up notifications
        //To set separate notification for each to-do reminder
        intent.putExtra("todo_id", todoId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext().getApplicationContext(), todoId, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, Long.parseLong(milliseconds), pendingIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, Long.parseLong(milliseconds), pendingIntent);
        }
    }

    public void cancelReminderAlarm(int todoId) {

        Intent intent = new Intent(getContext(), MyBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                getContext().getApplicationContext(), todoId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.cancel(pendingIntent);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        releaseSound();
        parentLayout.requestFocus();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            requestScrollListener = (RequestScrollListener) context;
        } catch (ClassCastException ex) {
        }
    }

    @Override
    public void onSubmitTodo(Todo todo, boolean isNewEntry, boolean isDueDateChangedFromNull, Todo oldTodo) {
        if (isNewEntry) {
            //Add the new object to notify ViewModel with its addition
            addNewTodo(todo);
        } else {
            // if the to-do didn't have due date at first and changed after to have one
            // delete it and insert another one with same data to have a new id so it won't mess things up
            if (isDueDateChangedFromNull) {
                //if the old to-do had a reminder time set, cancel it
                if (!TextUtils.isEmpty(oldTodo.getReminderTime())) {
                    cancelReminderAlarm(oldTodo.getId());
                }
                todoViewModel.delete(oldTodo);
                addNewTodo(todo);
            } else {
                todoViewModel.update(todo);
                if (!TextUtils.isEmpty(todo.getReminderTime())) {
                    setReminderAlarm(todo.getId(), todo);
                } else {
                    cancelReminderAlarm(todo.getId());
                }
            }
        }
    }

    public interface RequestScrollListener {
        void onNeedScroll(float yPosition);
    }

    public void playSound(int resId) {
        mp = MediaPlayer.create(getContext(), resId);
        if (mp != null) {
            mp.start();
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    releaseSound();
                }
            });
        }
    }

    public void playVoiceNote(String filePath, Button playButton) {

        releaseSound();
        mp = new MediaPlayer();

        try {
            mp.setDataSource(filePath);
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mp != null) {
            mp.start();
        }

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                releaseSound();
                //This will remove focus from the play button and avoid gaining focus by the next view
                parentLayout.requestFocus();
            }
        });
    }

    public void releaseSound() {
        if (mp != null) {
            mp.release();
            mp = null;
        }
    }
}
