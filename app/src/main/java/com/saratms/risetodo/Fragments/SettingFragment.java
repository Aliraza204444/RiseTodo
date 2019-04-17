package com.saratms.risetodo.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.saratms.risetodo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Sarah Al-Shamy on 22/11/2018.
 */

public class SettingFragment extends Fragment {

    @BindView(R.id.sound_switch)
    Switch soundSwitch;

    boolean isSoundChecked;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_fragment_layout, container, false);
        ButterKnife.bind(this, view);

        if (isSoundChecked) {
            soundSwitch.setChecked(true);
        } else {
            soundSwitch.setChecked(false);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        soundSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(soundSwitch.isChecked()){
                    setSoundStateSharedPref("yes");
                }else{
                    setSoundStateSharedPref("no");
                }
            }
        });
    }

    public void setSoundStateSharedPref(String soundState) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("shared_pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("sound", soundState);
        editor.commit();
    }

    public void getSoundStateSharedPref(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("shared_pref", Context.MODE_PRIVATE);
        String soundSharedPref = sharedPreferences.getString("sound", "yes");
        if (soundSharedPref.equals("yes")) {
            isSoundChecked = true;
        } else {
            isSoundChecked = false;
        }
    }
}
