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
import android.widget.Toast;
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

        
        if (tvTongThuNhap != null) tvTongThuNhap.setText("+ " + dinhDangTien(income) + " ₫");
        if (tvTongChiTieu != null) tvTongChiTieu.setText("- " + dinhDangTien(expense) + " ₫");

        
        double totalBalance = walletDAO.getTotalBalance();
        if (tvTotalBalance != null) {
            tvTotalBalance.setText(dinhDangTien(totalBalance) + " ₫");
        }
    }

    private void showWalletDialog(ViTien existingWallet) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_vi_tien, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        TextView tvTitle = dialogView.findViewById(R.id.tvDialogTitle);
        EditText etName = dialogView.findViewById(R.id.etWalletName);
        EditText etBalance = dialogView.findViewById(R.id.etWalletBalance);
        EditText etMinBalance = dialogView.findViewById(R.id.etWalletMinBalance);
        Spinner spinnerIcon = dialogView.findViewById(R.id.spinnerWalletIcon);
        Spinner spinnerColor = dialogView.findViewById(R.id.spinnerWalletColor);
        Button btnCancel = dialogView.findViewById(R.id.btnCancelWallet);
        Button btnSave = dialogView.findViewById(R.id.btnSaveWallet);

        String[] icons = {"cash", "bank", "saving"};
        String[] iconNames = {"Tiền mặt", "Ngân hàng", "Tiết kiệm"};
        String[] colors = {"#4CAF50", "#2196F3", "#FF9800", "#E91E63", "#9C27B0"};
        String[] colorNames = {"Xanh lá", "Xanh dương", "Cam", "Hồng", "Tím"};

        ArrayAdapter<String> iconAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, iconNames);
        iconAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIcon.setAdapter(iconAdapter);

        ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, colorNames);
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerColor.setAdapter(colorAdapter);

        if (existingWallet != null) {
            tvTitle.setText("Sửa ví tiền");
            etName.setText(existingWallet.getName());
            etBalance.setText(String.valueOf(existingWallet.getBalance()));
            etMinBalance.setText(String.valueOf(existingWallet.getMinBalance()));
            for (int i = 0; i < icons.length; i++) {
                if (icons[i].equalsIgnoreCase(existingWallet.getIcon())) {
                    spinnerIcon.setSelection(i);
                    break;
                }
            }
            for (int i = 0; i < colors.length; i++) {
                if (colors[i].equalsIgnoreCase(existingWallet.getColor())) {
                    spinnerColor.setSelection(i);
                    break;
                }
            }
        } else {
            tvTitle.setText("Thêm ví mới");
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng nhập tên ví", Toast.LENGTH_SHORT).show();
                return;
            }

            double balance = 0;
            String balanceStr = etBalance.getText().toString().trim();
            if (!balanceStr.isEmpty()) {
                try {
                    balance = Double.parseDouble(balanceStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Số dư không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            double minBalance = 0;
            String minBalanceStr = etMinBalance.getText().toString().trim();
            if (!minBalanceStr.isEmpty()) {
                try {
                    minBalance = Double.parseDouble(minBalanceStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(getContext(), "Số dư cảnh báo không hợp lệ", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String selectedIcon = icons[spinnerIcon.getSelectedItemPosition()];
            String selectedColor = colors[spinnerColor.getSelectedItemPosition()];

            if (existingWallet != null) {
                existingWallet.setName(name);
                existingWallet.setBalance(balance);
                existingWallet.setMinBalance(minBalance);
                existingWallet.setIcon(selectedIcon);
                existingWallet.setColor(selectedColor);
                walletDAO.updateWallet(existingWallet);
                Toast.makeText(getContext(), "Đã cập nhật ví tiền", Toast.LENGTH_SHORT).show();
            } else {
                ViTien newWallet = new ViTien(0, name, balance, selectedIcon, "VND", selectedIcon, selectedColor, minBalance);
                walletDAO.addWallet(newWallet);
                Toast.makeText(getContext(), "Đã thêm ví tiền", Toast.LENGTH_SHORT).show();
            }

            refreshData();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showDeleteConfirmDialog(ViTien wallet) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa ví tiền")
                .setMessage("Bạn có muốn xóa ví tiền này không? Các dữ liệu thanh toán sẽ được giữ nguyên.")
                .setPositiveButton("Đồng ý", (dialog, which) -> {
                    int result = walletDAO.deleteWallet(wallet.getId());
                    if (result > 0) {
                        Toast.makeText(getContext(), "Đã xóa ví tiền", Toast.LENGTH_SHORT).show();
                        refreshData();
                    } else {
                        Toast.makeText(getContext(), "Xóa ví tiền thất bại", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
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
