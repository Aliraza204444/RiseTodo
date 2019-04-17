package com.saratms.risetodo.Adapters;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.saratms.risetodo.Communicators.AddTodoListener;
import com.saratms.risetodo.Communicators.DeleteTodoListener;
import com.saratms.risetodo.Communicators.TodoChangeCountListener;
import com.saratms.risetodo.Communicators.TodoCheckListener;
import com.saratms.risetodo.Communicators.TodoClickListener;
import com.saratms.risetodo.Communicators.VoiceNoteClickListener;
import com.saratms.risetodo.Database.Todo;
import com.saratms.risetodo.MyDiffCallback;
import com.saratms.risetodo.R;
import com.saratms.risetodo.Utilities.DateUtils;

import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.animation.AnimationUtils.loadAnimation;

/**
 * Created by Sarah Al-Shamy on 20/11/2018.
 */

public class TodoRecyclerAdapter extends RecyclerView.Adapter<TodoRecyclerAdapter.TodoViewHolder> {

    private List<Todo> mTodolist;
    private Context mContext;
    private DeleteTodoListener mDeleteListener;
    private TodoClickListener mTodoClickListener;
    private TodoChangeCountListener mTodoCountChangeListener;
    private TodoCheckListener mTodoCheckListener;
    private AddTodoListener mAddListener;
    private VoiceNoteClickListener mVoiceNoteClickListener;

    private int lastPosition = -1;

    public TodoRecyclerAdapter(Context context, List<Todo> todolist, AddTodoListener addListener, DeleteTodoListener deleteListener, TodoClickListener clickListener, TodoChangeCountListener todoChangeCountListener, TodoCheckListener todoCheckListener, VoiceNoteClickListener voiceNoteClickListener) {
        this.mContext = context;
        this.mTodolist = todolist;
        this.mAddListener = addListener;
        this.mDeleteListener = deleteListener;
        this.mTodoClickListener = clickListener;
        this.mTodoCountChangeListener = todoChangeCountListener;
        this.mTodoCheckListener = todoCheckListener;
        this.mVoiceNoteClickListener = voiceNoteClickListener;

    }

