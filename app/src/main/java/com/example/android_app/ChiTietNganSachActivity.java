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

/**
 * Màn hình Chi tiết Ngân sách.
 *
 * Chức năng:
 * 1. Hiển thị thông tin tổng quan: tên, hạn mức, đã chi, % sử dụng.
 * 2. Hiển thị danh sách tất cả giao dịch chi tiêu thuộc ngân sách này
 *    (lọc theo danh mục + khoảng ngày của ngân sách, đồng thời hiển thị tên ví).
 * 3. Nút Xóa: Hiện AlertDialog xác nhận → Xóa ngân sách (KHÔNG xóa giao dịch).
 * 4. Nút Sửa: Kiểm tra spent_amount > 0 → Chặn + cảnh báo, ngược lại → Mở ThemNganSachActivity ở chế độ Edit.
 */
public class ChiTietNganSachActivity extends AppCompatActivity {

    // ===== CONSTANT =====
    /** Key truyền Budget ID qua Intent */
    public static final String EXTRA_BUDGET_ID = "budget_id";
    /** Request code khi mở màn hình Edit */
    public static final int REQUEST_EDIT_BUDGET = 1001;

    // ===== UI VIEWS =====
    private TextView tvDetailBudgetName, tvDetailBudgetDates, tvDetailBudgetCategories;
    private TextView tvDetailSpent, tvDetailBudgetAmount, tvDetailPercent, tvTransactionCount;
    private ProgressBar pbDetailBudget;
    private RecyclerView rvBudgetTransactions;
    private View layoutEmptyTransactions;

    // ===== DATA =====
    private NganSachDAO budgetDAO;
    private GiaoDichDAO transactionDAO;
    private ViTienDAO walletDAO;

