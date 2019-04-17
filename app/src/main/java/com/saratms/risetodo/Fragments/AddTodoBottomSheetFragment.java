package com.saratms.risetodo.Fragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.saratms.risetodo.Database.Todo;
import com.saratms.risetodo.R;
import com.saratms.risetodo.RecorderAsset;
import com.saratms.risetodo.ReminderAsset;
import com.saratms.risetodo.Utilities.DateUtils;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.Calendar;

/**
 * Created by Sarah Al-Shamy on 16/04/2019.
 */

public class AddTodoBottomSheetFragment extends BottomSheetDialogFragment {

    EditText todoBodyEt, todoNotesEt;
    TextView dueDateTv, mandatoryFieldTv, addVoiceTv, holdToRecordTv, durationTv, addReminderTv, reminderTv;
    Button submitTodoButton, playButton, resetButton, setReminderButton, removeReminderButton;
    ImageView dismissDialogButton;
    ImageButton recordIv;
    Chronometer recordChronometer;
    RelativeLayout recordDoneLayout, recordLayout;
    ExpandableLayout addVoiceExpandableLayout, reminderExpandableLayout;

    SubmitTodoListener mSubmitTodoListener;

    private static final int RECORD_AUDIO_REQUEST_CODE = 55;

    ReminderAsset reminderAsset;
    RecorderAsset recorderAsset;

    Todo todoToBeUpdated;

    boolean isDated = false;

    // Used to check If the to-do had a previously recorded voice
    // It helps in case the user made changes to the recorded voice but didn't submit changes,
    // if it returns true, file won't be deleted till change has been submitted, otherwise we retrieve it
    boolean hasVoiceInitially = false;
    String previousFilePath = null;
    boolean hasOriginalVoiceChanged = false;


    // To check if changes has been submitted or dismissed, to deal with recorded voice
    // to delete voice from storage in case changes has not been submitted
    boolean isSubmitted = false;

    // To identify collapsed layout state which is altered by clicking on add voice textview and add reminder textview
    boolean isAddVoiceCollapsed = true;
    boolean isAddReminderCollapsed = true;

    // To check if the voice note in the dialog is already playing when click the play button, if so pause it
    boolean isPlayed = false;

