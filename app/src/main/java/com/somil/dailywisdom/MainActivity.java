package com.somil.dailywisdom;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.somil.dailywisdom.fragments.HomeFragment;
import com.somil.dailywisdom.fragments.MyQuotesFragment;
import com.somil.dailywisdom.fragments.ProfileFragment;
import com.somil.dailywisdom.fragments.QuoteListFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        SplashScreen.installSplashScreen(this);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            replaceFragment(new HomeFragment(), "home");
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            String tag = null;
            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
                tag = "home";
            } else if (item.getItemId() == R.id.nav_add) {
                startActivity(new Intent(MainActivity.this, AddQuoteActivity.class));
                return false;
            } else if (item.getItemId() == R.id.nav_list) {
                selectedFragment = new QuoteListFragment();
                tag = "my_quotes";
            } else if (item.getItemId() == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
                tag = "profile";
            }

            if (selectedFragment != null) {
                replaceFragment(selectedFragment, tag);
                return true;
            }
            return false;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

                if (currentFragment instanceof HomeFragment) {
                    showExitConfirmationDialog();
                } else {
                    bottomNavigationView.setSelectedItemId(R.id.nav_home);
                    replaceFragment(new HomeFragment(), "home");
                }
            }
        });
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, tag);
        fragmentTransaction.commit();
    }

    private void showExitConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Exit App")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Yes", (dialog, which) -> finish())
                .show();
    }
}