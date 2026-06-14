package com.example.android_app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_app.adapter.DanhMucAdapter;
import com.example.android_app.database.DanhMucDAO;
import com.example.android_app.model.DanhMuc;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * Màn hình Onboarding – Bước 1: Thiết lập Danh mục.
 *
 * LUỒNG ĐIỀU HƯỚNG:
 *   [DangNhapActivity / DangKyActivity]
 *       → SetupCategoryActivity  (bước này)
 *       → SetupWalletActivity
 *       → MainActivity
 *
 * LOGIC CHẶN ĐIỀU HƯỚNG:
 *   Nút "Tiếp tục" (btnContinueSetupCategory) bị disabled từ đầu.
 *   Sau mỗi lần thêm danh mục thành công, refreshGuard() được gọi:
 *   - Nếu getCategoryCount() >= 1 → enable nút, alpha = 1.0f
 *   - Nếu == 0 → giữ disabled, alpha = 0.5f
 *
 * DANH MỤC CHUNG:
 *   Danh mục trong màn hình này không cần chọn loại (thu/chi).
 *   Tất cả lưu với type = "general".
 */
public class SetupCategoryActivity extends AppCompatActivity {

    // ===== KEY =====
    /** SharedPreferences key đánh dấu onboarding đã hoàn thành */
    private static final String PREF_ONBOARDING_DONE = "isOnboardingDone";

    // ===== UI =====
    private TextInputEditText etCategoryName;
    private MaterialButton btnAddCategory, btnContinue;
    private RecyclerView rvCategories;
    private TextView tvCategoryCount;

    /** Mảng View màu và giá trị màu tương ứng */
    private View[] colorViews;
    private final int[] colors = {
            Color.parseColor("#8B5CF6"), // Tím
            Color.parseColor("#E74C3C"), // Đỏ
            Color.parseColor("#2ECC71"), // Xanh lá
            Color.parseColor("#F1C40F"), // Vàng
            Color.parseColor("#3498DB")  // Xanh dương
    };
    /** Màu đang được chọn (mặc định = màu đầu tiên) */
    private int selectedColor;

    // ===== DATA =====
    private DanhMucDAO categoryDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_category);

        // Khởi tạo DAO
        categoryDAO = new DanhMucDAO(this);
        categoryDAO.open();

        // Khởi tạo màu mặc định
        selectedColor = colors[0];

        initViews();
        setupColorPicker();
        setupClickListeners();
        refreshList();  // Load danh sách hiện có (nếu có) và cập nhật guard
    }

    /**
     * Ánh xạ View từ XML.
     */
    private void initViews() {
        etCategoryName = findViewById(R.id.etCategoryNameSetup);
        btnAddCategory = findViewById(R.id.btnAddCategorySetup);
        btnContinue = findViewById(R.id.btnContinueSetupCategory);
        rvCategories = findViewById(R.id.rvCategoriesSetup);
        tvCategoryCount = findViewById(R.id.tvCategoryCountSetup);

        rvCategories.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Thiết lập bộ chọn màu.
     * Khi bấm một màu: màu đó alpha=1.0, các màu còn lại alpha=0.35.
     */
    private void setupColorPicker() {
        colorViews = new View[]{
                findViewById(R.id.colorSetup1),
                findViewById(R.id.colorSetup2),
                findViewById(R.id.colorSetup3),
                findViewById(R.id.colorSetup4),
                findViewById(R.id.colorSetup5)
        };

        // Khởi tạo trạng thái visual
        colorViews[0].setAlpha(1.0f); // Màu đầu tiên được chọn mặc định
        for (int i = 1; i < colorViews.length; i++) {
            colorViews[i].setAlpha(0.35f);
        }

        // Gắn click listener cho từng màu
        for (int i = 0; i < colorViews.length; i++) {
            final int index = i;
            colorViews[i].setOnClickListener(v -> {
                selectedColor = colors[index];
                // Cập nhật visual: chỉ màu được chọn sáng
                for (View cv : colorViews) cv.setAlpha(0.35f);
                v.setAlpha(1.0f);
            });
        }
    }

    /**
     * Gắn sự kiện click cho nút Thêm và nút Tiếp tục.
     */
    private void setupClickListeners() {
        // Nút Thêm danh mục
        btnAddCategory.setOnClickListener(v -> addCategory());

        // Nút Tiếp tục: chuyển sang SetupWalletActivity
        // Chú ý: nút này sẽ chỉ active khi categoryCount >= 1 (xem refreshGuard())
        btnContinue.setOnClickListener(v -> {
            Intent intent = new Intent(this, SetupWalletActivity.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Xử lý thêm danh mục mới.
     *
     * Validation:
     * - Tên không được để trống
     *
     * Sau khi thêm thành công:
     * - Xóa ô nhập
     * - Gọi refreshList() để cập nhật danh sách và kiểm tra guard
     */
    private void addCategory() {
        String name = etCategoryName.getText() != null
                ? etCategoryName.getText().toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        // Lưu danh mục với type = "general" (danh mục chung, không phân biệt thu/chi)
        DanhMuc newCategory = new DanhMuc(0, name, "ic_menu_sort_by_size", "general", selectedColor);
        long id = categoryDAO.addCategory(newCategory);

        if (id > 0) {
            Toast.makeText(this, "✅ Đã thêm danh mục \"" + name + "\"", Toast.LENGTH_SHORT).show();
            etCategoryName.setText(""); // Xóa ô nhập để thêm tiếp
            refreshList();              // Cập nhật danh sách + enable guard nếu đủ điều kiện
        } else {
            Toast.makeText(this, "Lỗi khi thêm danh mục", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Làm mới danh sách hiển thị và cập nhật trạng thái nút "Tiếp tục".
     *
     * *** ĐÂY LÀ LOGIC CHẶN ĐIỀU HƯỚNG CHÍNH ***
     *
     * Quy tắc:
     * - getCategoryCount() == 0 → btnContinue.setEnabled(false), alpha = 0.5
     * - getCategoryCount() >= 1 → btnContinue.setEnabled(true),  alpha = 1.0
     */
    private void refreshList() {
        List<DanhMuc> list = categoryDAO.getAllCategories();
        int count = list.size();

        // Cập nhật counter text
        tvCategoryCount.setText(count + " danh mục");

        // Cập nhật RecyclerView
        DanhMucAdapter adapter = new DanhMucAdapter(this, list, new DanhMucAdapter.OnCategoryClickListener() {
            @Override
            public void onEditClick(DanhMuc category) {
                // Trong màn hình setup, không cần chỉnh sửa phức tạp
            }

            @Override
            public void onDeleteClick(DanhMuc category) {
                categoryDAO.deleteCategory(category.getId());
                refreshList(); // Cập nhật lại sau khi xóa
            }
        });
        rvCategories.setAdapter(adapter);

        // *** GUARD LOGIC: Enable/Disable nút "Tiếp tục" ***
        if (count >= 1) {
            // Có ít nhất 1 danh mục → CHO PHÉP tiếp tục
            btnContinue.setEnabled(true);
            btnContinue.setAlpha(1.0f);
        } else {
            // Chưa có danh mục → CHẶN, không cho tiếp tục
            btnContinue.setEnabled(false);
            btnContinue.setAlpha(0.5f);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (categoryDAO != null) categoryDAO.close();
    }
}
