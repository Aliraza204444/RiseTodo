package com.saratms.risetodo.Database;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by Sarah Al-Shamy on 20/11/2018.
 */

@Entity(tableName = "todo_table")
public class Todo {

    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "todo_notes")
    private String additionalNotes;

    @ColumnInfo(name = "todo_desc")
    private String description;

    @ColumnInfo(name = "todo_checked")
    private boolean checked;

    @ColumnInfo(name = "todo_duedate")
    private String date;

    @ColumnInfo(name = "voice_note_path")
    private String voiceNotePath;

    @ColumnInfo(name = "reminder_time")
    private String reminderTime;

    public Todo(String additionalNotes, String description, String date, boolean checked, String voiceNotePath) {
        this.additionalNotes = additionalNotes;
        this.description = description;
        this.checked = checked;
        this.date = date;
        this.voiceNotePath = voiceNotePath;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;

    }

    public boolean isChecked() {
        return this.checked;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getAdditionalNotes() {
        return this.additionalNotes;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public String getVoiceNotePath() {
        return voiceNotePath;
    }

    public void setVoiceNotePath(String voiceNotePath) {
        this.voiceNotePath = voiceNotePath;
    }

    public String getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.reminderTime = reminderTime;
    }

}