    public AddTodoBottomSheetFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_todo_dialog, container, false);

        getDialog().setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                //This is will make the dialog show fully when it's opened
                BottomSheetDialog d = (BottomSheetDialog) dialog;
                FrameLayout bottomSheet = (FrameLayout) d.findViewById(android.support.design.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        initViews(view);

        reminderAsset = new ReminderAsset(getContext(), reminderTv, setReminderButton, removeReminderButton);
        recorderAsset = new RecorderAsset(getContext());

        setDialogFieldsWithTodoProperties();
        setupExpandablesClickListener();
        setupButtonsClickListener();
        setupEditTexts();

        return view;
    }

    //We don't use onAttach because we don't want to attach it to an activity
    //rather, we want to attach it to its parent fragment
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            mSubmitTodoListener = (SubmitTodoListener) getParentFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement Callback interface");
        }
    }

    public void initViews(View view) {
        todoBodyEt = view.findViewById(R.id.add_todo_et);
        todoNotesEt = view.findViewById(R.id.add_todo_notes_et);
        dueDateTv = view.findViewById(R.id.add_todo_duedate_tv);
        submitTodoButton = view.findViewById(R.id.submit_todo_button);
        dismissDialogButton = view.findViewById(R.id.dismiss_dialog_button);
        mandatoryFieldTv = view.findViewById(R.id.mandatory_field_tv);

        addVoiceTv = view.findViewById(R.id.add_voice_tv);
        recordIv = view.findViewById(R.id.add_todo_record_iv);
        recordChronometer = view.findViewById(R.id.record_chronometer);
        holdToRecordTv = view.findViewById(R.id.hold_record_tv);

        recordDoneLayout = view.findViewById(R.id.recorded_done_layout);
        recordLayout = view.findViewById(R.id.record_layout);
        playButton = view.findViewById(R.id.play_button);
        resetButton = view.findViewById(R.id.reset_button);
        durationTv = view.findViewById(R.id.voice_duration_tv);
        addVoiceExpandableLayout = view.findViewById(R.id.expandableLayout);

        addReminderTv = view.findViewById(R.id.add_reminder_tv);
        reminderTv = view.findViewById(R.id.reminder_tv);
        setReminderButton = view.findViewById(R.id.set_reminder_button);
        removeReminderButton = view.findViewById(R.id.remove_reminder_button);
        reminderExpandableLayout = view.findViewById(R.id.expandable_reminder_layout);
    }

    //This method is called from outside in case it's the edit dialog
    //we set the member class instance to the passed one
    public void setTodo(Todo todo) {
        if (todo != null) {
            todoToBeUpdated = todo;
        }
    }

    //This will only be called in case the user click on te to-do to update it
    //it will update the dialog fields with the to-do properties
    public void setDialogFieldsWithTodoProperties() {
        if (todoToBeUpdated != null) {

            submitTodoButton.setText("Submit");
            todoNotesEt.setText(todoToBeUpdated.getAdditionalNotes());
            todoBodyEt.setText(todoToBeUpdated.getDescription());
            dueDateTv.setText(todoToBeUpdated.getDate());

            if (todoToBeUpdated.getDate() == null) {
                isDated = false;
            } else {
                isDated = true;
            }
        }

        initDialogState();
    }


    public void initDialogState() {
        initVoiceLayout();
        initReminderLayout();
    }

    public void initVoiceLayout() {
        if (todoToBeUpdated != null) {
            if (!TextUtils.isEmpty(todoToBeUpdated.getVoiceNotePath())) {
                previousFilePath = todoToBeUpdated.getVoiceNotePath();
                recorderAsset.setFileName(previousFilePath);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        addVoiceExpandableLayout.expand();
                    }
                }, 400);

                // Expand the expandable layout, because there is a voice note already saved
                addVoiceTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_keyboard_arrow_down_24dp, 0, 0, 0);
                isAddVoiceCollapsed = false;

                //This is the only chance this boolean is set to true, if there was initially a previous saved voice note
                hasVoiceInitially = true;

                String duration = recorderAsset.getDurationOfVoiceNote();
                if (duration != null) {
                    durationTv.setText(duration);
                } else {
                    durationTv.setText("00:00");
                }

                recordDoneLayout.setVisibility(View.VISIBLE);
                recordLayout.setVisibility(View.GONE);
            } else {
                addVoiceExpandableLayout.collapse();
                isAddVoiceCollapsed = true;
                addVoiceTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_keyboard_arrow_right_24dp, 0, 0, 0);
            }
        }
    }

    public void initReminderLayout() {
        //In case the to-do wasn't null (meaning this is the edit case), check if it has reminder already set
        //If so, pass it to the reminder asset and update ui of the dialog accordingly
        if (todoToBeUpdated != null) {
            if (!TextUtils.isEmpty(todoToBeUpdated.getReminderTime())) {
                Calendar calendar = DateUtils.convertMillisecondsToCalendar(todoToBeUpdated.getReminderTime());
                reminderAsset.setCalendar(calendar);
                reminderAsset.updateUi();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        reminderExpandableLayout.expand();
                    }
                }, 400);
                isAddReminderCollapsed = false;
                addReminderTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_keyboard_arrow_down_24dp, 0, 0, 0);
            } else {
                reminderExpandableLayout.collapse();
                isAddReminderCollapsed = true;
                addReminderTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_keyboard_arrow_right_24dp, 0, 0, 0);
            }
        }
    }

    public void setupExpandablesClickListener() {
        setupVoiceExpandable();
        setupReminderExpandable();
    }

    public void setupVoiceExpandable() {
        //This will handle clicks on the "add voice textview" to either collapse or expand the expandable layout
        addVoiceTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAddVoiceCollapsed) {
                    addVoiceExpandableLayout.expand();
                    addVoiceTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_keyboard_arrow_down_24dp, 0, 0, 0);
                    isAddVoiceCollapsed = false;
                } else {
                    addVoiceExpandableLayout.collapse();
                    addVoiceTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_keyboard_arrow_right_24dp, 0, 0, 0);
                    isAddVoiceCollapsed = true;
                }
            }
        });
    }

    public void setupReminderExpandable() {
        addReminderTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAddReminderCollapsed) {
                    reminderExpandableLayout.expand();
                    addReminderTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_keyboard_arrow_down_24dp, 0, 0, 0);
                    isAddReminderCollapsed = false;
                } else {
                    reminderExpandableLayout.collapse();
                    addReminderTv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_keyboard_arrow_right_24dp, 0, 0, 0);
                    isAddReminderCollapsed = true;
                }
            }
        });
    }

    public void setupButtonsClickListener() {
        setupVoiceButtons();
        setupReminderButtons();
        setupDueDateButton();
        setupDialogDismissButton();
        setupSubmitButton();
    }

    public void setupSubmitButton() {
        submitTodoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Fetch data from the EditTexts and save it to strings
                String todoNotes = todoNotesEt.getText().toString().trim();
                String todoDesc = todoBodyEt.getText().toString().trim();
                String todoDueDate = dueDateTv.getText().toString();
                String filePath = recorderAsset.getFileName();
                String reminderTime = reminderAsset.convertCalendarToMilliseconds();

                //Makes sure the to-do body field is completed, else show warning of invalidity
                if (!TextUtils.isEmpty(todoDesc)) {

                    // the "add new to-do" case
                    if (todoToBeUpdated != null) {
                        //Check if the to-do already had voice and later has been reset and submitted
                        //in this case, delete it from storage
                        if (hasVoiceInitially && hasOriginalVoiceChanged) {
                            recorderAsset.deleteFileFromStorage(previousFilePath);
                        }

                        // Update the to-do if it already had a due date or didn't have due date but still stayed with no one
                        // if the to-do didn't have due date at first and changed after to have one
                        // delete it and insert another one with same data to have a new id so it won't mess things up
                        if (isDated || TextUtils.isEmpty(todoDueDate)) {
                            updateTodo(todoToBeUpdated, todoNotes, todoDesc, todoDueDate, filePath, reminderTime);
                        } else {
                            Todo newTodo = new Todo(todoNotes, todoDesc, todoDueDate, todoToBeUpdated.isChecked(), filePath);
                            if (!TextUtils.isEmpty(reminderTime)) {
                                //set the reminder time for the to-do only if it's past the current time
                                if (Long.parseLong(reminderTime) > System.currentTimeMillis()) {
                                    newTodo.setReminderTime(reminderTime);
                                }
                            }
                            mSubmitTodoListener.onSubmitTodo(newTodo, false, true, todoToBeUpdated);
                        }
                    } else {

                        //Makes new To-do object from the data entered
                        //If date is not entered, set null for date
                        Todo todoAdded;
                        if (!TextUtils.isEmpty(todoDueDate)) {
                            todoAdded = new Todo(todoNotes, todoDesc, todoDueDate, false, filePath);
                        } else {
                            todoAdded = new Todo(todoNotes, todoDesc, null, false, filePath);
                        }

                        if (!TextUtils.isEmpty(reminderTime)) {
                            if (Long.parseLong(reminderTime) > System.currentTimeMillis()) {
                                todoAdded.setReminderTime(reminderTime);
                            }
                        }

                        //because this is a new entry, oldTodo is set to null
                        mSubmitTodoListener.onSubmitTodo(todoAdded, true, false, null);
                    }

                    isSubmitted = true;
                    dismiss();

                } else {
                    mandatoryFieldTv.setVisibility(View.VISIBLE);
                    todoBodyEt.setBackground(getResources().getDrawable(R.drawable.edit_text_invalid_style));
                }
            }
        });
    }

    private void setupDialogDismissButton() {
        dismissDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void setupDueDateButton() {
        dueDateTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker(dueDateTv);
            }
        });
    }

    private void setupReminderButtons() {
        setReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reminderAsset.setDate();
            }
        });
    }

    private void setupVoiceButtons() {
        playButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (hasPermissions()) {
                    if (isPlayed) {
                        playButton.setText("PLAY");
                        playButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow_blue_24dp, 0, 0, 0);
                        recorderAsset.releaseSound();
                        isPlayed = false;
                    } else {
                        playButton.setText("STOP");
                        playButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_stop_blue_24dp, 0, 0, 0);
                        recorderAsset.playVoiceNote(recorderAsset.getFileName(), playButton);
                        isPlayed = true;
                    }
                } else {
                    return;
                }
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                if (hasPermissions()) {
                    // release the voice note record if it's currently playing
                    recorderAsset.releaseSound();
                    recordDoneLayout.setVisibility(View.GONE);
                    recordLayout.setVisibility(View.VISIBLE);

                    if (todoToBeUpdated != null) {
                        if (hasVoiceInitially && !hasOriginalVoiceChanged) {
                            // just set the file path in recorderAsset to null
                            // but won't delete the file itself in case the change hasn't been submitted
                            recorderAsset.setFileName(null);
                            hasOriginalVoiceChanged = true;
                        } else {
                            recorderAsset.deleteFileFromStorage();
                        }
                    } else {
                        //if this is the add new to-do case, delete the file directly from storage
                        recorderAsset.deleteFileFromStorage();
                    }
                } else {
                    return;
                }
            }
        });

        recordIv.setOnTouchListener(new View.OnTouchListener() {

            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!hasPermissions()) {
                    return false;
                } else {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN: {
                            holdToRecordTv.setVisibility(View.GONE);
                            recordChronometer.setVisibility(View.VISIBLE);

                            recordIv.setImageResource(R.drawable.ic_mic_white24dp);
                            recordIv.setBackgroundResource(R.drawable.red_circle);
                            recorderAsset.animateAndVibrateButton(recordIv);
                            recorderAsset.startRecording(recordChronometer);

                            return true;
                        }

                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL: {
                            recordIv.clearAnimation();
                            recorderAsset.stopRecording(recordChronometer);
                            recordLayout.setVisibility(View.GONE);
                            recordChronometer.setVisibility(View.GONE);

                            String duration = recorderAsset.getDurationOfVoiceNote();
                            if (duration != null) {
                                durationTv.setText(duration);
                            } else {
                                durationTv.setText("00:00");
                            }

                            recordDoneLayout.setVisibility(View.VISIBLE);

                            // Return the resources ui to its original form
                            recordIv.setImageResource(R.drawable.ic_mic_24dp);
                            recordIv.setBackgroundResource(R.drawable.circle);
                            recordIv.setScaleX(1f);
                            recordIv.setScaleY(1f);
                            holdToRecordTv.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                    return false;
                }
            }
        });
    }

    public void showDatePicker(TextView dueDateTextView) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar newCalendar = Calendar.getInstance();
                newCalendar.set(Calendar.YEAR, year);
                newCalendar.set(Calendar.MONTH, month);
                newCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                int actualMonth = month + 1;

                dueDateTextView.setText(dayOfMonth + "/" + actualMonth + "/" + year);
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    public void setupEditTexts() {
        //This will get the notes field to hide the pencil icon when the user starts writing
        //and re-show it when he erases all the writing
        todoNotesEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    todoNotesEt.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                } else {
                    //Assign your image again to the view, otherwise it will always be gone even if the text is 0 again.
                    todoNotesEt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_border_color_gray_24dp, 0, 0, 0);
                }
            }
        });

        //This will remove the invalid red alert (that shows when the user submits the to-do without filling the to-do body) as
        //he starts filling the to-do body field
        todoBodyEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() > 0) {
                    mandatoryFieldTv.setVisibility(View.GONE);
                    todoBodyEt.setBackground(getResources().getDrawable(R.drawable.edit_text_style));
                }
            }
        });
    }

    public void updateTodo(Todo todoToBeUpdated, String todoTitle, String todoDesc, String todoDueDate, String filePath, String reminderTime) {
        todoToBeUpdated.setAdditionalNotes(todoTitle);
        todoToBeUpdated.setDescription(todoDesc);

        //Change due date if a new value has been passed, otherwise keep the old one
        if (!TextUtils.isEmpty(todoDueDate)) {
            todoToBeUpdated.setDate(todoDueDate);
        }

        todoToBeUpdated.setVoiceNotePath(filePath);

        if (!TextUtils.isEmpty(reminderTime)) {
            if (Long.parseLong(reminderTime) > System.currentTimeMillis()) {
                //using the to-do id will override if there's a reminder previously set for the same to-do
                todoToBeUpdated.setReminderTime(reminderTime);
            } else {
                //If the reminder set has passed the current time, set it to null
                todoToBeUpdated.setReminderTime(null);
            }
        } else if (!TextUtils.isEmpty(todoToBeUpdated.getReminderTime()) && TextUtils.isEmpty(reminderTime)) {
            //In case the to-do initially had reminder set, but later removed, we should cancel the reminder alarm from the system
            todoToBeUpdated.setReminderTime(null);
        }

        //there's no old to-do because we didn't make new one, we just updated it
        mSubmitTodoListener.onSubmitTodo(todoToBeUpdated, false, false, null);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        recorderAsset.releaseSound();

        //Handle the dismiss event according to which phase this is
        // whether it's the "add new to-do" or "edit the already existing to-do"
        if (!isSubmitted) {
            //The "add new to-do" case
            if (todoToBeUpdated == null) {
                //Delete the recorded voice note from storage, as there's no already submitted one before
                recorderAsset.deleteFileFromStorage();
            }
            //The "edit" case
            else {
                //This will delete from storage the recorded file in two cases
                //first one, the to-do didn't have previously recorded one, so any new recorded voice will be deleted
                //second one, it had a previously recorded one but has altered, so it will delete the new one
                //In case the to-do had previously recorded voice, and hasn't been changed, will do nothing
                if (!hasVoiceInitially || hasOriginalVoiceChanged) {
                    //this will delete the file whose path has been set to the recorder asset
                    //In case reset button clicked once, it's either null(if no other voice recorded), or the new recorded voice path
                    recorderAsset.deleteFileFromStorage();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public boolean hasPermissions() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    RECORD_AUDIO_REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }

    // Callback with the request from calling requestPermissions(...)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == RECORD_AUDIO_REQUEST_CODE) {
            if (grantResults.length == 3 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED) {


            } else {
                Toast.makeText(getContext(), R.string.must_give_permission_toast, Toast.LENGTH_SHORT).show();
            }
        }
    }

    interface SubmitTodoListener {
        void onSubmitTodo(Todo todo, boolean isNewEntry, boolean isDueDateChangedFromNull, Todo oldTodo);
    }
}
