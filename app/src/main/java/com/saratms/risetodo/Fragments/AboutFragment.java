package com.saratms.risetodo.Fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.oss.licenses.OssLicensesMenuActivity;
import com.saratms.risetodo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Sarah Al-Shamy on 22/11/2018.
 */

public class AboutFragment extends Fragment {

    @BindView(R.id.licenses_text_view)
    TextView licensesTextView;
    @BindView(R.id.rate_app_text_view)
    TextView rateAppTextView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.about_fragment_layout, container, false);
        ButterKnife.bind(this, view);

        licensesTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), OssLicensesMenuActivity.class));
            }
        });

        rateAppTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=" + getContext().getPackageName());
                Intent goToStore = new Intent(Intent.ACTION_VIEW, uri);

                //To add play store to backstack, so when we press back button, it takes us back to the application
                goToStore.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                try {
                    startActivity(goToStore);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + getContext().getPackageName())));
                }
            }
        });
        return view;
    }
}
