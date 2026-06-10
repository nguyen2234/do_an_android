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

        btnStatistics = view.findViewById(R.id.btnStatistics);
        btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnTransferMoney = view.findViewById(R.id.btnTransferMoney);

        // Load thông tin NguoiDung
        loadUserProfile();

        // Xử lý Chỉnh sửa hồ sơ
        if (btnEditProfile != null) {
            btnEditProfile.setOnClickListener(v -> {
                // Sử dụng CaiDatActivity thay vì ChinhSuaHoSoActivity vì đã gộp
                Intent intent = new Intent(getActivity(), com.example.android_app.CaiDatActivity.class);
                startActivity(intent);
            });
        }

        // Xử lý Chuyển tiền
        if (btnTransferMoney != null) {
            btnTransferMoney.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), com.example.android_app.ChuyenTienActivity.class);
                startActivity(intent);
            });
        }
        
        // Xử lý Thống kê (Chuyển hướng sang màn hình Thống kê chính thức)
        if (btnStatistics != null) {
            btnStatistics.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).setSelectedItemId(R.id.nav_stats);
                }
            });
        }

        // Xử lý Quản lý
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
                tvProfileName.setText(user.getUsername());
                if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                    tvProfileEmail.setText(user.getEmail());
                } else {
                    tvProfileEmail.setText("Chưa cập nhật Email");
                }

                // Load Avatar
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



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (userDAO != null) userDAO.close();
    }
}
