package com.example.android_app.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android_app.R;
import com.example.android_app.adapter.ViTienAdapter;
import com.example.android_app.database.GiaoDichDAO;
import com.example.android_app.database.ViTienDAO;
import com.example.android_app.model.GiaoDich;
import com.example.android_app.model.ViTien;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ViTienFragment extends Fragment {

    private RecyclerView rvWallets;
    private TextView tvTotalBalance, tvTongThuNhap, tvTongChiTieu;
    private ViTienDAO walletDAO;
    private GiaoDichDAO transactionDAO;
    private ViTienAdapter adapter;
    private List<ViTien> wallets;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vi_tien, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ View với ID chính xác từ fragment_vi_tien.xml
        tvTotalBalance = view.findViewById(R.id.tvTotalBalance);
        tvTongThuNhap = view.findViewById(R.id.tvCardIncome);
        tvTongChiTieu = view.findViewById(R.id.tvCardExpense);
        rvWallets = view.findViewById(R.id.rvWallets);

        rvWallets.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWallets.setNestedScrollingEnabled(false);

        walletDAO = new ViTienDAO(getContext());
        transactionDAO = new GiaoDichDAO(getContext());
        walletDAO.open();
        transactionDAO.open();

        refreshData();

        view.findViewById(R.id.btnAddWallet).setOnClickListener(v -> showWalletDialog(null));
    }

    private void refreshData() {
        loadWallets();
        updateSummary();
    }

    private void loadWallets() {
        wallets = walletDAO.getAllWallets();
        if (wallets.isEmpty()) {
            walletDAO.addWallet(new ViTien(0, "Tiền mặt", 0, "cash", "VND", "cash", "#4CAF50", 0));
            wallets = walletDAO.getAllWallets();
        }

        adapter = new ViTienAdapter(getContext(), wallets);
        adapter.setOnEditClickListener(this::showWalletDialog);
        adapter.setOnDeleteClickListener(this::showDeleteConfirmDialog);
        rvWallets.setAdapter(adapter);
    }

    private void updateSummary() {
        List<GiaoDich> transactions = transactionDAO.getAllTransactions();
        double income = 0;
        double expense = 0;

        for (GiaoDich t : transactions) {
            if ("income".equalsIgnoreCase(t.getLoai())) {
                income += t.getSoTien();
            } else {
                expense += t.getSoTien();
            }
        }

        // Cập nhật text Thu nhập / Chi tiêu
        if (tvTongThuNhap != null) tvTongThuNhap.setText("+ " + dinhDangTien(income) + " ₫");
        if (tvTongChiTieu != null) tvTongChiTieu.setText("- " + dinhDangTien(expense) + " ₫");
        
        // QUAN TRỌNG: Tổng số dư = Tổng Thu nhập - Tổng Chi tiêu
        double actualTotalBalance = income - expense;
        tvTotalBalance.setText(dinhDangTien(actualTotalBalance) + " ₫");
    }

    private void showWalletDialog(@Nullable ViTien walletToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_vi_tien, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etName = dialogView.findViewById(R.id.etWalletName);
        EditText etBalance = dialogView.findViewById(R.id.etWalletBalance);
        EditText etMinBalance = dialogView.findViewById(R.id.etWalletMinBalance);
        Spinner spinnerIcon = dialogView.findViewById(R.id.spinnerWalletIcon);
        Spinner spinnerColor = dialogView.findViewById(R.id.spinnerWalletColor);
        Button btnHuy = dialogView.findViewById(R.id.btnCancelWallet);
        Button btnLuu = dialogView.findViewById(R.id.btnSaveWallet);

        String[] iconKeys = {"cash", "bank", "saving"};
        String[] iconLabels = {"Tiền mặt", "Ngân hàng", "Tiết kiệm"};
        String[] colorHexes = {"#4CAF50", "#2196F3", "#F44336", "#FF9800", "#9C27B0"};
        String[] colorLabels = {"Xanh lá", "Xanh dương", "Đỏ", "Cam", "Tím"};

        ArrayAdapter<String> iconAdapter = new ArrayAdapter<>(getContext(), R.layout.item_spinner, iconLabels);
        iconAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerIcon.setAdapter(iconAdapter);

        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(getContext(), R.layout.item_spinner, colorLabels);
        colorAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerColor.setAdapter(colorAdapter);

        if (walletToEdit != null) {
            etName.setText(walletToEdit.getName());
            etBalance.setText(String.valueOf((long) walletToEdit.getBalance()));
            if (walletToEdit.getMinBalance() > 0) {
                etMinBalance.setText(String.valueOf((long) walletToEdit.getMinBalance()));
            }
            for (int i = 0; i < iconKeys.length; i++) {
                if (iconKeys[i].equals(walletToEdit.getIcon())) {
                    spinnerIcon.setSelection(i);
                    break;
                }
            }
            for (int i = 0; i < colorHexes.length; i++) {
                if (colorHexes[i].equals(walletToEdit.getColor())) {
                    spinnerColor.setSelection(i);
                    break;
                }
            }
        }

        btnHuy.setOnClickListener(v -> dialog.dismiss());

        btnLuu.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            // Xử lý dấu phẩy do bàn phím tạo ra
            String balanceStr = etBalance.getText().toString().trim().replace(",", ".");
            String minBalanceStr = etMinBalance.getText().toString().trim().replace(",", ".");
            
            if (name.isEmpty() || balanceStr.isEmpty()) {
                android.widget.Toast.makeText(getContext(), "Vui lòng nhập tên và số dư", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            double balance = 0;
            double minBalance = 0;
            try {
                balance = Double.parseDouble(balanceStr);
                if (!minBalanceStr.isEmpty()) {
                    minBalance = Double.parseDouble(minBalanceStr);
                }
            } catch (NumberFormatException e) {
                android.widget.Toast.makeText(getContext(), "Số tiền không hợp lệ!", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            String selectedIcon = iconKeys[spinnerIcon.getSelectedItemPosition()];
            String selectedColor = colorHexes[spinnerColor.getSelectedItemPosition()];

            if (walletToEdit == null) {
                walletDAO.addWallet(new ViTien(0, name, balance, "cash", "VND", selectedIcon, selectedColor, minBalance));
            } else {
                walletToEdit.setName(name);
                walletToEdit.setBalance(balance);
                walletToEdit.setMinBalance(minBalance);
                walletToEdit.setIcon(selectedIcon);
                walletToEdit.setColor(selectedColor);
                walletDAO.updateWallet(walletToEdit);
            }
            dialog.dismiss();
            refreshData();
        });
        dialog.show();
    }

    private void showDeleteConfirmDialog(ViTien wallet) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa ví")
                .setMessage("Xác nhận xóa ví '" + wallet.getName() + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    walletDAO.deleteWallet(wallet.getId());
                    refreshData();
                })
                .setNegativeButton("Hủy", null).show();
    }

    private String dinhDangTien(double amount) {
        return String.format(Locale.GERMANY, "%,.0f", amount);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (walletDAO != null) walletDAO.close();
        if (transactionDAO != null) transactionDAO.close();
    }
}
