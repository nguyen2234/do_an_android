package com.example.android_app;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.android_app.database.NganSachDAO;
import com.example.android_app.database.DanhMucDAO;
import com.example.android_app.model.NganSach;
import com.example.android_app.model.DanhMuc;
import com.google.android.material.button.MaterialButton;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Màn hình Thêm / Sửa Ngân sách.
 *
 * Hai chế độ hoạt động:
 * - THÊM MỚI: Không có EXTRA_EDIT_BUDGET_ID trong Intent → Tạo ngân sách mới.
 * - SỬA:      Có EXTRA_EDIT_BUDGET_ID trong Intent → Pre-fill dữ liệu và cập nhật.
 *
 * Quy tắc Unique Category:
 * - Khi mở form, lọc bỏ các danh mục đã được dùng trong ngân sách khác.
 * - Khi lưu, kiểm tra lại để chặn trùng lặp ngay cả khi người dùng cố tình submit.
 */
public class ThemNganSachActivity extends AppCompatActivity {

    // Key truyền qua Intent để phân biệt chế độ Thêm / Sửa
    public static final String EXTRA_EDIT_BUDGET_ID = "edit_budget_id";

    private EditText etBudgetName, etBudgetAmount;
    private TextView tvStartDate, tvEndDate;
    private LinearLayout containerCategories;
    private NganSachDAO budgetDAO;
    private DanhMucDAO categoryDAO;
    private Calendar startCal = Calendar.getInstance();
    private Calendar endCal = Calendar.getInstance();

    private List<CheckBox> categoryCheckboxes = new ArrayList<>();
    /** Danh sách DanhMuc tương ứng với từng CheckBox (cùng thứ tự) */
    private List<DanhMuc> categoryList = new ArrayList<>();

