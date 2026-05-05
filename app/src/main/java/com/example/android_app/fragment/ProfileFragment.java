package com.example.android_app.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.example.android_app.LoginActivity;
import com.example.android_app.R;
import com.example.android_app.database.UserDAO;
import com.example.android_app.model.User;

public class ProfileFragment extends Fragment {

    private TextView tvProfileName;
    private Switch switchTheme;
    private LinearLayout btnLogout, btnChangePassword;
    private UserDAO userDAO;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userDAO = new UserDAO(getContext());
        userDAO.open();
        prefs = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        tvProfileName = view.findViewById(R.id.tvProfileName);
        switchTheme = view.findViewById(R.id.switchTheme);
        btnLogout = view.findViewById(R.id.btnLogout);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);

        // Load thông tin User
        loadUserProfile();

        // Xử lý đổi giao diện Sáng/Tối
        int currentTheme = prefs.getInt("theme_mode", 0);
        switchTheme.setChecked(currentTheme == 2);
        
        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            int newTheme = isChecked ? 2 : 1;
            prefs.edit().putInt("theme_mode", newTheme).apply();
            
            long userId = prefs.getLong("user_id", -1);
            if (userId != -1) {
                User user = userDAO.getUserById(userId);
                if (user != null) {
                    user.setThemeMode(newTheme);
                    userDAO.updateUser(user);
                }
            }

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Xử lý Đổi mật khẩu
        btnChangePassword.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Chức năng Đổi mật khẩu đang phát triển", Toast.LENGTH_SHORT).show();
        });

        // Xử lý Đăng xuất
        btnLogout.setOnClickListener(v -> {
            prefs.edit().clear().apply();
            Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(getActivity(), LoginActivity.class));
            if (getActivity() != null) getActivity().finish();
        });
    }

    private void loadUserProfile() {
        long userId = prefs.getLong("user_id", -1);
        if (userId != -1) {
            User user = userDAO.getUserById(userId);
            if (user != null) {
                tvProfileName.setText(user.getUsername());
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userDAO != null) userDAO.close();
    }
}
