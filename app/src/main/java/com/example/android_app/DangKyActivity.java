package com.example.android_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_app.database.NguoiDungDAO;
import com.example.android_app.model.NguoiDung;
import com.example.android_app.utils.SecurityUtils;

public class DangKyActivity extends AppCompatActivity {

    private EditText etUsername, etEmail, etPassword, etConfirmPassword, etFullname;
    private Button btnRegister;
    private TextView tvGoToLogin;
    private NguoiDungDAO userDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_ky);

        userDAO = new NguoiDungDAO(this);
        userDAO.open();

        etFullname = findViewById(R.id.etRegisterFullname);
        etUsername = findViewById(R.id.etRegisterUsername);
        etEmail = findViewById(R.id.etRegisterEmail);
        etPassword = findViewById(R.id.etRegisterPassword);
        etConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvGoToLogin = findViewById(R.id.tvGoToLogin);

        btnRegister.setOnClickListener(v -> handleRegister());

        tvGoToLogin.setOnClickListener(v -> {
            finish(); // Trở về trang đăng nhập
        });
    }

    private void handleRegister() {
        String fullname = etFullname.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (fullname.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập Họ và tên (bắt buộc)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        if (username.length() < 4 || username.contains(" ")) {
            Toast.makeText(this, "Tên đăng nhập tối thiểu 4 ký tự và không chứa dấu cách", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải dài ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userDAO.checkUsernameExist(username)) {
            Toast.makeText(this, "Tên đăng nhập đã tồn tại, vui lòng chọn tên khác", Toast.LENGTH_SHORT).show();
            return;
        }

        String hashedPassword = SecurityUtils.hashPasswordSHA256(password);
        NguoiDung newUser = new NguoiDung(0, username, hashedPassword, email, null, 0, fullname);
        long result = userDAO.registerUser(newUser);

        if (result > 0) {
            /*
             * SAU KHI ĐĂNG KÝ THÀNH CÔNG:
             * 1. Lưu session vào SharedPreferences (user_id, username, fullname)
             *    để các DAO có thể lấy getCurrentUserId() ngay lập tức
             * 2. Điều hướng sang Onboarding (SetupCategoryActivity)
             *    → Người dùng mới LUÔN đi qua Onboarding
             */
            // Lấy user vừa tạo để lấy ID
            NguoiDung createdUser = userDAO.checkLogin(username, hashedPassword);
            if (createdUser != null) {
                android.content.SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                prefs.edit()
                        .putLong("user_id", createdUser.getId())
                        .putString("username", createdUser.getUsername())
                        .putString("fullname", createdUser.getFullname())
                        .putBoolean("isOnboardingDone", false) // Đảm bảo onboarding chưa done
                        .apply();
            }

            Toast.makeText(this, "Đăng ký thành công! Hãy thiết lập ban đầu.", Toast.LENGTH_SHORT).show();
            // Bắt đầu Onboarding
            Intent intent = new Intent(this, SetupCategoryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Có lỗi hệ thống xảy ra khi đăng ký", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDAO != null) userDAO.close();
    }
}
