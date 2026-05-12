package com.example.android_app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.android_app.fragment.ThemGiaoDichFragment;
import com.example.android_app.fragment.TrangChuFragment;
import com.example.android_app.fragment.HoSoFragment;
import com.example.android_app.fragment.ThongKeFragment;
import com.example.android_app.fragment.ViTienFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private  BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);
        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);

        // Load fragment mặc định khi mở app
        if (savedInstanceState == null) {
            loadFragment(new TrangChuFragment());
            bottomNavigation.setSelectedItemId(R.id.nav_home);
        }

        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                loadFragment(new TrangChuFragment());
                return true;
            } else if (id == R.id.nav_stats) {
                loadFragment(new ThongKeFragment());
                return true;
            } else if (id == R.id.nav_add) {
                loadFragment(new ThemGiaoDichFragment());
                return true;
            } else if (id == R.id.nav_wallet) {
                loadFragment(new ViTienFragment());
                return true;
            } else if (id == R.id.nav_profile) {
                loadFragment(new HoSoFragment());
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
