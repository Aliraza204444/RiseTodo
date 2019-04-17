package com.saratms.risetodo.Activities;

import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.saratms.risetodo.Fragments.AboutFragment;
import com.saratms.risetodo.Fragments.SettingFragment;
import com.saratms.risetodo.Fragments.TodoListFragment;
import com.saratms.risetodo.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements TodoListFragment.RequestScrollListener {

    @BindView(R.id.add_todo_floating_button)
    FloatingActionButton addTodoFloatingButton;
    @BindView(R.id.toolbar)
    android.support.v7.widget.Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.toolbar_layout)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.app_bar)
    AppBarLayout appBarLayout;
    @BindView(R.id.expandedImage)
    ImageView expandedImage;
    @BindView(R.id.scroll)
    NestedScrollView nestedScrollView;

    FragmentManager manager;
    TodoListFragment todoListFragment;
    SettingFragment settingFragment;
    AboutFragment aboutFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //To prevent layout views from moving when the add dialog come up
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        //Instantiate fragments
        todoListFragment = new TodoListFragment();
        aboutFragment = new AboutFragment();
        settingFragment = new SettingFragment();
        manager = getSupportFragmentManager();

        setupNavigationDrawer();
        setupCollapsingToolbar();

        manager.beginTransaction().add(R.id.main_screen_frame, todoListFragment).commit();

        addTodoFloatingButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                todoListFragment.showBottomSheet(null);
            }
        });
    }

    public void setupCollapsingToolbar() {
        appBarLayout.setExpanded(true, false);
        expandedImage.setVisibility(View.VISIBLE);
        collapsingToolbarLayout.setTitleEnabled(true);
        collapsingToolbarLayout.setTitle("My To-Do List");
    }

    public void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setCheckedItem(R.id.nav_home);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // Handle navigation view item clicks here.
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    appBarLayout.setExpanded(true, false);
                    expandedImage.setVisibility(View.VISIBLE);
                    collapsingToolbarLayout.setTitleEnabled(true);
                    collapsingToolbarLayout.setTitle("My To-Do List");
                    addTodoFloatingButton.setVisibility(View.VISIBLE);
                    manager.beginTransaction().replace(R.id.main_screen_frame, todoListFragment).commit();


                } else if (id == R.id.nav_setting) {
                    appBarLayout.setExpanded(false, false);
                    expandedImage.setVisibility(View.GONE);
                    collapsingToolbarLayout.setTitleEnabled(false);
                    toolbar.setTitle("Setting");
                    addTodoFloatingButton.setVisibility(View.GONE);

                    // Get SharedPref for sound state :on:off before transacting to the fragment
                    // Otherwise, it will be set after the fragment is visible to screen
                    settingFragment.getSoundStateSharedPref(MainActivity.this);
                    manager.beginTransaction().replace(R.id.main_screen_frame, settingFragment).commit();


                } else if (id == R.id.nav_about) {
                    appBarLayout.setExpanded(false, false);
                    expandedImage.setVisibility(View.GONE);
                    addTodoFloatingButton.setVisibility(View.GONE);
                    collapsingToolbarLayout.setTitleEnabled(false);
                    toolbar.setTitle("About");
                    manager.beginTransaction().replace(R.id.main_screen_frame, aboutFragment).commit();

                } else if (id == R.id.nav_exit) {
                    finishAffinity();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onNeedScroll(float yPosition) {
        //This method listens to when new item is added in order to scroll to position of the new item
        //This callback is triggered from the TodoListFragment
        nestedScrollView.post(new Runnable() {
            @Override
            public void run() {
                nestedScrollView.fling(0);
                appBarLayout.setExpanded(false, false);
                // When yPosition is equal to -1, this represents that the item added has no due date
                // So, it's added to the end bottom of the list and therefore the ScrollView is scrolled to the bottom
                if (yPosition == -1) {
                    nestedScrollView.fullScroll(NestedScrollView.FOCUS_DOWN);
                } else {
                    // If it has due date, it will scroll to its position according to the date after sorting
                    nestedScrollView.smoothScrollTo(0, (int) yPosition);
                }
            }
        });
    }
}