package com.example.android_app;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.android_app.fragment.GiaoDichDuKienFragment;
import com.example.android_app.fragment.NganSachFragment;
import com.example.android_app.fragment.ThemGiaoDichFragment;
import com.example.android_app.fragment.TrangChuFragment;
import com.example.android_app.fragment.HoSoFragment;
import com.example.android_app.fragment.ThongKeFragment;
import com.example.android_app.utils.NotificationHelper;
import com.example.android_app.utils.ReminderWorker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigation;

    // Launcher xin quyền POST_NOTIFICATIONS (Android 13+)
    private final ActivityResultLauncher<String> notifPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                // Không cần làm gì thêm, notification sẽ hoạt động nếu được cấp
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Khởi tạo Notification Channels
        NotificationHelper.createNotificationChannels(this);

        // 2. Xin quyền POST_NOTIFICATIONS trên Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        // 3. Đăng ký Worker nhắc nhở chạy mỗi 24 giờ
        PeriodicWorkRequest reminderWork = new PeriodicWorkRequest.Builder(
                ReminderWorker.class, 24, TimeUnit.HOURS)
                .build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "DailyReminderWork",
                ExistingPeriodicWorkPolicy.KEEP,
                reminderWork
        );

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
            } else if (id == R.id.nav_budget) {
                loadFragment(new NganSachFragment());
                return true;
            } else if (id == R.id.nav_add) {
                loadFragment(new ThemGiaoDichFragment());
                return true;
            } else if (id == R.id.nav_stats) {
                loadFragment(new ThongKeFragment());
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

    /**
     * Mở màn hình Khoản đến hạn (có thể gọi từ notification hoặc các nơi khác).
     */
    public void openDuKienFragment() {
        loadFragment(new GiaoDichDuKienFragment());
    }

    /**
     * Chọn mục trên thanh điều hướng dưới.
     */
    public void setSelectedItemId(int itemId) {
        if (bottomNavigation != null) {
            bottomNavigation.setSelectedItemId(itemId);
        }
    }
}
