package com.example.android_app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android_app.database.NguoiDungDAO;
import com.example.android_app.model.NguoiDung;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ChinhSuaHoSoActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 101;

    private ImageView ivEditAvatar;
    private EditText etEditUsername, etEditPhone, etEditEmail;
    private Button btnSaveProfile;
    private ImageView btnBack;
    private TextView tvChangeAvatarLabel;

    private NguoiDungDAO userDAO;
    private SharedPreferences prefs;
    private long userId;
    private NguoiDung currentUser;

    private Uri selectedImageUri = null;
    private String savedAvatarPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chinh_sua_ho_so);

        // Initialize DAO
        userDAO = new NguoiDungDAO(this);
        userDAO.open();

        // SharedPreferences
        prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        userId = prefs.getLong("user_id", -1);

        if (userId == -1) {
            Toast.makeText(this, "Không tìm thấy phiên làm việc người dùng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Map Views
        ivEditAvatar = findViewById(R.id.ivEditAvatar);
        etEditUsername = findViewById(R.id.etEditUsername);
        etEditPhone = findViewById(R.id.etEditPhone);
        etEditEmail = findViewById(R.id.etEditEmail);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        btnBack = findViewById(R.id.btnBack);
        tvChangeAvatarLabel = findViewById(R.id.tvChangeAvatarLabel);

        // Set Click Listeners
        btnBack.setOnClickListener(v -> finish());

        findViewById(R.id.btnChooseAvatar).setOnClickListener(v -> openGallery());
        tvChangeAvatarLabel.setOnClickListener(v -> openGallery());

        btnSaveProfile.setOnClickListener(v -> saveProfileChanges());

        // Load profile data
        loadProfileData();
    }

    private void loadProfileData() {
        currentUser = userDAO.getUserById(userId);
        if (currentUser != null) {
            etEditUsername.setText(currentUser.getUsername());
            etEditEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "");

            // Phone number from SharedPreferences since db does not contain phone number
            String phone = prefs.getString("phone_" + userId, "");
            etEditPhone.setText(phone);

            // Load Avatar
            savedAvatarPath = currentUser.getAvatar();
            displayAvatar(savedAvatarPath);
        }
    }

    private void displayAvatar(String path) {
        if (path != null && !path.isEmpty()) {
            File imgFile = new File(path);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ivEditAvatar.setImageBitmap(myBitmap);
                return;
            }
        }
        // Default avatar
        ivEditAvatar.setImageResource(R.drawable.ic_avatar_banana);
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                // Instantly display selected image in crop UI
                InputStream is = getContentResolver().openInputStream(selectedImageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                ivEditAvatar.setImageBitmap(bitmap);
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Không thể đọc ảnh", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProfileChanges() {
        String username = etEditUsername.getText().toString().trim();
        String phone = etEditPhone.getText().toString().trim();
        String email = etEditEmail.getText().toString().trim();

        if (username.isEmpty()) {
            Toast.makeText(this, "Tên người dùng không được để trống", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. If an image is selected from gallery, save it to internal storage
        if (selectedImageUri != null) {
            try {
                InputStream is = getContentResolver().openInputStream(selectedImageUri);
                File internalDir = getFilesDir();
                File avatarFile = new File(internalDir, "avatar_" + userId + ".jpg");

                FileOutputStream fos = new FileOutputStream(avatarFile);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
                fos.flush();
                fos.close();
                is.close();

                savedAvatarPath = avatarFile.getAbsolutePath();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Có lỗi xảy ra khi lưu ảnh đại diện", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 2. Update Database via a new clean custom database update method in NguoiDungDAO
        // (We will make sure NguoiDungDAO supports updateUserProfile or we will modify NguoiDungDAO safely)
        int updateResult = userDAO.updateUserProfile(userId, username, email, savedAvatarPath);

        if (updateResult > 0) {
            // 3. Save Phone Number and Username to SharedPreferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("phone_" + userId, phone);
            editor.putString("username", username); // keep it synced
            editor.apply();

            Toast.makeText(this, "Cập nhật hồ sơ thành công!", Toast.LENGTH_SHORT).show();
            
            // Set result and close
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Có lỗi xảy ra khi lưu thông tin cơ sở dữ liệu", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDAO != null) {
            userDAO.close();
        }
    }
}
