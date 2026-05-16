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

public class ThemNganSachActivity extends AppCompatActivity {

    private EditText etBudgetName, etBudgetAmount;
    private TextView tvStartDate, tvEndDate;
    private LinearLayout containerCategories;
    private NganSachDAO budgetDAO;
    private DanhMucDAO categoryDAO;
    private Calendar startCal = Calendar.getInstance();
    private Calendar endCal = Calendar.getInstance();

    private List<CheckBox> categoryCheckboxes = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_them_ngan_sach);

        budgetDAO = new NganSachDAO(this);
        budgetDAO.open();
        
        categoryDAO = new DanhMucDAO(this);
        categoryDAO.open();

        findViewById(R.id.btnBackAddBudget).setOnClickListener(v -> finish());

        etBudgetName = findViewById(R.id.etBudgetName);
        etBudgetAmount = findViewById(R.id.etBudgetAmount);
        tvStartDate = findViewById(R.id.tvStartDate);
        tvEndDate = findViewById(R.id.tvEndDate);
        containerCategories = findViewById(R.id.containerCategories);

        setupDatePickers();
        setupCategoryCheckboxes();

        MaterialButton btnSaveBudget = findViewById(R.id.btnSaveBudget);
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
        List<DanhMuc> expenseCategories = categoryDAO.getCategoriesByType("expense");
        
        // Nếu trống thì vẫn cung cấp vài tuỳ chọn cơ bản
        if (expenseCategories.isEmpty()) {
            expenseCategories.add(new DanhMuc(0, "Ăn uống", "", "expense", 0));
            expenseCategories.add(new DanhMuc(0, "Di chuyển", "", "expense", 0));
            expenseCategories.add(new DanhMuc(0, "Khác", "", "expense", 0));
        }

        for (DanhMuc cat : expenseCategories) {
            CheckBox cb = new CheckBox(this);
            cb.setText(cat.getName());
            cb.setTextSize(16);
            cb.setPadding(0, 8, 0, 8);
            containerCategories.addView(cb);
            categoryCheckboxes.add(cb);
        }
    }

    private void saveBudget() {
        String name = etBudgetName.getText().toString().trim();
        String amountStr = etBudgetAmount.getText().toString().trim();

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty()) {
            name = "Ngân sách";
        }

        double amount = Double.parseDouble(amountStr);
        String startDate = tvStartDate.getText().toString();
        String endDate = tvEndDate.getText().toString();

        StringBuilder selectedCats = new StringBuilder();
        for (CheckBox cb : categoryCheckboxes) {
            if (cb.isChecked()) {
                if (selectedCats.length() > 0) selectedCats.append(", ");
                selectedCats.append(cb.getText().toString());
            }
        }

        if (selectedCats.length() == 0) {
            Toast.makeText(this, "Vui lòng chọn ít nhất 1 danh mục", Toast.LENGTH_SHORT).show();
            return;
        }

        NganSach budget = new NganSach(0, name, amount, 0, startDate, endDate, selectedCats.toString());
        long result = budgetDAO.addNganSach(budget);

        if (result > 0) {
            Toast.makeText(this, "Tạo ngân sách thành công", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Lỗi khi tạo ngân sách", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (budgetDAO != null) budgetDAO.close();
        if (categoryDAO != null) categoryDAO.close();
    }
}
