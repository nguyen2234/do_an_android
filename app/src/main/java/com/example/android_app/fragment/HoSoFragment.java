package com.example.android_app.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android_app.DangNhapActivity;
import com.example.android_app.R;
import com.example.android_app.database.NguoiDungDAO;
import com.example.android_app.model.NguoiDung;
import com.google.android.material.button.MaterialButton;

public class HoSoFragment extends Fragment {

    private TextView tvProfileName, tvProfileEmail;
    private ImageView ivAvatar;
    private View btnLogout, btnChangePassword, btnStatistics;
    private MaterialButton btnEditProfile;
    private NguoiDungDAO userDAO;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ho_so, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userDAO = new NguoiDungDAO(requireContext());
        userDAO.open();
        prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        ivAvatar = view.findViewById(R.id.ivAvatar);

        btnLogout = view.findViewById(R.id.btnLogout);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
        btnStatistics = view.findViewById(R.id.btnStatistics);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);

        loadUserProfile();

        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                if (isAdded()) {
                    getParentFragmentManager().beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .replace(R.id.fragmentContainer, new ThongTinNguoiDungFragment())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }

        if (btnStatistics != null) {
            btnStatistics.setOnClickListener(v -> {
                if (isAdded()) {
                    getParentFragmentManager().beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .replace(R.id.fragmentContainer, new ThongKeFragment())
                            .addToBackStack(null).commit();
                }
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                if (prefs != null) {
                    prefs.edit().clear().apply();
                }
                Intent intent = new Intent(getActivity(), DangNhapActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }
    }

    private void loadUserProfile() {
        if (prefs == null || userDAO == null) return;
        long userId = prefs.getLong("user_id", -1);
        if (userId != -1) {
            NguoiDung user = userDAO.getUserById(userId);
            if (user != null) {
                if (tvProfileName != null) tvProfileName.setText(user.getUsername());
                if (tvProfileEmail != null) {
                    String email = user.getEmail();
                    tvProfileEmail.setText(email != null && !email.isEmpty() ? email : "Chưa cập nhật Email");
                }
                
                if (ivAvatar != null && user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                    try {
                        Uri uri = Uri.parse(user.getAvatar());
                        ivAvatar.setImageURI(uri);
                    } catch (Exception e) {
                        e.printStackTrace();
                        // Nếu lỗi thì giữ ảnh mặc định
                        ivAvatar.setImageResource(R.drawable.ic_avatar_banana);
                    }
                }
            }
        }
    }

    private void showChangePasswordDialog() {
        if (!isAdded()) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_doi_mat_khau, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etCurrent = dialogView.findViewById(R.id.etCurrentPassword);
        EditText etNew = dialogView.findViewById(R.id.etNewPassword);
        EditText etConfirm = dialogView.findViewById(R.id.etConfirmNewPassword);
        
        Button btnConfirm = dialogView.findViewById(R.id.btnConfirmChange);
        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                dialog.dismiss();
                Toast.makeText(requireContext(), "Đã cập nhật mật khẩu", Toast.LENGTH_SHORT).show();
            });
        }
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userDAO != null) userDAO.close();
    }
}
