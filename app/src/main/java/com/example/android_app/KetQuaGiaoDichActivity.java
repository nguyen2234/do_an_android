package com.example.android_app;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.NumberFormat;
import java.util.Locale;

public class KetQuaGiaoDichActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ket_qua_giao_dich);

        ImageView ivIcon = findViewById(R.id.ivResultIcon);
        TextView tvMessage = findViewById(R.id.tvResultMessage);
        TextView tvAmount = findViewById(R.id.tvResultAmount);
        TextView tvType = findViewById(R.id.tvResultType);
        TextView tvCategory = findViewById(R.id.tvResultCategory);
        TextView tvWallet = findViewById(R.id.tvResultWallet);
        TextView tvNote = findViewById(R.id.tvResultNote);
        Button btnBack = findViewById(R.id.btnBackToHome);

        Intent intent = getIntent();
        boolean isSuccess = intent.getBooleanExtra("isSuccess", true);
        double amount = intent.getDoubleExtra("amount", 0);
        String type = intent.getStringExtra("type");
        String category = intent.getStringExtra("category");
        String walletName = intent.getStringExtra("walletName");
        String note = intent.getStringExtra("note");

        NumberFormat fmt = NumberFormat.getInstance(new Locale("vi", "VN"));
        String amountStr = fmt.format(amount) + " ₫";

        // Display transaction details
        tvType.setText("income".equals(type) ? "Khoản thu" : "Khoản chi");
        tvCategory.setText(category != null ? category : "");
        tvWallet.setText(walletName != null ? walletName : "");
        tvNote.setText((note != null && !note.isEmpty()) ? note : "Không có");

        if (isSuccess) {
            // Icon Success (Có thể dùng ảnh tích xanh tự tạo, ở đây dùng mặc định)
            ivIcon.setImageResource(android.R.drawable.ic_dialog_info);
            ivIcon.setColorFilter(Color.parseColor("#4CAF50")); // Màu xanh lá
            tvMessage.setText("Giao dịch thành công!");
            
            if ("income".equals(type)) {
                tvAmount.setText("+ " + amountStr);
                tvAmount.setTextColor(Color.parseColor("#2ECC71"));
            } else {
                tvAmount.setText("- " + amountStr);
                tvAmount.setTextColor(Color.parseColor("#E74C3C"));
            }
        } else {
            // Icon Fail
            ivIcon.setImageResource(android.R.drawable.ic_delete);
            ivIcon.setColorFilter(Color.parseColor("#F44336")); // Màu đỏ
            tvMessage.setText("Giao dịch thất bại");
            tvAmount.setText(amountStr);
            tvAmount.setTextColor(Color.parseColor("#F44336"));
        }

        btnBack.setOnClickListener(v -> {
            finish(); // Quay lại trang trước (Fragment Home)
        });
    }
}
