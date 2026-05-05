package com.example.android_app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.android_app.fragment.AddTransactionFragment;
import com.example.android_app.fragment.HomeFragment;
import com.example.android_app.fragment.ProfileFragment;
import com.example.android_app.fragment.StatisticsFragment;
import com.example.android_app.fragment.WalletFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        // Load fragment mặc định khi mở app
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new HomeFragment());
                return true;
            } else if (id == R.id.nav_stats) {
                loadFragment(new StatisticsFragment());
                return true;
            } else if (id == R.id.nav_add) {
                loadFragment(new AddTransactionFragment());
                return true;
            } else if (id == R.id.nav_wallet) {
                loadFragment(new WalletFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                loadFragment(new ProfileFragment());
                return true;
            }
            return false;
        });

        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> bottomNavigation.setSelectedItemId(R.id.nav_add));
        }
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
        }
    }
}
