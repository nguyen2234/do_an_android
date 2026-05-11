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

    private TextView tvProfileName, tvProfileEmail;
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
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
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
            showChangePasswordDialog();
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
                if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                    tvProfileEmail.setText(user.getEmail());
                } else {
                    tvProfileEmail.setText("Chưa cập nhật Email");
                }
            }
        }
    }

    private void showChangePasswordDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);
        
        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        android.widget.EditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        android.widget.EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        android.widget.EditText etConfirmNewPassword = dialogView.findViewById(R.id.etConfirmNewPassword);
        android.widget.Button btnCancel = dialogView.findViewById(R.id.btnCancelChange);
        android.widget.Button btnConfirm = dialogView.findViewById(R.id.btnConfirmChange);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String currentPwd = etCurrentPassword.getText().toString().trim();
            String newPwd = etNewPassword.getText().toString().trim();
            String confirmNewPwd = etConfirmNewPassword.getText().toString().trim();

            if (currentPwd.isEmpty() || newPwd.isEmpty() || confirmNewPwd.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPwd.length() < 6) {
                Toast.makeText(getContext(), "Mật khẩu mới phải dài ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPwd.equals(confirmNewPwd)) {
                Toast.makeText(getContext(), "Xác nhận mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            long userId = prefs.getLong("user_id", -1);
            if (userId != -1) {
                User user = userDAO.getUserById(userId);
                if (user != null) {
                    // Băm mật khẩu cũ để kiểm tra
                    String hashedCurrentInput = com.example.android_app.utils.SecurityUtils.hashPasswordSHA256(currentPwd);
                    if (!hashedCurrentInput.equals(user.getPassword())) {
                        Toast.makeText(getContext(), "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Cập nhật mật khẩu mới (đã mã hóa)
                    user.setPassword(com.example.android_app.utils.SecurityUtils.hashPasswordSHA256(newPwd));
                    int result = userDAO.updateUser(user);
                    if (result > 0) {
                        Toast.makeText(getContext(), "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(getContext(), "Có lỗi xảy ra, thử lại sau", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userDAO != null) userDAO.close();
    }
}
