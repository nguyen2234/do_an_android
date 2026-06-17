package com.example.android_app;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.android_app.adapter.GiaoDichNganSachAdapter;
import com.example.android_app.database.GiaoDichDAO;
import com.example.android_app.database.NganSachDAO;
import com.example.android_app.database.ViTienDAO;
import com.example.android_app.model.GiaoDich;
import com.example.android_app.model.NganSach;
import com.example.android_app.model.ViTien;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class ChiTietNganSachActivity extends AppCompatActivity {

    
    
    public static final String EXTRA_BUDGET_ID = "budget_id";
    
    public static final int REQUEST_EDIT_BUDGET = 1001;

    
    private TextView tvDetailBudgetName, tvDetailBudgetDates, tvDetailBudgetCategories;
    private TextView tvDetailSpent, tvDetailBudgetAmount, tvDetailPercent, tvTransactionCount;
    private ProgressBar pbDetailBudget;
    private RecyclerView rvBudgetTransactions;
    private View layoutEmptyTransactions;

    
    private NganSachDAO budgetDAO;
    private GiaoDichDAO transactionDAO;
    private ViTienDAO walletDAO;

    private NganSach currentBudget; 
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi"));
    private final NumberFormat numFmt = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_ngan_sach);

        
        int budgetId = getIntent().getIntExtra(EXTRA_BUDGET_ID, -1);
        if (budgetId == -1) {
            Toast.makeText(this, "Không tìm thấy ngân sách!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        
        budgetDAO = new NganSachDAO(this);
        transactionDAO = new GiaoDichDAO(this);
        walletDAO = new ViTienDAO(this);

        budgetDAO.open();
        transactionDAO.open();
        walletDAO.open();

        
        initViews();

        
        currentBudget = budgetDAO.getBudgetById(budgetId);
        if (currentBudget == null) {
            Toast.makeText(this, "Ngân sách không tồn tại!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        
        renderBudgetSummary();
        loadTransactions();

        
        setupClickListeners();
    }

    
    private void initViews() {
        tvDetailBudgetName = findViewById(R.id.tvDetailBudgetName);
        tvDetailBudgetDates = findViewById(R.id.tvDetailBudgetDates);
        tvDetailBudgetCategories = findViewById(R.id.tvDetailBudgetCategories);
        tvDetailSpent = findViewById(R.id.tvDetailSpent);
        tvDetailBudgetAmount = findViewById(R.id.tvDetailBudgetAmount);
        tvDetailPercent = findViewById(R.id.tvDetailPercent);
        pbDetailBudget = findViewById(R.id.pbDetailBudget);
        rvBudgetTransactions = findViewById(R.id.rvBudgetTransactions);
        layoutEmptyTransactions = findViewById(R.id.layoutEmptyTransactions);
        tvTransactionCount = findViewById(R.id.tvTransactionCount);

        rvBudgetTransactions.setLayoutManager(new LinearLayoutManager(this));
    }

    
    private void renderBudgetSummary() {
        tvDetailBudgetName.setText(currentBudget.getName());

        
        tvDetailBudgetDates.setText(currentBudget.getStartDate() + " → " + currentBudget.getEndDate());

        
        tvDetailBudgetCategories.setText("Áp dụng: " + currentBudget.getCategoryIds());

        
        tvDetailSpent.setText(numFmt.format(currentBudget.getSpentAmount()) + " ₫");
        tvDetailBudgetAmount.setText(numFmt.format(currentBudget.getAmount()) + " ₫");

        
        double pct = 0;
        if (currentBudget.getAmount() > 0) {
            pct = (currentBudget.getSpentAmount() / currentBudget.getAmount()) * 100.0;
        }
        int progress = (int) Math.min(pct, 100);
        tvDetailPercent.setText(String.format(new Locale("vi"), "%.1f%% đã sử dụng", pct));
        pbDetailBudget.setProgress(progress);

        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int color;
            if (pct < 80.0) {
                color = Color.parseColor("#4CAF50"); 
            } else if (pct < 100.0) {
                color = Color.parseColor("#FFC107"); 
            } else {
                color = Color.parseColor("#F44336"); 
            }
            pbDetailBudget.setProgressTintList(ColorStateList.valueOf(color));
        }
    }

    
    private void loadTransactions() {
        
        String categoryIdsRaw = currentBudget.getCategoryIds();
        List<String> budgetCategories = new ArrayList<>();
        if (categoryIdsRaw != null && !categoryIdsRaw.trim().isEmpty()) {
            for (String s : categoryIdsRaw.split(",")) {
                budgetCategories.add(s.trim().toLowerCase());
            }
        }

        
        Date startDate = null, endDate = null;
        try {
            if (currentBudget.getStartDate() != null) {
                startDate = sdf.parse(currentBudget.getStartDate());
            }
            if (currentBudget.getEndDate() != null) {
                endDate = sdf.parse(currentBudget.getEndDate());
            }
        } catch (ParseException e) {
            android.util.Log.e("BudgetDetail", "Lỗi parse ngày: " + e.getMessage());
        }

        
        if (endDate != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(endDate);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
            cal.set(java.util.Calendar.MINUTE, 59);
            cal.set(java.util.Calendar.SECOND, 59);
            endDate = cal.getTime();
        }

        
        List<GiaoDich> allExpenses = transactionDAO.getAllTransactions();
        List<GiaoDich> filteredList = new ArrayList<>();

        final Date finalStart = startDate;
        final Date finalEnd = endDate;

        for (GiaoDich t : allExpenses) {
            
            if (!"expense".equalsIgnoreCase(t.getLoai())) continue;

            
            String transCategory = t.getCategory() != null ? t.getCategory().trim().toLowerCase() : "";
            if (!budgetCategories.contains(transCategory)) continue;

            
            try {
                if (t.getNgay() != null && !t.getNgay().trim().isEmpty()) {
                    Date transDate = sdf.parse(t.getNgay().trim());
                    if (transDate != null) {
                        boolean afterStart = (finalStart == null) || !transDate.before(finalStart);
                        boolean beforeEnd = (finalEnd == null) || !transDate.after(finalEnd);
                        if (afterStart && beforeEnd) {
                            filteredList.add(t);
                        }
                    }
                }
            } catch (ParseException e) {
                android.util.Log.w("BudgetDetail", "Bỏ qua giao dịch ID " + t.getId() + " do lỗi ngày");
            }
        }

        
        Map<Long, String> walletNameMap = new HashMap<>();
        for (ViTien w : walletDAO.getAllWallets()) {
            walletNameMap.put(w.getId(), w.getName());
        }

        
        tvTransactionCount.setText(filteredList.size() + " giao dịch");

        if (filteredList.isEmpty()) {
            layoutEmptyTransactions.setVisibility(View.VISIBLE);
            rvBudgetTransactions.setVisibility(View.GONE);
        } else {
            layoutEmptyTransactions.setVisibility(View.GONE);
            rvBudgetTransactions.setVisibility(View.VISIBLE);
            GiaoDichNganSachAdapter adapter = new GiaoDichNganSachAdapter(this, filteredList, walletNameMap);
            rvBudgetTransactions.setAdapter(adapter);
        }
    }

    
    private void setupClickListeners() {
        
        findViewById(R.id.btnBackBudgetDetail).setOnClickListener(v -> finish());

        
        findViewById(R.id.btnDeleteBudget).setOnClickListener(v -> confirmDeleteBudget());

        
        findViewById(R.id.btnEditBudget).setOnClickListener(v -> handleEditBudget());
    }

    
    
    

    
    private void confirmDeleteBudget() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa ngân sách")
                .setMessage("Bạn có chắc chắn muốn xóa ngân sách\n\"" + currentBudget.getName() + "\" không?\n\n"
                        + "Lưu ý: Các giao dịch đã chi tiêu sẽ không bị xóa theo.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Xóa", (dialog, which) -> executeDeletion())
                .setNegativeButton("Hủy", null)
                .show();
    }

    
    private void executeDeletion() {
        int rowsDeleted = budgetDAO.deleteNganSach(currentBudget.getId());
        if (rowsDeleted > 0) {
            Toast.makeText(this, "Đã xóa ngân sách \"" + currentBudget.getName() + "\"", Toast.LENGTH_SHORT).show();
            
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Xóa thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }

    
    
    

    
    private void handleEditBudget() {
        if (currentBudget.getSpentAmount() > 0) {
            
            showCannotEditDialog();
        } else {
            
            openEditScreen();
        }
    }

    
    private void showCannotEditDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Không thể chỉnh sửa")
                .setMessage("Không thể chỉnh sửa ngân sách đã có phát sinh giao dịch.\n\n"
                        + "Ngân sách này đã chi: "
                        + numFmt.format(currentBudget.getSpentAmount()) + " ₫\n\n"
                        + "Nếu cần thay đổi, hãy xóa ngân sách và tạo ngân sách mới.")
                .setPositiveButton("Đã hiểu", null)
                .show();
    }

    
    private void openEditScreen() {
        Intent intent = new Intent(this, ThemNganSachActivity.class);
        
        intent.putExtra(ThemNganSachActivity.EXTRA_EDIT_BUDGET_ID, currentBudget.getId());
        startActivityForResult(intent, REQUEST_EDIT_BUDGET);
    }

    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_BUDGET && resultCode == RESULT_OK) {
            
            currentBudget = budgetDAO.getBudgetById(currentBudget.getId());
            if (currentBudget != null) {
                renderBudgetSummary();
                loadTransactions();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (budgetDAO != null) budgetDAO.close();
        if (transactionDAO != null) transactionDAO.close();
        if (walletDAO != null) walletDAO.close();
    }
}