    // Trạng thái chế độ Edit
    private boolean isEditMode = false;
    private int editBudgetId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_ngan_sach);

        budgetDAO = new NganSachDAO(this);
        budgetDAO.open();

        categoryDAO = new DanhMucDAO(this);
        categoryDAO.open();

        // Xác định chế độ hoạt động
        editBudgetId = getIntent().getIntExtra(EXTRA_EDIT_BUDGET_ID, -1);
        isEditMode = (editBudgetId != -1);

        // Cập nhật tiêu đề AppBar nếu là chế độ Sửa
        if (isEditMode) {
            TextView tvTitle = (TextView) ((android.view.ViewGroup)
                    ((android.view.ViewGroup) getWindow().getDecorView()).getChildAt(0)).getChildAt(1);
            // Cách an toàn hơn: dùng id nếu header có TextView
            // Tạm tìm qua View trực tiếp
        }

        findViewById(R.id.btnBackAddBudget).setOnClickListener(v -> finish());

        etBudgetName = findViewById(R.id.etBudgetName);
        etBudgetAmount = findViewById(R.id.etBudgetAmount);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        containerCategories = findViewById(R.id.containerCategories);

        setupDatePickers();
        setupCategoryCheckboxes(); // Lọc danh mục đã dùng theo chế độ hiện tại

        // Nếu là chế độ Sửa, pre-fill dữ liệu cũ vào form
        if (isEditMode) {
            prefillEditData();
        }

        MaterialButton btnSaveBudget = findViewById(R.id.btnSaveBudget);
        btnSaveBudget.setText(isEditMode ? "Cập nhật Ngân sách" : "Lưu Ngân sách");
        btnSaveBudget.setOnClickListener(v -> saveBudget());
    }

    private void setupDatePickers() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi"));
        tvStartDate.setText(sdf.format(startCal.getTime()));

        endCal.add(Calendar.MONTH, 1);
        tvEndDate.setText(sdf.format(endCal.getTime()));

        findViewById(R.id.btnStartDate).setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                startCal.set(year, month, dayOfMonth);
                tvStartDate.setText(sdf.format(startCal.getTime()));
            }, startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DAY_OF_MONTH)).show();
        });

        findViewById(R.id.btnEndDate).setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                endCal.set(year, month, dayOfMonth);
                tvEndDate.setText(sdf.format(endCal.getTime()));
            }, endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH), endCal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    /**
     * Tạo danh sách CheckBox cho từng danh mục chi tiêu.
     *
     * Quy tắc Unique Category (Độc quyền Danh mục):
     * - Nếu THÊM MỚI: Ẩn (lọc bỏ) các danh mục đã có trong BẤT KỲ ngân sách nào.
     * - Nếu SỬA:      Ẩn các danh mục đã có trong ngân sách KHÁC (không phải ngân sách đang sửa).
     *   Vì vậy danh mục của chính ngân sách đang sửa vẫn hiển thị để người dùng có thể chọn lại.
     */
    private void setupCategoryCheckboxes() {
        // Lấy tập hợp danh mục đã được dùng (loại trừ ngân sách hiện tại nếu đang sửa)
        Set<String> usedCategoryNames = isEditMode
                ? budgetDAO.getUsedCategoryNamesExcluding(editBudgetId)
                : budgetDAO.getUsedCategoryNames();

        List<DanhMuc> expenseCategories = categoryDAO.getAllCategories();

        // Dự phòng: thêm một số danh mục mặc định nếu DB trống
        if (expenseCategories.isEmpty()) {
            expenseCategories.add(new DanhMuc(0, "Ăn uống", "", "general", 0));
            expenseCategories.add(new DanhMuc(0, "Di chuyển", "", "general", 0));
            expenseCategories.add(new DanhMuc(0, "Mua sắm", "", "general", 0));
            expenseCategories.add(new DanhMuc(0, "Khác", "", "general", 0));
        }

        containerCategories.removeAllViews(); // Xóa view cũ nếu có
        categoryCheckboxes.clear();
        categoryList.clear();

        boolean anyAvailable = false;
        for (DanhMuc cat : expenseCategories) {
            String catName = cat.getName().trim();

            // Kiểm tra xem danh mục này đã được lập ngân sách bởi ngân sách khác chưa
            boolean isUsedByOther = usedCategoryNames.contains(catName);

            CheckBox cb = new CheckBox(this);
            cb.setText(catName + (isUsedByOther ? " (Đã lập ngân sách)" : ""));
            cb.setEnabled(!isUsedByOther); // Disable nếu đã dùng
            cb.setTextSize(16);
            cb.setPadding(0, 8, 0, 8);
            // Màu mờ cho checkbox không khả dụng
            cb.setAlpha(isUsedByOther ? 0.4f : 1.0f);

            containerCategories.addView(cb);
            categoryCheckboxes.add(cb);
            categoryList.add(cat);

            if (!isUsedByOther) anyAvailable = true;
        }

        // Cảnh báo nếu không còn danh mục nào có thể chọn
        if (!anyAvailable) {
            TextView tvNoCategory = new TextView(this);
            tvNoCategory.setText("⚠ Tất cả danh mục đã được lập ngân sách.\nVui lòng tạo thêm danh mục mới.");
            tvNoCategory.setTextSize(14);
            tvNoCategory.setPadding(0, 8, 0, 8);
            tvNoCategory.setTextColor(android.graphics.Color.parseColor("#E53935"));
            containerCategories.addView(tvNoCategory);
        }
    }

    /**
     * Pre-fill dữ liệu cũ của ngân sách vào các field input (chế độ Sửa).
     * Gọi SAU setupCategoryCheckboxes() để tích chọn đúng danh mục cũ.
     */
    private void prefillEditData() {
        NganSach oldBudget = budgetDAO.getBudgetById(editBudgetId);
        if (oldBudget == null) {
            Toast.makeText(this, "Không tìm thấy ngân sách!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Điền tên và số tiền
        etBudgetName.setText(oldBudget.getName());
        etBudgetAmount.setText(String.valueOf((long) oldBudget.getAmount()));

        // Phân tích các danh mục cũ đã chọn
        Set<String> oldCategories = new java.util.HashSet<>();
        if (oldBudget.getCategoryIds() != null) {
            for (String s : oldBudget.getCategoryIds().split(",")) {
                oldCategories.add(s.trim().toLowerCase());
            }
        }

        // Tích checkbox theo danh mục cũ
        for (int i = 0; i < categoryCheckboxes.size(); i++) {
            CheckBox cb = categoryCheckboxes.get(i);
            if (i < categoryList.size()) {
                String catName = categoryList.get(i).getName().trim().toLowerCase();
                cb.setChecked(oldCategories.contains(catName));
            }
        }

        // Phân tích ngày
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi"));
        try {
            if (oldBudget.getStartDate() != null) {
                java.util.Date startD = sdf.parse(oldBudget.getStartDate());
                if (startD != null) startCal.setTime(startD);
            }
            if (oldBudget.getEndDate() != null) {
                java.util.Date endD = sdf.parse(oldBudget.getEndDate());
                if (endD != null) endCal.setTime(endD);
            }
        } catch (Exception ignored) {}

        tvStartDate.setText(sdf.format(startCal.getTime()));
        tvEndDate.setText(sdf.format(endCal.getTime()));
    }

    /**
     * Lưu hoặc cập nhật ngân sách sau khi validate.
     *
     * Validation gồm:
     * 1. Số tiền không được trống.
     * 2. Phải chọn ít nhất 1 danh mục.
     * 3. Danh mục đã chọn không được trùng với ngân sách khác (kiểm tra lại ở tầng lưu để đảm bảo).
     */
    private void saveBudget() {
        String name = etBudgetName.getText().toString().trim();
        String amountStr = etBudgetAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền hạn mức", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty()) {
            name = "Ngân sách";
        }

        double amount = 0;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        // -- Thu thập danh mục đã chọn --
        List<String> selectedCatNames = new ArrayList<>();
        for (CheckBox cb : categoryCheckboxes) {
            if (cb.isChecked() && cb.isEnabled()) {
                // Lấy tên thật (bỏ phần suffix " (Đã lập ngân sách)" nếu có)
                String fullText = cb.getText().toString();
                String realName = fullText.replace(" (Đã lập ngân sách)", "").trim();
                selectedCatNames.add(realName);
            }
        }

        if (selectedCatNames.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        // -- VALIDATE trùng lặp danh mục (lần kiểm tra cuối trước khi lưu) --
        // Lấy danh sách đã dùng bởi ngân sách KHÁC
        Set<String> usedByOthers = isEditMode
                ? budgetDAO.getUsedCategoryNamesExcluding(editBudgetId)
                : budgetDAO.getUsedCategoryNames();

        List<String> conflictCats = new ArrayList<>();
        for (String selCat : selectedCatNames) {
            if (usedByOthers.contains(selCat.trim())) {
                conflictCats.add(selCat);
            }
        }

        if (!conflictCats.isEmpty()) {
            Toast.makeText(this,
                    "Danh mục này đã được lập ngân sách, vui lòng chọn danh mục khác: "
                            + conflictCats.toString(),
                    Toast.LENGTH_LONG).show();
            return;
        }

        String categoryIds = android.text.TextUtils.join(", ", selectedCatNames);
        String startDate = tvStartDate.getText().toString();
        String endDate = tvEndDate.getText().toString();

        // -- Thực thi lưu hoặc cập nhật --
        if (isEditMode) {
            NganSach updatedBudget = new NganSach(editBudgetId, name, amount, 0, startDate, endDate, categoryIds);
            int rows = budgetDAO.updateNganSach(updatedBudget);
            if (rows > 0) {
                Toast.makeText(this, "Cập nhật ngân sách thành công!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi cập nhật ngân sách", Toast.LENGTH_SHORT).show();
            }
        } else {
            NganSach budget = new NganSach(0, name, amount, 0, startDate, endDate, categoryIds);
            long result = budgetDAO.addNganSach(budget);
            if (result > 0) {
                Toast.makeText(this, "Tạo ngân sách thành công!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Lỗi khi tạo ngân sách", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (budgetDAO != null) budgetDAO.close();
        if (categoryDAO != null) categoryDAO.close();
    }
}
