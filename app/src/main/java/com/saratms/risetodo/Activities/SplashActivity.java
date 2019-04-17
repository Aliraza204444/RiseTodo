package com.saratms.risetodo.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.saratms.risetodo.R;
import com.saratms.risetodo.ViewPagerPackage.CustomPagerAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Sarah Al-Shamy on 25/02/2019.
 */

public class SplashActivity extends AppCompatActivity {
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.tabDots)
    TabLayout tabLayoutDots;
    @BindView(R.id.splash_next_tv)
    TextView splashNextTv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        // This will check if this is the user's first time using the app
        // If it is not, take him to the app directly without showing this opening theme
        checkForSharedPref();

        viewPager.setAdapter(new CustomPagerAdapter(this));
        tabLayoutDots.setupWithViewPager(viewPager);

        splashNextTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
            }
        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        splashNextTv.setText("NEXT");
                        splashNextTv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                viewPager.setCurrentItem(1);
                            }
                        });
                        break;

                    case 1:
                        splashNextTv.setText("NEXT");
                        splashNextTv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                viewPager.setCurrentItem(2);
                            }
                        });
                        break;

                    case 2:
                        splashNextTv.setText("DONE");
                        splashNextTv.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //This will prevent the opening theme from showing up every time the user opens the app
                                saveToSharedPref();

                                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void saveToSharedPref() {
        SharedPreferences pref = this.getSharedPreferences("shared_pref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("first_enter", "true");
        editor.commit();
    }

    public void checkForSharedPref() {
        SharedPreferences pref = this.getSharedPreferences("shared_pref", MODE_PRIVATE);
        String firstEnter = pref.getString("first_enter", "false");
        if (firstEnter.equals("true")){
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
