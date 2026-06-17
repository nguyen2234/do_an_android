package com.example.android_app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_app.database.GiaoDichDAO;
import com.example.android_app.database.ViTienDAO;
import com.example.android_app.model.GiaoDich;
import com.example.android_app.model.ViTien;
import com.example.android_app.utils.NotificationHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ChuyenTienActivity extends AppCompatActivity {

    private TextView tabInternal, tabBank;
    private LinearLayout containerInternal, containerBank;
    private ImageView btnBackTransfer;

    
    private Spinner spinnerSenderWallet, spinnerReceiverWallet;
    private EditText etInternalAmount, etInternalNote;
    private Button btnConfirmInternal;

    
    private Spinner spinnerBankSourceWallet, spinnerBeneficiaryBank;
    private EditText etBeneficiaryAccount, etBeneficiaryName, etBankAmount, etBankNote;
    private Button btnConfirmBank;

    private ViTienDAO walletDAO;
    private GiaoDichDAO transactionDAO;
    private List<ViTien> walletList;

    private String[] bankList = {
            "Vietcombank (VCB)",
            "Techcombank (TCB)",
            "BIDV",
            "Agribank",
            "VietinBank",
            "MB Bank (MBB)",
            "VPBank",
            "ACB",
            "TPBank",
            "Sacombank"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chuyen_tien);

        
        walletDAO = new ViTienDAO(this);
        transactionDAO = new GiaoDichDAO(this);
        walletDAO.open();
        transactionDAO.open();

        
        tabInternal = findViewById(R.id.tabInternal);
        tabBank = findViewById(R.id.tabBank);
        containerInternal = findViewById(R.id.containerInternal);
        containerBank = findViewById(R.id.containerBank);
        btnBackTransfer = findViewById(R.id.btnBackTransfer);

        
        spinnerSenderWallet = findViewById(R.id.spinnerSenderWallet);
        spinnerReceiverWallet = findViewById(R.id.spinnerReceiverWallet);
        etInternalAmount = findViewById(R.id.etInternalAmount);
        etInternalNote = findViewById(R.id.etInternalNote);
        btnConfirmInternal = findViewById(R.id.btnConfirmInternal);

        
        spinnerBankSourceWallet = findViewById(R.id.spinnerBankSourceWallet);
        spinnerBeneficiaryBank = findViewById(R.id.spinnerBeneficiaryBank);
        etBeneficiaryAccount = findViewById(R.id.etBeneficiaryAccount);
        etBeneficiaryName = findViewById(R.id.etBeneficiaryName);
        etBankAmount = findViewById(R.id.etBankAmount);
        etBankNote = findViewById(R.id.etBankNote);
        btnConfirmBank = findViewById(R.id.btnConfirmBank);

        
        tabInternal.setOnClickListener(v -> selectTab(true));
        tabBank.setOnClickListener(v -> selectTab(false));
        btnBackTransfer.setOnClickListener(v -> finish());

        
        btnConfirmInternal.setOnClickListener(v -> executeInternalTransfer());
        btnConfirmBank.setOnClickListener(v -> executeBankTransfer());

        
        setupWallets();
        setupBankSpinner();
    }

    private void selectTab(boolean isInternal) {
        if (isInternal) {
            tabInternal.setBackgroundResource(R.drawable.bg_card_primary);
            tabInternal.setTextColor(Color.WHITE);
            tabBank.setBackgroundResource(android.R.color.transparent);
            tabBank.setTextColor(Color.parseColor("#6B7280"));

            containerInternal.setVisibility(View.VISIBLE);
            containerBank.setVisibility(View.GONE);
        } else {
            tabBank.setBackgroundResource(R.drawable.bg_card_primary);
            tabBank.setTextColor(Color.WHITE);
            tabInternal.setBackgroundResource(android.R.color.transparent);
            tabInternal.setTextColor(Color.parseColor("#6B7280"));

            containerBank.setVisibility(View.VISIBLE);
            containerInternal.setVisibility(View.GONE);
        }
    }

    private void setupWallets() {
        walletList = walletDAO.getAllWallets();
        if (walletList.isEmpty()) {
            walletDAO.addWallet(new ViTien(0, "Tiền mặt", 0, "cash", "VND"));
            walletList = walletDAO.getAllWallets();
        }

        List<String> walletNames = new ArrayList<>();
        for (ViTien w : walletList) {
            walletNames.add(w.getName() + " (" + String.format(Locale.GERMANY, "%,.0f", w.getBalance()) + " ₫)");
        }

        ArrayAdapter<String> walletAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, walletNames);
        walletAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerSenderWallet.setAdapter(walletAdapter);
        spinnerReceiverWallet.setAdapter(walletAdapter);
        spinnerBankSourceWallet.setAdapter(walletAdapter);

        
        if (walletAdapter.getCount() > 1) {
            spinnerReceiverWallet.setSelection(1);
        }
    }

    private void setupBankSpinner() {
        ArrayAdapter<String> bankAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, bankList);
        bankAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBeneficiaryBank.setAdapter(bankAdapter);
    }

    private void executeInternalTransfer() {
        int senderIndex = spinnerSenderWallet.getSelectedItemPosition();
        int receiverIndex = spinnerReceiverWallet.getSelectedItemPosition();

        if (senderIndex == receiverIndex) {
            Toast.makeText(this, "Ví gửi và ví nhận phải khác nhau!", Toast.LENGTH_SHORT).show();
            return;
        }

        String amountStr = etInternalAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền cần chuyển", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        if (amount <= 0) {
            Toast.makeText(this, "Số tiền chuyển phải lớn hơn 0", Toast.LENGTH_SHORT).show();
            return;
        }

        ViTien senderWallet = walletList.get(senderIndex);
        ViTien receiverWallet = walletList.get(receiverIndex);

        if (senderWallet.getBalance() < amount) {
            Toast.makeText(this, "Số dư ví gửi không đủ để thực hiện giao dịch!", Toast.LENGTH_SHORT).show();
            return;
        }

        
        double newSenderBalance = senderWallet.getBalance() - amount;
        double newReceiverBalance = receiverWallet.getBalance() + amount;

        
        int updateSender = walletDAO.updateBalance(senderWallet.getId(), newSenderBalance);
        int updateReceiver = walletDAO.updateBalance(receiverWallet.getId(), newReceiverBalance);

        if (updateSender > 0 && updateReceiver > 0) {
            
            String dateStr = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi")).format(Calendar.getInstance().getTime());
            String note = etInternalNote.getText().toString().trim();
            if (note.isEmpty()) {
                note = "Chuyển tiền nội bộ sang ví " + receiverWallet.getName();
            }

            GiaoDich expenseTrans = new GiaoDich(0, "Chuyển tiền nội bộ", amount, "Chuyển tiền", "expense", dateStr, note, senderWallet.getId());
            transactionDAO.addTransaction(expenseTrans);

            
            GiaoDich incomeTrans = new GiaoDich(0, "Nhận tiền nội bộ", amount, "Chuyển tiền", "income", dateStr, "Nhận từ ví " + senderWallet.getName() + ": " + note, receiverWallet.getId());
            transactionDAO.addTransaction(incomeTrans);

            
            NotificationHelper.showTransferNotification(this, senderWallet.getName(), receiverWallet.getName(), amount);
            if (newSenderBalance < senderWallet.getMinBalance() && senderWallet.getMinBalance() > 0) {
                NotificationHelper.showLowBalanceNotification(this, senderWallet.getName(), newSenderBalance);
            }

            Toast.makeText(this, "Chuyển tiền nội bộ thành công!", Toast.LENGTH_SHORT).show();

            
            Intent intent = new Intent(this, KetQuaGiaoDichActivity.class);
            intent.putExtra("isSuccess", true);
            intent.putExtra("amount", amount);
            intent.putExtra("type", "expense");
            intent.putExtra("category", "Chuyển nội bộ");
            intent.putExtra("walletName", senderWallet.getName() + " ➔ " + receiverWallet.getName());
            intent.putExtra("note", note);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Giao dịch lỗi. Vui lòng thử lại sau!", Toast.LENGTH_SHORT).show();
        }
    }

    private void executeBankTransfer() {
        int sourceIndex = spinnerBankSourceWallet.getSelectedItemPosition();
        String bank = spinnerBeneficiaryBank.getSelectedItem().toString();
        String accountNo = etBeneficiaryAccount.getText().toString().trim();
        String beneficiaryName = etBeneficiaryName.getText().toString().trim().toUpperCase();
        String amountStr = etBankAmount.getText().toString().trim();
        String note = etBankNote.getText().toString().trim();

        if (accountNo.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tài khoản thụ hưởng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (beneficiaryName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên người thụ hưởng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền cần chuyển", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        if (amount <= 0) {
            Toast.makeText(this, "Số tiền chuyển phải lớn hơn 0", Toast.LENGTH_SHORT).show();
            return;
        }

        ViTien sourceWallet = walletList.get(sourceIndex);

        if (sourceWallet.getBalance() < amount) {
            Toast.makeText(this, "Số dư nguồn tiền không đủ!", Toast.LENGTH_SHORT).show();
            return;
        }

        
        double newBalance = sourceWallet.getBalance() - amount;
        int updateResult = walletDAO.updateBalance(sourceWallet.getId(), newBalance);

        if (updateResult > 0) {
            String dateStr = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi")).format(Calendar.getInstance().getTime());
            if (note.isEmpty()) {
                note = "Chuyển tiền nhanh 247";
            }
            String fullNote = "Nguồn: " + sourceWallet.getName() + " | TK Nhận: " + accountNo + " - " + beneficiaryName + " | Lời nhắn: " + note;

            
            GiaoDich bankTrans = new GiaoDich(0, "CK Ngân hàng (" + bank + ")", amount, "Chuyển tiền", "expense", dateStr, fullNote, sourceWallet.getId());
            transactionDAO.addTransaction(bankTrans);

            
            NotificationHelper.showTransferNotification(this, sourceWallet.getName(), beneficiaryName + " (" + bank + ")", amount);
            if (newBalance < sourceWallet.getMinBalance() && sourceWallet.getMinBalance() > 0) {
                NotificationHelper.showLowBalanceNotification(this, sourceWallet.getName(), newBalance);
            }

            Toast.makeText(this, "Chuyển khoản liên ngân hàng thành công!", Toast.LENGTH_SHORT).show();

            
            Intent intent = new Intent(this, KetQuaGiaoDichActivity.class);
            intent.putExtra("isSuccess", true);
            intent.putExtra("amount", amount);
            intent.putExtra("type", "expense");
            intent.putExtra("category", "CK Ngân hàng");
            intent.putExtra("walletName", sourceWallet.getName() + " ➔ " + bank);
            intent.putExtra("note", "Thụ hưởng: " + beneficiaryName + " (" + accountNo + ")");
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Giao dịch lỗi. Vui lòng thử lại sau!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (walletDAO != null) {
            walletDAO.close();
        }
        if (transactionDAO != null) {
            transactionDAO.close();
        }
    }
}