    @NonNull
    @Override
    public TodoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.todo_card, parent, false);
        return (new TodoViewHolder(view));
    }

    @Override
    public void onBindViewHolder(@NonNull final TodoRecyclerAdapter.TodoViewHolder holder, final int position) {
        final Todo currentTodo = mTodolist.get(position);

        holder.doneCheckbox.setChecked(currentTodo.isChecked());

        if (holder.doneCheckbox.isChecked()) {
            if (!TextUtils.isEmpty(currentTodo.getAdditionalNotes())) {
                //To put strike through the text checked is done
                holder.toDoNotesTextView.setPaintFlags(holder.toDoNotesTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
            holder.toDoTextView.setPaintFlags(holder.toDoTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            //To remove the strike if the to-do got unchecked
            holder.toDoNotesTextView.setPaintFlags(holder.toDoNotesTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.toDoTextView.setPaintFlags(holder.toDoTextView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        String voiceNotePath = currentTodo.getVoiceNotePath();
        if (voiceNotePath == null) {
            holder.voiceNoteButton.setVisibility(View.GONE);
        } else {
            holder.voiceNoteButton.setVisibility(View.VISIBLE);
//            if(holder.voiceNoteButton.hasFocus()){
//                holder.voiceNoteButton.setCompoundDrawables(mContext.getResources().getDrawable(R.drawable.ic_pause_24dp), null, null, null);
//            }else{holder.voiceNoteButton.setCompoundDrawables(mContext.getResources().getDrawable(R.drawable.ic_play_arrow_blue_24dp), null, null, null);}
            holder.voiceNoteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//
                    if (holder.voiceNoteButton.hasFocus()) {
                        holder.voiceNoteButton.clearFocus();
                    } else {
                        holder.voiceNoteButton.setFocusable(true);
                        holder.voiceNoteButton.setFocusableInTouchMode(true);
                        holder.voiceNoteButton.requestFocus();
                    }
                }
            });

            holder.voiceNoteButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        String filePath = currentTodo.getVoiceNotePath();
                        Drawable img = mContext.getResources().getDrawable(R.drawable.ic_volume_up_blue_24dp);
                        holder.voiceNoteButton.setBackgroundResource(R.drawable.circle_stroke_blue);
                        holder.voiceNoteButton.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                        mVoiceNoteClickListener.onVoiceNoteClick(filePath, holder.voiceNoteButton, false);
                        Log.d("onFocusChange: ", "has focus");
                    } else {
                        String filePath = currentTodo.getVoiceNotePath();
                        Drawable img = mContext.getResources().getDrawable(R.drawable.ic_volume_mute_24dp);
                        holder.voiceNoteButton.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                        holder.voiceNoteButton.setBackgroundResource(R.drawable.circle_stroke_gray);
                        holder.voiceNoteButton.setFocusable(false);
                        holder.voiceNoteButton.setFocusableInTouchMode(false);
                        mVoiceNoteClickListener.onVoiceNoteClick(filePath, holder.voiceNoteButton, true);
                        Log.d("onFocusChange: ", "doesnt has focus");
                    }
                }
            });
        }


        if (TextUtils.isEmpty(currentTodo.getAdditionalNotes())) {
            holder.toDoNotesTextView.setVisibility(View.GONE);
        } else {
            holder.toDoNotesTextView.setText(currentTodo.getAdditionalNotes());
            holder.toDoNotesTextView.setVisibility(View.VISIBLE);
        }

        if (TextUtils.isEmpty(currentTodo.getReminderTime())) {
            holder.todoReminderTv.setVisibility(View.GONE);
        } else if (!TextUtils.isEmpty(currentTodo.getReminderTime()) && Long.parseLong(currentTodo.getReminderTime()) > System.currentTimeMillis()) {
            holder.todoReminderTv.setVisibility(View.VISIBLE);
            Calendar reminderCalendar = DateUtils.convertMillisecondsToCalendar(currentTodo.getReminderTime());
            String reminderTime = DateUtils.formatReminderTime(reminderCalendar);
            holder.todoReminderTv.setText(reminderTime);
        } else {
            holder.todoReminderTv.setVisibility(View.GONE);
        }

        holder.toDoTextView.setText(currentTodo.getDescription());

        String dateString = currentTodo.getDate();

        //If there's two adjacent to-dos with the same due date, this will make only the first one to display it
        if (!TextUtils.isEmpty(dateString) || dateString != null) {
            if (position == 0) {
                Date date = DateUtils.getDateFromString(dateString);
                String day = DateUtils.getDayFromDate(date);
                String month = DateUtils.getMonthFromDate(date);
                holder.toDoDueDayTv.setText(day);
                holder.toDoDueMonthTv.setText(month);
            } else if (dateString.equals(mTodolist.get(position - 1).getDate())) {
                holder.toDoDueDayTv.setText(" ");
                holder.toDoDueMonthTv.setText(" ");
            } else {
                Date date = DateUtils.getDateFromString(dateString);
                String day = DateUtils.getDayFromDate(date);
                String month = DateUtils.getMonthFromDate(date);
                holder.toDoDueDayTv.setText(day);
                holder.toDoDueMonthTv.setText(month);
            }
            setAnimation(holder.itemView, position);
        } else {
            if (position == 0) {
                holder.toDoDueMonthTv.setText("N/A");
                holder.toDoDueDayTv.setText("");
            } else {
                holder.toDoDueMonthTv.setText("");
                holder.toDoDueDayTv.setText("");
            }

        }

        setAnimation(holder.container, position);

        holder.doneCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.doneCheckbox.isChecked()) {
                    currentTodo.setChecked(true);
                    mTodoCheckListener.onCheck(currentTodo);
                } else {
                    currentTodo.setChecked(false);
                    mTodoCheckListener.onCheck(currentTodo);
                }
            }
        });

        holder.container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PopupMenu popupMenu = new PopupMenu(mContext, holder.todoOptionsButton);
                popupMenu.getMenuInflater().inflate(R.menu.to_do_options, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case (R.id.delete_todo): {
                                holder.container.animate().translationX(holder.container.getWidth() + 30).setDuration(200).withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        mDeleteListener.onDelete(currentTodo);
                                        holder.container.animate().setStartDelay(1000).translationX(0).start();
                                    }
                                }).start();
                            }
                            break;
                        }
                        return true;
                    }
                });
                popupMenu.show();

                return true;
            }
        });
    }


    public void setData(List<Todo> newTodoList, Boolean isDated) {

        Todo todoAdded = null;

        // Check for new item added by checking the new list size, if so, get the last item added
        // to search for its index later after sorting the list
        // to animate its insertion
        if (newTodoList != null && mTodolist != null) {
            if (newTodoList.size() > mTodolist.size()) {
                todoAdded = newTodoList.get(newTodoList.size() - 1);
                Log.d("old list size", String.valueOf(mTodolist.size()));
                Log.d("new list size", String.valueOf(newTodoList.size()));

                for (int i = 0; i < newTodoList.size(); i++) {
                    Log.d("items in list", newTodoList.get(i).getAdditionalNotes().toString() + " " + String.valueOf(newTodoList.get(i).getId()));
                }

            }
        }

        // If the to-do has a due date, sort them by their dates on ascending order
        if (isDated) {
            Collections.sort(newTodoList, new Comparator<Todo>() {
                public int compare(Todo o1, Todo o2) {
                    if (DateUtils.getDateFromString(o1.getDate()) == null || DateUtils.getDateFromString(o2.getDate()) == null)
                        return 0;
                    return DateUtils.getDateFromString(o1.getDate()).compareTo(DateUtils.getDateFromString(o2.getDate()));
                }
            });
        }

        // Get the index of the added to-do after sorting
        if (todoAdded != null) {
            lastPosition = newTodoList.indexOf(todoAdded);
            mAddListener.onAdd(isDated, lastPosition);
        }

        updateList(newTodoList);

    }

    public void updateList(List<Todo> newList) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new MyDiffCallback(mTodolist, newList));
        mTodolist = newList;
        diffResult.dispatchUpdatesTo(this);
        mTodoCountChangeListener.onCountChange();
    }

    public void setAnimation(View viewToAnimate, int position) {
        // If the bound view wasn't previously displayed on screen, show it animated
        if (position == lastPosition) {
            Animation animation = loadAnimation(mContext, R.anim.slide_from_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = -1;
        }
    }

    @Override
    public int getItemCount() {

        if (mTodolist != null) {
            return mTodolist.size();
        } else {
            return 0;
        }
    }

    public class TodoViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.to_do_tv)
        TextView toDoTextView;
        @BindView(R.id.to_do_notes_tv)
        TextView toDoNotesTextView;
        @BindView(R.id.to_do_due_month_tv)
        TextView toDoDueMonthTv;
        @BindView(R.id.to_do_due_day_tv)
        TextView toDoDueDayTv;
        @BindView(R.id.to_do_reminder_tv)
        TextView todoReminderTv;
        @BindView(R.id.todo_done_checkbox)
        CheckBox doneCheckbox;
        @BindView(R.id.to_do_options_button)
        ImageView todoOptionsButton;
        @BindView(R.id.voice_note_button)
        Button voiceNoteButton;

        View container;

        public TodoViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            container = itemView;

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mTodoClickListener.onTodoClick(mTodolist.get(getAdapterPosition()));
                }
            });
        }

        public void clearAnimation() {
            container.clearAnimation();
        }
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull TodoViewHolder holder) {
        holder.clearAnimation();
    }
}