    private NganSach currentBudget; // Ngân sách đang xem
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi"));
    private final NumberFormat numFmt = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chi_tiet_ngan_sach);

        // -- Lấy budget_id từ Intent --
        int budgetId = getIntent().getIntExtra(EXTRA_BUDGET_ID, -1);
        if (budgetId == -1) {
            Toast.makeText(this, "Không tìm thấy ngân sách!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // -- Khởi tạo DAOs --
        budgetDAO = new NganSachDAO(this);
        transactionDAO = new GiaoDichDAO(this);
        walletDAO = new ViTienDAO(this);

        budgetDAO.open();
        transactionDAO.open();
        walletDAO.open();

        // -- Ánh xạ Views --
        initViews();

        // -- Tải dữ liệu ngân sách --
        currentBudget = budgetDAO.getBudgetById(budgetId);
        if (currentBudget == null) {
            Toast.makeText(this, "Ngân sách không tồn tại!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // -- Hiển thị dữ liệu --
        renderBudgetSummary();
        loadTransactions();

        // -- Gắn sự kiện --
        setupClickListeners();
    }

    /**
     * Ánh xạ tất cả View từ XML sang Java.
     */
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

    /**
     * Hiển thị card tổng quan ngân sách lên giao diện.
     */
    private void renderBudgetSummary() {
        tvDetailBudgetName.setText(currentBudget.getName());

        // Khoảng thời gian
        tvDetailBudgetDates.setText(currentBudget.getStartDate() + " → " + currentBudget.getEndDate());

        // Danh mục áp dụng
        tvDetailBudgetCategories.setText("Áp dụng: " + currentBudget.getCategoryIds());

        // Số tiền
        tvDetailSpent.setText(numFmt.format(currentBudget.getSpentAmount()) + " ₫");
        tvDetailBudgetAmount.setText(numFmt.format(currentBudget.getAmount()) + " ₫");

        // Phần trăm và ProgressBar
        double pct = 0;
        if (currentBudget.getAmount() > 0) {
            pct = (currentBudget.getSpentAmount() / currentBudget.getAmount()) * 100.0;
        }
        int progress = (int) Math.min(pct, 100);
        tvDetailPercent.setText(String.format(new Locale("vi"), "%.1f%% đã sử dụng", pct));
        pbDetailBudget.setProgress(progress);

        // Đổi màu ProgressBar theo ngưỡng
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            int color;
            if (pct < 80.0) {
                color = Color.parseColor("#4CAF50"); // Xanh lá
            } else if (pct < 100.0) {
                color = Color.parseColor("#FFC107"); // Vàng
            } else {
                color = Color.parseColor("#F44336"); // Đỏ
            }
            pbDetailBudget.setProgressTintList(ColorStateList.valueOf(color));
        }
    }

    /**
     * Tải danh sách giao dịch thuộc ngân sách này từ DB và gắn vào RecyclerView.
     *
     * Logic lọc:
     * - Lấy TẤT CẢ giao dịch loại "expense" của user hiện tại
     * - Lọc giữ lại những giao dịch có: category nằm trong categoryIds của ngân sách
     *   VÀ ngày giao dịch nằm trong khoảng [startDate, endDate]
     */
    private void loadTransactions() {
        // -- Bước 1: Phân tích danh sách danh mục của ngân sách --
        String categoryIdsRaw = currentBudget.getCategoryIds();
        List<String> budgetCategories = new ArrayList<>();
        if (categoryIdsRaw != null && !categoryIdsRaw.trim().isEmpty()) {
            for (String s : categoryIdsRaw.split(",")) {
                budgetCategories.add(s.trim().toLowerCase());
            }
        }

        // -- Bước 2: Parse khoảng ngày của ngân sách --
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

        // Mở rộng endDate đến cuối ngày để tính đủ ngày kết thúc
        if (endDate != null) {
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(endDate);
            cal.set(java.util.Calendar.HOUR_OF_DAY, 23);
            cal.set(java.util.Calendar.MINUTE, 59);
            cal.set(java.util.Calendar.SECOND, 59);
            endDate = cal.getTime();
        }

        // -- Bước 3: Lấy tất cả giao dịch expense và lọc --
        List<GiaoDich> allExpenses = transactionDAO.getAllTransactions();
        List<GiaoDich> filteredList = new ArrayList<>();

        final Date finalStart = startDate;
        final Date finalEnd = endDate;

        for (GiaoDich t : allExpenses) {
            // Chỉ lấy giao dịch chi tiêu
            if (!"expense".equalsIgnoreCase(t.getLoai())) continue;

            // Kiểm tra danh mục có trong ngân sách không
            String transCategory = t.getCategory() != null ? t.getCategory().trim().toLowerCase() : "";
            if (!budgetCategories.contains(transCategory)) continue;

            // Kiểm tra ngày nằm trong khoảng thời gian ngân sách
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

        // -- Bước 4: Xây dựng map walletId -> walletName để tránh truy vấn lặp --
        Map<Long, String> walletNameMap = new HashMap<>();
        for (ViTien w : walletDAO.getAllWallets()) {
            walletNameMap.put(w.getId(), w.getName());
        }

        // -- Bước 5: Cập nhật giao diện --
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

    /**
     * Gắn sự kiện click cho các nút: Quay lại, Sửa, Xóa.
     */
    private void setupClickListeners() {
        // Nút quay lại
        findViewById(R.id.btnBackBudgetDetail).setOnClickListener(v -> finish());

        // Nút Xóa
        findViewById(R.id.btnDeleteBudget).setOnClickListener(v -> confirmDeleteBudget());

        // Nút Sửa
        findViewById(R.id.btnEditBudget).setOnClickListener(v -> handleEditBudget());
    }

    // =====================================================================
    // CHỨC NĂNG XÓA NGÂN SÁCH
    // =====================================================================

    /**
     * Hiển thị hộp thoại xác nhận xóa ngân sách.
     *
     * QUAN TRỌNG: Xóa ngân sách CHỈ xóa bản ghi trong bảng 'budgets'.
     * Tất cả giao dịch (bảng 'transactions') KHÔNG bị xóa theo.
     */
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

    /**
     * Thực thi xóa ngân sách sau khi người dùng xác nhận.
     */
    private void executeDeletion() {
        int rowsDeleted = budgetDAO.deleteNganSach(currentBudget.getId());
        if (rowsDeleted > 0) {
            Toast.makeText(this, "Đã xóa ngân sách \"" + currentBudget.getName() + "\"", Toast.LENGTH_SHORT).show();
            // Trả kết quả về để NganSachFragment có thể refresh danh sách
            setResult(RESULT_OK);
            finish();
        } else {
            Toast.makeText(this, "Xóa thất bại. Vui lòng thử lại!", Toast.LENGTH_SHORT).show();
        }
    }

    // =====================================================================
    // CHỨC NĂNG SỬA NGÂN SÁCH (với ràng buộc không cho sửa khi đã có giao dịch)
    // =====================================================================

    /**
     * Kiểm tra điều kiện trước khi cho phép sửa ngân sách.
     *
     * Quy tắc nghiệp vụ:
     * - Nếu spentAmount > 0 (đã có ít nhất 1 giao dịch phát sinh):
     *   → Hiển thị dialog cảnh báo và CHẶN không cho mở màn hình sửa.
     * - Nếu spentAmount == 0 (chưa tiêu đồng nào):
     *   → Mở ThemNganSachActivity ở chế độ "Edit" (truyền budget_id qua Intent).
     */
    private void handleEditBudget() {
        if (currentBudget.getSpentAmount() > 0) {
            // Đã có giao dịch → CHẶN SỬA
            showCannotEditDialog();
        } else {
            // Chưa có giao dịch → Mở màn hình sửa
            openEditScreen();
        }
    }

    /**
     * Hiển thị hộp thoại cảnh báo khi ngân sách đã có giao dịch phát sinh.
     */
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

    /**
     * Mở màn hình ThemNganSachActivity ở chế độ SỬA.
     * Truyền budget_id để ThemNganSachActivity biết đây là chỉnh sửa (không phải thêm mới).
     */
    private void openEditScreen() {
        Intent intent = new Intent(this, ThemNganSachActivity.class);
        // Truyền budget_id → ThemNganSachActivity sẽ pre-fill dữ liệu và gọi updateNganSach
        intent.putExtra(ThemNganSachActivity.EXTRA_EDIT_BUDGET_ID, currentBudget.getId());
        startActivityForResult(intent, REQUEST_EDIT_BUDGET);
    }

    /**
     * Nhận kết quả từ ThemNganSachActivity sau khi sửa xong.
     * Reload dữ liệu ngân sách để phản ánh thay đổi mới nhất.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EDIT_BUDGET && resultCode == RESULT_OK) {
            // Reload ngân sách từ DB
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
