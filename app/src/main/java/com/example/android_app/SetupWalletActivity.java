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


public class SetupWalletActivity extends AppCompatActivity {

    
    private static final String PREF_KEY = "UserPrefs";
    private static final String PREF_ONBOARDING_DONE = "isOnboardingDone";

    
    private TextInputEditText etWalletName, etWalletBalance;
    private MaterialButton btnAddWallet, btnFinish;
    private RecyclerView rvWallets;
    private TextView tvWalletCount;

    
    private ViTienDAO walletDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup_wallet);

        walletDAO = new ViTienDAO(this);
        walletDAO.open();

        initViews();
        setupClickListeners();
        refreshList(); 
    }

    
    private void initViews() {
        etWalletName = findViewById(R.id.etWalletNameSetup);
        etWalletBalance = findViewById(R.id.etWalletBalanceSetup);
        btnAddWallet = findViewById(R.id.btnAddWalletSetup);
        btnFinish = findViewById(R.id.btnFinishSetupWallet);
        rvWallets = findViewById(R.id.rvWalletsSetup);
        tvWalletCount = findViewById(R.id.tvWalletCountSetup);

        rvWallets.setLayoutManager(new LinearLayoutManager(this));
    }

    
    private void setupClickListeners() {
        
        btnAddWallet.setOnClickListener(v -> addWallet());

        
        btnFinish.setOnClickListener(v -> finishOnboarding());
    }

    
    private void addWallet() {
        String name = etWalletName.getText() != null
                ? etWalletName.getText().toString().trim() : "";

        if (name.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập tên ví", Toast.LENGTH_SHORT).show();
            return;
        }

        
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

        
        ViTien newWallet = new ViTien();
        newWallet.setName(name);
        newWallet.setBalance(balance);
        newWallet.setLoai("cash");
        newWallet.setCurrency("VND");
        newWallet.setIcon("cash");
        newWallet.setColor("#4CAF50"); 

        long id = walletDAO.addWallet(newWallet);

        if (id > 0) {
            Toast.makeText(this, "✅ Đã tạo ví \"" + name + "\"", Toast.LENGTH_SHORT).show();
            etWalletName.setText("");
            etWalletBalance.setText("");
            refreshList(); 
        } else {
            Toast.makeText(this, "Lỗi khi tạo ví", Toast.LENGTH_SHORT).show();
        }
    }

    
    private void refreshList() {
        List<ViTien> list = walletDAO.getAllWallets();
        int count = list.size();

        
        tvWalletCount.setText(count + " ví");

        
        ViTienAdapter adapter = new ViTienAdapter(this, list);
        rvWallets.setAdapter(adapter);

        
        if (count >= 1) {
            
            btnFinish.setEnabled(true);
            btnFinish.setAlpha(1.0f);
        } else {
            
            btnFinish.setEnabled(false);
            btnFinish.setAlpha(0.5f);
        }
    }

    
    private void finishOnboarding() {
        
        SharedPreferences prefs = getSharedPreferences(PREF_KEY, MODE_PRIVATE);
        prefs.edit().putBoolean(PREF_ONBOARDING_DONE, true).apply();

        
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (walletDAO != null) walletDAO.close();
    }
}
