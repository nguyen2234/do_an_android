package com.example.android_app.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.android_app.DangNhapActivity;
import com.example.android_app.MainActivity;
import com.example.android_app.R;
import com.example.android_app.database.NguoiDungDAO;
import com.example.android_app.model.NguoiDung;

public class HoSoFragment extends Fragment {

    private static final int EDIT_PROFILE_REQUEST = 102;

    private TextView tvProfileName, tvProfileEmail;
    private android.widget.ImageView ivAvatar;

    private LinearLayout btnStatistics, btnEditProfile, btnTransferMoney;
    private NguoiDungDAO userDAO;
    private SharedPreferences prefs;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ho_so, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        userDAO = new NguoiDungDAO(getContext());
        userDAO.open();
        prefs = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        ivAvatar = view.findViewById(R.id.ivAvatar);


        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnTransferMoney = view.findViewById(R.id.btnTransferMoney);

        
        loadUserProfile();

        
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                
                Intent intent = new Intent(getActivity(), com.example.android_app.CaiDatActivity.class);
                startActivity(intent);
            });
        }

        
        if (btnTransferMoney != null) {
            btnTransferMoney.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), com.example.android_app.ChuyenTienActivity.class);
                startActivity(intent);
            });
        }

        
        View btnChangePin = view.findViewById(R.id.btnChangePin);
        if (btnChangePin != null) {
            btnChangePin.setOnClickListener(v -> showVerifyPasswordDialog());
        }

        
        View btnLogoutProfile = view.findViewById(R.id.btnLogoutProfile);
        if (btnLogoutProfile != null) {
            btnLogoutProfile.setOnClickListener(v -> {
                prefs.edit().clear().apply();
                Toast.makeText(getContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), DangNhapActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            });
        }
        
        
        if (btnStatistics != null) {
            btnStatistics.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).setSelectedItemId(R.id.nav_stats);
                }
            });
        }

        
        View btnManageCategories = view.findViewById(R.id.btnManageCategories);
        View btnManageWallets = view.findViewById(R.id.btnManageWallets);

        if (btnManageCategories != null) {
            btnManageCategories.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, new DanhMucFragment())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        if (btnManageWallets != null) {
            btnManageWallets.setOnClickListener(v -> {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragmentContainer, new ViTienFragment())
                            .addToBackStack(null)
                            .commit();
                }
            });
        }


    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        long userId = prefs.getLong("user_id", -1);
        if (userId != -1) {
            NguoiDung user = userDAO.getUserById(userId);
            if (user != null) {
                if (user.getFullname() != null && !user.getFullname().isEmpty()) {
                    tvProfileName.setText(user.getFullname());
                } else {
                    tvProfileName.setText("Chưa cập nhật Họ tên");
                }
                if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                    tvProfileEmail.setText(user.getEmail());
                } else {
                    tvProfileEmail.setText("Chưa cập nhật Email");
                }

                
                String avatarPath = user.getAvatar();
                if (avatarPath != null && !avatarPath.isEmpty()) {
                    if (avatarPath.startsWith("/")) {
                        java.io.File imgFile = new java.io.File(avatarPath);
                        if (imgFile.exists()) {
                            android.graphics.Bitmap myBitmap = android.graphics.BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                            ivAvatar.setImageBitmap(myBitmap);
                        } else {
                            ivAvatar.setImageResource(R.drawable.ic_avatar_banana);
                        }
                    } else {
                        int resId = getResources().getIdentifier(avatarPath, "drawable", getContext().getPackageName());
                        if (resId != 0) ivAvatar.setImageResource(resId);
                        else ivAvatar.setImageResource(R.drawable.ic_avatar_banana);
                    }
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_avatar_banana);
                }
            }
        }
    }

    private void showVerifyPasswordDialog() {
        if (getContext() == null) return;

        long userId = prefs.getLong("user_id", -1);
        if (userId == -1) {
            Toast.makeText(getContext(), "Không tìm thấy thông tin tài khoản", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_verify_password, null);
        final EditText etVerifyPassword = dialogView.findViewById(R.id.etVerifyPassword);
        android.widget.Button btnCancelVerifyPassword = dialogView.findViewById(R.id.btnCancelVerifyPassword);
        android.widget.Button btnConfirmVerifyPassword = dialogView.findViewById(R.id.btnConfirmVerifyPassword);

        final androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnCancelVerifyPassword.setOnClickListener(v -> dialog.dismiss());

        btnConfirmVerifyPassword.setOnClickListener(v -> {
            String password = etVerifyPassword.getText().toString().trim();
            if (password.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập mật khẩu", Toast.LENGTH_SHORT).show();
                return;
            }

            NguoiDung user = userDAO.getUserById(userId);
            if (user != null) {
                String hashedInput = com.example.android_app.utils.SecurityUtils.hashPasswordSHA256(password);
                if (hashedInput.equals(user.getPassword())) {
                    dialog.dismiss();
                    showChangePinDialog(userId);
                } else {
                    Toast.makeText(getContext(), "Mật khẩu không chính xác!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "Lỗi hệ thống khi tìm người dùng", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showChangePinDialog(final long userId) {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_pin, null);
        final EditText etNewPin = dialogView.findViewById(R.id.etNewPin);
        final EditText etConfirmPin = dialogView.findViewById(R.id.etConfirmPin);
        android.widget.Button btnCancelChangePin = dialogView.findViewById(R.id.btnCancelChangePin);
        android.widget.Button btnConfirmChangePin = dialogView.findViewById(R.id.btnConfirmChangePin);

        final androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnCancelChangePin.setOnClickListener(v -> dialog.dismiss());

        btnConfirmChangePin.setOnClickListener(v -> {
            String newPin = etNewPin.getText().toString().trim();
            String confirmPin = etConfirmPin.getText().toString().trim();

            if (newPin.isEmpty() || confirmPin.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập đầy đủ mã PIN", Toast.LENGTH_SHORT).show();
                return;
            }

            if (newPin.length() != 6 || !newPin.matches("\\d{6}")) {
                Toast.makeText(getContext(), "Mã PIN phải gồm đúng 6 chữ số", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPin.equals(confirmPin)) {
                Toast.makeText(getContext(), "Xác nhận mã PIN không khớp", Toast.LENGTH_SHORT).show();
                return;
            }

            int result = userDAO.updateTransactionPin(userId, newPin);
            if (result > 0) {
                Toast.makeText(getContext(), "Đổi mã PIN giao dịch thành công!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Đã xảy ra lỗi khi đổi mã PIN", Toast.LENGTH_SHORT).show();
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
