package com.example.android_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_app.adapter.ViTienAdapter;
import com.example.android_app.database.ViTienDAO;
import com.example.android_app.model.ViTien;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * Màn hình Onboarding – Bước 2: Thiết lập Ví tiền.
 *
 * LUỒNG ĐIỀU HƯỚNG:
 *   SetupCategoryActivity
 *       → SetupWalletActivity  (màn hình này)
 *       → MainActivity         (khi nhấn "Hoàn thành" và walletCount >= 1)
 *
 * LOGIC CHẶN ĐIỀU HƯỚNG:
 *   Nút "Hoàn thành" (btnFinishSetupWallet) bị disabled từ đầu.
 *   Sau mỗi lần tạo ví thành công, refreshGuard() được gọi:
 *   - Nếu getWalletCount() >= 1 → enable nút, alpha = 1.0f
 *   - Nếu == 0 → giữ disabled, alpha = 0.5f
 *
 * KHI HOÀN THÀNH:
 *   - Lưu isOnboardingDone = true vào SharedPreferences
 *   - Chuyển sang MainActivity và xóa back stack (không thể quay lại onboarding)
 */
public class SetupWalletActivity extends AppCompatActivity {

    // Key đánh dấu onboarding đã hoàn thành
    private static final String PREF_KEY = "UserPrefs";
    private static final String PREF_ONBOARDING_DONE = "isOnboardingDone";

    // ===== UI =====
    private TextInputEditText etWalletName, etWalletBalance;
    private MaterialButton btnAddWallet, btnFinish;
    private RecyclerView rvWallets;
    private TextView tvWalletCount;

    // ===== DATA =====
    private ViTienDAO walletDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_wallet);

        walletDAO = new ViTienDAO(this);
        walletDAO.open();

        initViews();
        setupClickListeners();
        refreshList(); // Hiển thị ví hiện có (nếu có) và cập nhật guard
    }

    /**
     * Ánh xạ View từ XML.
     */
    private void initViews() {
        etWalletName = findViewById(R.id.etWalletNameSetup);
        etWalletBalance = findViewById(R.id.etWalletBalanceSetup);
        btnAddWallet = findViewById(R.id.btnAddWalletSetup);
        btnFinish = findViewById(R.id.btnFinishSetupWallet);
        rvWallets = findViewById(R.id.rvWalletsSetup);
        tvWalletCount = findViewById(R.id.tvWalletCountSetup);

        rvWallets.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Gắn sự kiện click.
     */
    private void setupClickListeners() {
        // Nút thêm ví
        btnAddWallet.setOnClickListener(v -> addWallet());

        // Nút Hoàn thành: chỉ hoạt động khi walletCount >= 1
        btnFinish.setOnClickListener(v -> finishOnboarding());
    }

    /**
     * Xử lý tạo ví mới.
     *
     * Validation:
     * - Tên ví không được trống
     * - Số dư có thể để trống (mặc định = 0)
     *
     * Sau khi thêm:
     * - Xóa form nhập
     * - Gọi refreshList() để cập nhật danh sách và guard
     */
    private void addWallet() {
        String name = etWalletName.getText() != null
                ? etWalletName.getText().toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên ví", Toast.LENGTH_SHORT).show();
            return;
        }

        // Số dư ban đầu — để trống thì mặc định là 0
        double balance = 0;
        String balanceStr = etWalletBalance.getText() != null
                ? etWalletBalance.getText().toString().trim() : "";
        if (!balanceStr.isEmpty()) {
            try {
                balance = Double.parseDouble(balanceStr);
                if (balance < 0) {
                    Toast.makeText(this, "Số dư không được âm", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số dư không hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Tạo ví mới với loại "cash" (mặc định cho onboarding)
        ViTien newWallet = new ViTien();
        newWallet.setName(name);
        newWallet.setBalance(balance);
        newWallet.setLoai("cash");
        newWallet.setCurrency("VND");
        newWallet.setIcon("cash");
        newWallet.setColor("#4CAF50"); // Màu xanh mặc định

        long id = walletDAO.addWallet(newWallet);

        if (id > 0) {
            Toast.makeText(this, "✅ Đã tạo ví \"" + name + "\"", Toast.LENGTH_SHORT).show();
            etWalletName.setText("");
            etWalletBalance.setText("");
            refreshList(); // Cập nhật danh sách + enable guard nếu đủ điều kiện
        } else {
            Toast.makeText(this, "Lỗi khi tạo ví", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Làm mới danh sách ví và cập nhật trạng thái nút "Hoàn thành".
     *
     * *** ĐÂY LÀ LOGIC CHẶN ĐIỀU HƯỚNG CHÍNH ***
     *
     * Quy tắc:
     * - getWalletCount() == 0 → btnFinish.setEnabled(false), alpha = 0.5
     * - getWalletCount() >= 1 → btnFinish.setEnabled(true),  alpha = 1.0
     */
    private void refreshList() {
        List<ViTien> list = walletDAO.getAllWallets();
        int count = list.size();

        // Cập nhật counter text
        tvWalletCount.setText(count + " ví");

        // Cập nhật RecyclerView
        ViTienAdapter adapter = new ViTienAdapter(this, list);
        rvWallets.setAdapter(adapter);

        // *** GUARD LOGIC: Enable/Disable nút "Hoàn thành" ***
        if (count >= 1) {
            // Có ít nhất 1 ví → CHO PHÉP hoàn thành onboarding
            btnFinish.setEnabled(true);
            btnFinish.setAlpha(1.0f);
        } else {
            // Chưa có ví → CHẶN
            btnFinish.setEnabled(false);
            btnFinish.setAlpha(0.5f);
        }
    }

    /**
     * Hoàn thành quá trình Onboarding.
     *
     * Công việc:
     * 1. Lưu cờ isOnboardingDone = true vào SharedPreferences
     *    → Lần đăng nhập tiếp theo sẽ bỏ qua Onboarding, vào thẳng MainActivity
     * 2. Chuyển sang MainActivity và XÓA TOÀN BỘ BACK STACK
     *    → Người dùng không thể nhấn "Back" để quay lại SetupWallet hay SetupCategory
     */
    private void finishOnboarding() {
        // Bước 1: Đánh dấu Onboarding đã hoàn thành
        SharedPreferences prefs = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_ONBOARDING_DONE, true).apply();

        // Bước 2: Vào MainActivity, xóa back stack
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // finish() không cần thiết vì FLAG_ACTIVITY_CLEAR_TASK đã xử lý
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (walletDAO != null) walletDAO.close();
    }
}
