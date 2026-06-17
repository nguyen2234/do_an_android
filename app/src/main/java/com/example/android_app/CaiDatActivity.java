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

        
        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null && ivPreviewAvatar != null) {
                try {
                    
                    InputStream is = getContentResolver().openInputStream(uri);
                    File tempFile = new File(getCacheDir(), "temp_avatar_" + System.currentTimeMillis() + ".jpg");
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = is.read(buffer)) > 0) {
                        fos.write(buffer, 0, length);
                    }
                    fos.flush();
                    fos.close();
                    if (is != null) is.close();

                    currentSelectedAvatarPath = tempFile.getAbsolutePath();
                    ivPreviewAvatar.setImageURI(Uri.fromFile(tempFile));
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Lỗi khi tải ảnh từ thiết bị", Toast.LENGTH_SHORT).show();
                }
            }
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnEditProfile).setOnClickListener(v -> showEditProfileDialog());
        findViewById(R.id.btnChangePassword).setOnClickListener(v -> showChangePasswordDialog());

        
        String action = getIntent().getStringExtra("action");
        if ("change_password".equals(action)) {
            showChangePasswordDialog();
        }
    }

    private void cleanupTempAvatar() {
        if (currentSelectedAvatarPath != null && currentSelectedAvatarPath.contains("temp_avatar_")) {
            try {
                File tempFile = new File(currentSelectedAvatarPath);
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            long userId = prefs.getLong("user_id", -1);
            if (userId != -1) {
                NguoiDung user = userDAO.getUserById(userId);
                if (user != null) {
                    currentSelectedAvatarPath = user.getAvatar();
                }
            }
        }
    }

    private void showEditProfileDialog() {
        long userId = prefs.getLong("user_id", -1);
        if (userId == -1) return;
        NguoiDung user = userDAO.getUserById(userId);
        if (user == null) return;

        currentSelectedAvatarPath = user.getAvatar(); 

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_sua_thong_tin, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        
        dialog.setOnDismissListener(d -> cleanupTempAvatar());

        EditText etName = dialogView.findViewById(R.id.etEditName);
        ivPreviewAvatar = dialogView.findViewById(R.id.ivPreviewAvatar);
        Button btnChooseImage = dialogView.findViewById(R.id.btnChooseImage);

        etName.setText(user.getFullname() != null ? user.getFullname() : "");

        
        if (currentSelectedAvatarPath != null && !currentSelectedAvatarPath.isEmpty()) {
            if (currentSelectedAvatarPath.startsWith("/")) {
                ivPreviewAvatar.setImageBitmap(BitmapFactory.decodeFile(currentSelectedAvatarPath));
            } else {
                int resId = getResources().getIdentifier(currentSelectedAvatarPath, "drawable", getPackageName());
                if (resId != 0) ivPreviewAvatar.setImageResource(resId);
            }
        } else {
            ivPreviewAvatar.setImageResource(R.drawable.ic_avatar_banana);
        }

        
        btnChooseImage.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        Button btnCancel = dialogView.findViewById(R.id.btnCancelEdit);
        Button btnSave = dialogView.findViewById(R.id.btnSaveEdit);

        btnCancel.setOnClickListener(v -> {
            cleanupTempAvatar();
            dialog.dismiss();
        });

        btnSave.setOnClickListener(v -> {
            String newFullName = etName.getText().toString().trim();

            if (newFullName.isEmpty()) {
                Toast.makeText(this, "Họ và tên không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            
            if (currentSelectedAvatarPath != null && currentSelectedAvatarPath.contains("temp_avatar_")) {
                try {
                    File tempFile = new File(currentSelectedAvatarPath);
                    if (tempFile.exists()) {
                        File avatarDir = new File(getFilesDir(), "avatars");
                        if (!avatarDir.exists()) avatarDir.mkdir();
                        File finalFile = new File(avatarDir, "avatar_" + System.currentTimeMillis() + ".jpg");
                        
                        InputStream is = new java.io.FileInputStream(tempFile);
                        FileOutputStream fos = new FileOutputStream(finalFile);
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = is.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                        fos.flush();
                        fos.close();
                        is.close();
                        
                        tempFile.delete(); 
                        currentSelectedAvatarPath = finalFile.getAbsolutePath();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            user.setFullname(newFullName);
            user.setAvatar(currentSelectedAvatarPath);

            int result = userDAO.updateUser(user);
            if (result > 0) {
                
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("fullname", newFullName);
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
