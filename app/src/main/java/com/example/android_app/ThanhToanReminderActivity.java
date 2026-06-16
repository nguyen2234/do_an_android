package com.example.android_app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_app.database.GiaoDichDAO;
import com.example.android_app.database.NganSachDAO;
import com.example.android_app.database.ViTienDAO;
import com.example.android_app.database.NguoiDungDAO;
import com.example.android_app.model.GiaoDich;
import com.example.android_app.model.NganSach;
import com.example.android_app.model.NguoiDung;
import com.example.android_app.model.ViTien;
import com.example.android_app.utils.NotificationHelper;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Màn hình Xác nhận thanh toán nhắc hẹn.
 * Cho phép chọn ví, xác minh số dư, xác thực mã PIN và lưu giao dịch.
 */
public class ThanhToanReminderActivity extends AppCompatActivity {

    private TextView tvReminderTitle, tvAmountDisplay, tvCategoryDisplay, tvNotesDisplay;
    private Spinner spinnerWallet;
    private List<ViTien> walletList;

    private ViTienDAO walletDAO;
    private GiaoDichDAO giaoDichDAO;
    private NganSachDAO budgetDAO;

    private double amount = 0;
    private String category = "";
    private String title = "";
    private long reminderId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thanh_toan_reminder);

        walletDAO = new ViTienDAO(this);
        walletDAO.open();
        giaoDichDAO = new GiaoDichDAO(this);
        giaoDichDAO.open();
        budgetDAO = new NganSachDAO(this);
        budgetDAO.open();

        tvReminderTitle = findViewById(R.id.tvReminderTitle);
        tvAmountDisplay = findViewById(R.id.tvAmountDisplay);
        tvCategoryDisplay = findViewById(R.id.tvCategoryDisplay);
        tvNotesDisplay = findViewById(R.id.tvNotesDisplay);
        spinnerWallet = findViewById(R.id.spinnerWallet);
        MaterialButton btnXacNhanThanhToan = findViewById(R.id.btnXacNhanThanhToan);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        // Đọc dữ liệu từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            amount = intent.getDoubleExtra("prefilled_amount", 0);
            category = intent.getStringExtra("prefilled_category");
            title = intent.getStringExtra("prefilled_title");
            reminderId = intent.getLongExtra("reminder_id", -1);
        }

        // Điền dữ liệu vào giao diện (chỉ hiển thị, không cho phép sửa đổi)
        tvReminderTitle.setText(title != null ? title : "Chưa rõ");
        tvAmountDisplay.setText(formatCurrency(amount));
        tvCategoryDisplay.setText(category != null ? category : "Chi tiêu");
        tvNotesDisplay.setText("Thanh toán tiền " + (title != null ? title : ""));

        setupWalletSpinner();

        btnXacNhanThanhToan.setOnClickListener(v -> savePaymentTransaction());
    }

    private void setupWalletSpinner() {
        walletList = walletDAO.getAllWallets();
        if (walletList.isEmpty()) {
            Toast.makeText(this, "Chưa có ví nào. Vui lòng tạo ví trước.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        List<String> names = new ArrayList<>();
        for (ViTien w : walletList) {
            names.add(w.getName() + " (" + formatCurrency(w.getBalance()) + ")");
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.item_spinner, names);
        adapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerWallet.setAdapter(adapter);
    }

    private void savePaymentTransaction() {
        if (walletList.isEmpty()) return;

        int idx = spinnerWallet.getSelectedItemPosition();
        final ViTien selectedWallet = walletList.get(idx);

        // Chặn chi tiêu nếu không đủ số dư ví
        ViTien currentWallet = walletDAO.getWalletById(selectedWallet.getId());
        double currentBalance = (currentWallet != null) ? currentWallet.getBalance() : 0;
        if (amount > currentBalance) {
            new AlertDialog.Builder(this)
                    .setTitle("Số dư không đủ")
                    .setMessage("Số dư ví không đủ. Vui lòng nạp thêm tiền vào ví này để tiếp tục!")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        // Show PIN verification dialog
        showPinVerificationDialog(selectedWallet);
    }

    private void showPinVerificationDialog(final ViTien selectedWallet) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        long userId = prefs.getLong("user_id", -1);
        if (userId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy người dùng hiện tại", Toast.LENGTH_SHORT).show();
            return;
        }

        NguoiDungDAO uDAO = new NguoiDungDAO(this);
        uDAO.open();
        NguoiDung user = uDAO.getUserById(userId);
        uDAO.close();

        final String correctPin = (user != null) ? user.getTransactionPin() : "";

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_verify_pin, null);
        final EditText etVerifyPin = dialogView.findViewById(R.id.etVerifyPin);
        android.widget.Button btnCancelVerify = dialogView.findViewById(R.id.btnCancelVerify);
        android.widget.Button btnConfirmVerify = dialogView.findViewById(R.id.btnConfirmVerify);

        final androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        btnCancelVerify.setOnClickListener(v -> dialog.dismiss());

        btnConfirmVerify.setOnClickListener(v -> {
            String enteredPin = etVerifyPin.getText().toString().trim();
            if (enteredPin.isEmpty()) {
                Toast.makeText(ThanhToanReminderActivity.this, "Vui lòng nhập mã PIN", Toast.LENGTH_SHORT).show();
                return;
            }

            if (enteredPin.equals(correctPin)) {
                dialog.dismiss();
                executeSaveTransaction(selectedWallet);
            } else {
                Toast.makeText(ThanhToanReminderActivity.this, "Mã PIN giao dịch không chính xác!", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void executeSaveTransaction(ViTien selectedWallet) {
        String date = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi")).format(Calendar.getInstance().getTime());
        String finalNote = tvNotesDisplay.getText().toString().trim();

        // Tạo đối tượng GiaoDich
        GiaoDich transaction = new GiaoDich(0, category, amount, category, "expense", date, finalNote, selectedWallet.getId());
        
        long id = giaoDichDAO.addTransaction(transaction);
        if (id > 0) {
            // Cập nhật số dư ví
            double newBalance = selectedWallet.getBalance() - amount;
            walletDAO.updateBalance(selectedWallet.getId(), newBalance);

            // Gửi thông báo tương ứng với giao dịch chi tiêu
            NotificationHelper.showExpenseNotification(this, selectedWallet.getName(), amount, category);
            capNhatNganSach(date, category, amount);

            ViTien updatedWallet = walletDAO.getWalletById(selectedWallet.getId());
            if (updatedWallet != null && updatedWallet.getMinBalance() > 0
                    && newBalance < updatedWallet.getMinBalance()) {
                NotificationHelper.showLowBalanceNotification(
                        this, selectedWallet.getName(), newBalance);
            }

            // Đánh dấu nhắc hẹn đã hoàn tất thanh toán
            if (reminderId > 0) {
                com.example.android_app.database.ReminderDAO rDAO = new com.example.android_app.database.ReminderDAO(this);
                rDAO.open();
                rDAO.markAsPaid(reminderId);
                rDAO.close();
                Toast.makeText(this, "✅ Đã thanh toán nhắc hẹn thành công!", Toast.LENGTH_SHORT).show();
            }

            // Chuyển sang màn hình thông báo thành công
            Intent intent = new Intent(this, KetQuaGiaoDichActivity.class);
            intent.putExtra("isSuccess", true);
            intent.putExtra("amount", amount);
            intent.putExtra("type", "expense");
            startActivity(intent);
            finish();
        } else {
            // Chuyển sang màn hình thông báo thất bại
            Intent intent = new Intent(this, KetQuaGiaoDichActivity.class);
            intent.putExtra("isSuccess", false);
            intent.putExtra("amount", amount);
            intent.putExtra("type", "expense");
            startActivity(intent);
            finish();
        }
    }

    private void capNhatNganSach(String transDateStr, String cat, double amt) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi"));
            Date transDate = sdf.parse(transDateStr);

            List<NganSach> list = budgetDAO.getAllBudgets();
            for (NganSach b : list) {
                Date start = sdf.parse(b.getStartDate());
                Date end = sdf.parse(b.getEndDate());

                if (!transDate.before(start) && !transDate.after(end)) {
                    if (b.getCategoryIds() != null && b.getCategoryIds().contains(cat)) {
                        budgetDAO.updateSpentAmount(b.getId(), b.getSpentAmount() + amt);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String formatCurrency(double amt) {
        return String.format("%,.0f", amt).replace(",", ".") + " ₫";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (walletDAO != null) walletDAO.close();
        if (giaoDichDAO != null) giaoDichDAO.close();
        if (budgetDAO != null) budgetDAO.close();
    }
}
