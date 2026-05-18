package com.example.android_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_app.database.NguoiDungDAO;
import com.example.android_app.model.NguoiDung;
import com.example.android_app.utils.SecurityUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class CaiDatActivity extends AppCompatActivity {

    private SharedPreferences prefs;
    private NguoiDungDAO userDAO;

    private ActivityResultLauncher<String> imagePickerLauncher;
    private ImageView ivPreviewAvatar;
    private String currentSelectedAvatarPath = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cai_dat);

        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userDAO = new NguoiDungDAO(this);
        userDAO.open();

        // Khởi tạo ActivityResultLauncher để chọn ảnh
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null && ivPreviewAvatar != null) {
                try {
                    // Copy ảnh vào thư mục bộ nhớ trong của ứng dụng
                    InputStream is = getContentResolver().openInputStream(uri);
                    File avatarDir = new File(getFilesDir(), "avatars");
                    if (!avatarDir.exists()) avatarDir.mkdir();
                    
                    File avatarFile = new File(avatarDir, "avatar_" + System.currentTimeMillis() + ".jpg");
                    FileOutputStream fos = new FileOutputStream(avatarFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                    fos.flush();
                    fos.close();
                    if (is != null) is.close();

                    currentSelectedAvatarPath = avatarFile.getAbsolutePath();
                    ivPreviewAvatar.setImageURI(Uri.fromFile(avatarFile));
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Lỗi khi tải ảnh từ thiết bị", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnEditProfile).setOnClickListener(v -> showEditProfileDialog());
        findViewById(R.id.btnChangePassword).setOnClickListener(v -> showChangePasswordDialog());
        findViewById(R.id.btnLogout).setOnClickListener(v -> logout());
    }

    private void showEditProfileDialog() {
        long userId = prefs.getLong("user_id", -1);
        if (userId == -1) return;
        NguoiDung user = userDAO.getUserById(userId);
        if (user == null) return;

        currentSelectedAvatarPath = user.getAvatar(); // Lưu lại đường dẫn cũ

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sua_thong_tin, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etName = dialogView.findViewById(R.id.etEditName);
        EditText etEmail = dialogView.findViewById(R.id.etEditEmail);
        ivPreviewAvatar = dialogView.findViewById(R.id.ivPreviewAvatar);
        Button btnChooseImage = dialogView.findViewById(R.id.btnChooseImage);

        etName.setText(user.getUsername());
        if (user.getEmail() != null) {
            etEmail.setText(user.getEmail());
        }

        // Hiển thị avatar hiện tại
        if (currentSelectedAvatarPath != null && !currentSelectedAvatarPath.isEmpty()) {
            if (currentSelectedAvatarPath.startsWith("/")) {
                // Đây là đường dẫn file tuyệt đối
                ivPreviewAvatar.setImageBitmap(BitmapFactory.decodeFile(currentSelectedAvatarPath));
            } else {
                // Đây là file drawable mặc định
                int resId = getResources().getIdentifier(currentSelectedAvatarPath, "drawable", getPackageName());
                if (resId != 0) ivPreviewAvatar.setImageResource(resId);
            }
        } else {
            ivPreviewAvatar.setImageResource(R.drawable.ic_avatar_banana);
        }

        // Xử lý nút chọn ảnh
        btnChooseImage.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        Button btnCancel = dialogView.findViewById(R.id.btnCancelEdit);
        Button btnSave = dialogView.findViewById(R.id.btnSaveEdit);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(this, "Tên không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            user.setUsername(newName);
            user.setEmail(newEmail);
            user.setAvatar(currentSelectedAvatarPath); // Cập nhật đường dẫn file (hoặc tên drawable)

            int result = userDAO.updateUser(user);
            if (result > 0) {
                // Update SharedPreferences
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("username", newName);
                editor.apply();

                Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(this, "Có lỗi xảy ra, thử lại sau", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_doi_mat_khau, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etCurrentPassword = dialogView.findViewById(R.id.etCurrentPassword);
        EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirmNewPassword = dialogView.findViewById(R.id.etConfirmNewPassword);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelChange);
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmChange);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnConfirm.setOnClickListener(v -> {
            String currentPwd = etCurrentPassword.getText().toString().trim();
            String newPwd = etNewPassword.getText().toString().trim();
            String confirmNewPwd = etConfirmNewPassword.getText().toString().trim();

            if (currentPwd.isEmpty() || newPwd.isEmpty() || confirmNewPwd.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPwd.length() < 6) {
                Toast.makeText(this, "Mật khẩu mới phải dài ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPwd.equals(confirmNewPwd)) {
                Toast.makeText(this, "Xác nhận mật khẩu mới không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            long userId = prefs.getLong("user_id", -1);
            if (userId != -1) {
                NguoiDung user = userDAO.getUserById(userId);
                if (user != null) {
                    String hashedCurrentInput = SecurityUtils.hashPasswordSHA256(currentPwd);
                    if (!hashedCurrentInput.equals(user.getPassword())) {
                        Toast.makeText(this, "Mật khẩu hiện tại không đúng", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    user.setPassword(SecurityUtils.hashPasswordSHA256(newPwd));
                    int result = userDAO.updateUser(user);
                    if (result > 0) {
                        Toast.makeText(this, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "Có lỗi xảy ra, thử lại sau", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        dialog.show();
    }

    private void logout() {
        prefs.edit().clear().apply();
        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, DangNhapActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDAO != null) userDAO.close();
    }
}
