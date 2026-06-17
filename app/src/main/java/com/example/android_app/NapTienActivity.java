package com.example.android_app;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.android_app.database.GiaoDichDAO;
import com.example.android_app.database.ViTienDAO;
import com.example.android_app.model.GiaoDich;
import com.example.android_app.model.ViTien;
import com.example.android_app.utils.NotificationHelper;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public class NapTienActivity extends AppCompatActivity {

    private EditText etSoTien, etGhiChu;
    private TextView tvAmountDisplay;
    private Spinner spinnerWallet;
    private List<ViTien> walletList;
    private ViTienDAO walletDAO;
    private GiaoDichDAO giaoDichDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nap_tien);

        walletDAO = new ViTienDAO(this);
        walletDAO.open();
        giaoDichDAO = new GiaoDichDAO(this);
        giaoDichDAO.open();

        
        etSoTien = findViewById(R.id.etSoTienNap);
        etGhiChu = findViewById(R.id.etGhiChuNap);
        tvAmountDisplay = findViewById(R.id.tvAmountDisplayNap);
        spinnerWallet = findViewById(R.id.spinnerWalletNap);
        MaterialButton btnXacNhan = findViewById(R.id.btnXacNhanNap);

        
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        
        setupWalletSpinner();

        
        etSoTien.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                String val = s.toString().trim();
                if (!val.isEmpty()) {
                    try {
                        double amount = Double.parseDouble(val);
                        tvAmountDisplay.setText(formatCurrency(amount));
                    } catch (NumberFormatException ignored) {}
                } else {
                    tvAmountDisplay.setText("0 ₫");
                }
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        btnXacNhan.setOnClickListener(v -> napTien());
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

    private void napTien() {
        String soTienStr = etSoTien.getText().toString().trim();
        if (soTienStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }
        double soTien;
        try {
            soTien = Double.parseDouble(soTienStr);
            if (soTien <= 0) {
                Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        
        int idx = spinnerWallet.getSelectedItemPosition();
        ViTien selectedWallet = walletList.get(idx);

        
        double newBalance = selectedWallet.getBalance() + soTien;
        walletDAO.updateBalance(selectedWallet.getId(), newBalance);

        
        
        String date = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi")).format(Calendar.getInstance().getTime());
        String note = etGhiChu.getText().toString().trim();
        GiaoDich giaoDich = new GiaoDich(
                0, "Nạp tiền", soTien, "Nạp tiền", "income", date,
                note.isEmpty() ? "Nạp tiền vào ví " + selectedWallet.getName() : note,
                selectedWallet.getId()
        );
        giaoDichDAO.addTransaction(giaoDich);

        
        NotificationHelper.showTopUpNotification(this, selectedWallet.getName(), soTien);

        Toast.makeText(this, "✅ Nạp " + formatCurrency(soTien) + " thành công!", Toast.LENGTH_SHORT).show();
        finish();
    }

    private String formatCurrency(double amount) {
        return String.format("%,.0f", amount).replace(",", ".") + " ₫";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (walletDAO != null) walletDAO.close();
        if (giaoDichDAO != null) giaoDichDAO.close();
    }
}
