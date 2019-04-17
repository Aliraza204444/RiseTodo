package com.saratms.risetodo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.saratms.risetodo.Utilities.DateUtils;
import java.util.Calendar;

/**
 * Created by Sarah Al-Shamy on 24/03/2019.
 */

public class ReminderAsset {

    Context mContext;
    Calendar mCalendar;
    boolean isDateSet;
    boolean isTimeSet;

    TextView mReminderText;
    Button mSetReminderButton;
    Button mRemoveReminderButton;

    public ReminderAsset(Context context, TextView reminderText, Button setReminderButton, Button removeReminderButton) {
        mContext = context;
        mCalendar = Calendar.getInstance();
        mSetReminderButton = setReminderButton;
        mRemoveReminderButton = removeReminderButton;
        mReminderText = reminderText;
        isDateSet = false;
        isTimeSet = false;
    }


    public void setDate() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog = new DatePickerDialog(mContext, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, month);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                isDateSet = true;

                setTime();
            }
        }, year, month, day);
        datePickerDialog.show();
    }

    public void setTime() {

        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(mContext, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mCalendar.set(Calendar.MINUTE, minutes);
                mCalendar.set(Calendar.SECOND, 0);
                isTimeSet = true;

                if (mCalendar.getTimeInMillis() < System.currentTimeMillis()) {
                    Toast.makeText(mContext, R.string.reminder_expired, Toast.LENGTH_SHORT).show();
                    isTimeSet = false;
                    isDateSet = false;
                } else {
                    updateUi();
                }
            }
        }, hour, minute, false);
        timePickerDialog.show();
    }


    public void updateUi() {
        if (mCalendar != null) {
            String formattedTime = DateUtils.formatReminderTime(mCalendar);
            if (mCalendar.getTimeInMillis() > System.currentTimeMillis()) {
                mReminderText.setText(formattedTime);
                mReminderText.setTextColor(Color.GRAY);
                mReminderText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm_24dp, 0,0,0);
            } else {
                mReminderText.setText(mContext.getResources().getString(R.string.reminder_expired));
                mReminderText.setTextColor(Color.parseColor("#FFD50000"));
                mReminderText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm_red_24dp, 0,0,0);

            }
            mSetReminderButton.setText("EDIT");
            mRemoveReminderButton.setVisibility(View.VISIBLE);
        } else {
            mReminderText.setText(mContext.getResources().getString(R.string.no_reminder_specified_text));
            mSetReminderButton.setText(mContext.getResources().getString(R.string.set_reminder_button_text));
            mRemoveReminderButton.setVisibility(View.GONE);
        }

        mRemoveReminderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mReminderText.setText(mContext.getResources().getString(R.string.no_reminder_specified_text));
                mSetReminderButton.setText(mContext.getResources().getString(R.string.set_reminder_button_text));
                mReminderText.setTextColor(Color.GRAY);
                mReminderText.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_alarm_24dp, 0,0,0);
                mRemoveReminderButton.setVisibility(View.GONE);
                isDateSet = false;
                isTimeSet = false;
            }
        });
    }

    public void setCalendar(Calendar calendar) {
        mCalendar = calendar;
        if (calendar != null) {
            isDateSet = true;
            isTimeSet = true;
        }
    }

    //Make sure both date and time are set to return the milliseconds needed to set the reminder, else it returns null
    public String convertCalendarToMilliseconds() {
        if (mCalendar != null && isDateSet && isTimeSet) {
            long startTime = mCalendar.getTimeInMillis();
            return (String.valueOf(startTime));
        } else {
            return null;
        }
    }
}
