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


public class ThemNganSachActivity extends AppCompatActivity {

    
    public static final String EXTRA_EDIT_BUDGET_ID = "edit_budget_id";

    private EditText etBudgetName, etBudgetAmount;
    private TextView tvStartDate, tvEndDate;
    private LinearLayout containerCategories;
    private NganSachDAO budgetDAO;
    private DanhMucDAO categoryDAO;
    private Calendar startCal = Calendar.getInstance();
    private Calendar endCal = Calendar.getInstance();

    private List<CheckBox> categoryCheckboxes = new ArrayList<>();
    
    private List<DanhMuc> categoryList = new ArrayList<>();

    
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

        
        editBudgetId = getIntent().getIntExtra(EXTRA_EDIT_BUDGET_ID, -1);
        isEditMode = (editBudgetId != -1);

        
        if (isEditMode) {
            TextView tvTitle = (TextView) ((android.view.ViewGroup)
                    ((android.view.ViewGroup) getWindow().getDecorView()).getChildAt(0)).getChildAt(1);
            
            
        }

        findViewById(R.id.btnBackAddBudget).setOnClickListener(v -> finish());

        etBudgetName = findViewById(R.id.etBudgetName);
        etBudgetAmount = findViewById(R.id.etBudgetAmount);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        containerCategories = findViewById(R.id.containerCategories);

        setupDatePickers();
        setupCategoryCheckboxes(); 

        
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

    
    private void setupCategoryCheckboxes() {
        
        Set<String> usedCategoryNames = isEditMode
                ? budgetDAO.getUsedCategoryNamesExcluding(editBudgetId)
                : budgetDAO.getUsedCategoryNames();

        List<DanhMuc> expenseCategories = categoryDAO.getAllCategories();

        
        if (expenseCategories.isEmpty()) {
            expenseCategories.add(new DanhMuc(0, "Ăn uống", "", "general", 0));
            expenseCategories.add(new DanhMuc(0, "Di chuyển", "", "general", 0));
            expenseCategories.add(new DanhMuc(0, "Mua sắm", "", "general", 0));
            expenseCategories.add(new DanhMuc(0, "Khác", "", "general", 0));
        }

        containerCategories.removeAllViews(); 
        categoryCheckboxes.clear();
        categoryList.clear();

        boolean anyAvailable = false;
        for (DanhMuc cat : expenseCategories) {
            String catName = cat.getName().trim();

            
            boolean isUsedByOther = usedCategoryNames.contains(catName);

            CheckBox cb = new CheckBox(this);
            cb.setText(catName + (isUsedByOther ? " (Đã lập ngân sách)" : ""));
            cb.setEnabled(!isUsedByOther); 
            cb.setTextSize(16);
            cb.setPadding(0, 8, 0, 8);
            
            cb.setAlpha(isUsedByOther ? 0.4f : 1.0f);

            containerCategories.addView(cb);
            categoryCheckboxes.add(cb);
            categoryList.add(cat);

            if (!isUsedByOther) anyAvailable = true;
        }

        
        if (!anyAvailable) {
            TextView tvNoCategory = new TextView(this);
            tvNoCategory.setText("⚠ Tất cả danh mục đã được lập ngân sách.\nVui lòng tạo thêm danh mục mới.");
            tvNoCategory.setTextSize(14);
            tvNoCategory.setPadding(0, 8, 0, 8);
            tvNoCategory.setTextColor(android.graphics.Color.parseColor("#E53935"));
            containerCategories.addView(tvNoCategory);
        }
    }

    
    private void prefillEditData() {
        NganSach oldBudget = budgetDAO.getBudgetById(editBudgetId);
        if (oldBudget == null) {
            Toast.makeText(this, "Không tìm thấy ngân sách!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        
        etBudgetName.setText(oldBudget.getName());
        etBudgetAmount.setText(String.valueOf((long) oldBudget.getAmount()));

        
        Set<String> oldCategories = new java.util.HashSet<>();
        if (oldBudget.getCategoryIds() != null) {
            for (String s : oldBudget.getCategoryIds().split(",")) {
                oldCategories.add(s.trim().toLowerCase());
            }
        }

        
        for (int i = 0; i < categoryCheckboxes.size(); i++) {
            CheckBox cb = categoryCheckboxes.get(i);
            if (i < categoryList.size()) {
                String catName = categoryList.get(i).getName().trim().toLowerCase();
                cb.setChecked(oldCategories.contains(catName));
            }
        }

        
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

        
        List<String> selectedCatNames = new ArrayList<>();
        for (CheckBox cb : categoryCheckboxes) {
            if (cb.isChecked() && cb.isEnabled()) {
                
                String fullText = cb.getText().toString();
                String realName = fullText.replace(" (Đã lập ngân sách)", "").trim();
                selectedCatNames.add(realName);
            }
        }

        if (selectedCatNames.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        
        
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
