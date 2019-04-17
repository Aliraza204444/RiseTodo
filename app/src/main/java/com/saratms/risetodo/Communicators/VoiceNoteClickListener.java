package com.saratms.risetodo.Communicators;

import android.widget.Button;

/**
 * Created by Sarah Al-Shamy on 16/03/2019.
 */

public interface VoiceNoteClickListener {
    void onVoiceNoteClick(String filePath, Button playButton, boolean isAlreadyPlaying);
}
