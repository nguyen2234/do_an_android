package com.example.android_app.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.android_app.R;
import com.example.android_app.database.NguoiDungDAO;
import com.example.android_app.model.NguoiDung;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class ThongTinNguoiDungFragment extends Fragment {

    private TextInputEditText etUsername, etEmail, etPhone;
    private ImageView ivAvatar;
    private NguoiDungDAO userDAO;
    private SharedPreferences prefs;
    private NguoiDung currentUser;
    private Uri selectedImageUri;

    private final ActivityResultLauncher<String> mGetContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    if (ivAvatar != null) {
                        ivAvatar.setImageURI(uri);
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_thong_tin_nguoi_dung, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        etUsername = view.findViewById(R.id.etUsername);
        etEmail = view.findViewById(R.id.etEmail);
        etPhone = view.findViewById(R.id.etPhone);
        ivAvatar = view.findViewById(R.id.ivUserAvatar);
        MaterialButton btnUpdate = view.findViewById(R.id.btnUpdateProfile);
        View btnChangeAvatar = view.findViewById(R.id.btnChangeAvatar);
        
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> {
                if (isAdded()) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }

        userDAO = new NguoiDungDAO(requireContext());
        userDAO.open();
        prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        loadUserData();

        if (btnUpdate != null) {
            btnUpdate.setOnClickListener(v -> updateProfile());
        }
        
        if (btnChangeAvatar != null) {
            btnChangeAvatar.setOnClickListener(v -> mGetContent.launch("image/*"));
        }
    }

    private void loadUserData() {
        if (prefs == null || userDAO == null) return;
        long userId = prefs.getLong("user_id", -1);
        if (userId != -1) {
            currentUser = userDAO.getUserById(userId);
            if (currentUser != null) {
                if (etUsername != null) etUsername.setText(currentUser.getUsername());
                if (etEmail != null) etEmail.setText(currentUser.getEmail());
                if (etPhone != null) etPhone.setText(currentUser.getPhone());
                
                if (ivAvatar != null && currentUser.getAvatar() != null && !currentUser.getAvatar().isEmpty()) {
                    try {
                        Uri uri = Uri.parse(currentUser.getAvatar());
                        ivAvatar.setImageURI(uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ivAvatar.setImageResource(R.drawable.ic_avatar_banana);
                    }
                }
            }
        }
    }

    private void updateProfile() {
        if (!isAdded() || currentUser == null || etUsername == null || etEmail == null || etPhone == null) return;

        String newUsername = Objects.requireNonNull(etUsername.getText()).toString().trim();
        String newEmail = Objects.requireNonNull(etEmail.getText()).toString().trim();
        String newPhone = Objects.requireNonNull(etPhone.getText()).toString().trim();

        if (newUsername.isEmpty()) {
            etUsername.setError("Tên không được để trống");
            return;
        }

        currentUser.setUsername(newUsername);
        currentUser.setEmail(newEmail);
        currentUser.setPhone(newPhone);
        
        if (selectedImageUri != null) {
            currentUser.setAvatar(selectedImageUri.toString());
        }

        if (userDAO != null && userDAO.updateUser(currentUser) > 0) {
            Toast.makeText(requireContext(), "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
            if (isAdded()) {
                getParentFragmentManager().popBackStack();
            }
        } else {
            Toast.makeText(requireContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userDAO != null) {
            userDAO.close();
        }
    }
}
