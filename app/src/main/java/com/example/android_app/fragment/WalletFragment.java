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
import com.example.android_app.adapter.WalletAdapter;
import com.example.android_app.database.TransactionDAO;
import com.example.android_app.database.WalletDAO;
import com.example.android_app.model.Transaction;
import com.example.android_app.model.Wallet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WalletFragment extends Fragment {

    private RecyclerView rvWallets;
    private TextView tvTotalBalance, tvTotalIncome, tvTotalExpense;
    private WalletDAO walletDAO;
    private TransactionDAO transactionDAO;
    private WalletAdapter adapter;
    private List<Wallet> wallets;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wallet, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ánh xạ View với ID chính xác từ fragment_wallet.xml
        tvTotalBalance = view.findViewById(R.id.tvTotalBalance);
        tvTotalIncome = view.findViewById(R.id.tvCardIncome);
        tvTotalExpense = view.findViewById(R.id.tvCardExpense);
        rvWallets = view.findViewById(R.id.rvWallets);

        rvWallets.setLayoutManager(new LinearLayoutManager(getContext()));
        rvWallets.setNestedScrollingEnabled(false);

        walletDAO = new WalletDAO(getContext());
        transactionDAO = new TransactionDAO(getContext());
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
            walletDAO.addWallet(new Wallet(0, "Tiền mặt", 0, "cash", "VND", "cash", "#4CAF50"));
            wallets = walletDAO.getAllWallets();
        }

        adapter = new WalletAdapter(getContext(), wallets);
        adapter.setOnEditClickListener(this::showWalletDialog);
        adapter.setOnDeleteClickListener(this::showDeleteConfirmDialog);
        rvWallets.setAdapter(adapter);
    }

    private void updateSummary() {
        List<Transaction> transactions = transactionDAO.getAllTransactions();
        double income = 0;
        double expense = 0;

        for (Transaction t : transactions) {
            if ("income".equalsIgnoreCase(t.getType())) {
                income += t.getAmount();
            } else {
                expense += t.getAmount();
            }
        }

        // Cập nhật text Thu nhập / Chi tiêu
        if (tvTotalIncome != null) tvTotalIncome.setText("+ " + formatMoney(income) + " ₫");
        if (tvTotalExpense != null) tvTotalExpense.setText("- " + formatMoney(expense) + " ₫");
        
        // QUAN TRỌNG: Tổng số dư = Tổng Thu nhập - Tổng Chi tiêu
        double actualTotalBalance = income - expense;
        tvTotalBalance.setText(formatMoney(actualTotalBalance) + " ₫");
    }

    private void showWalletDialog(@Nullable Wallet walletToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_wallet, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        EditText etName = dialogView.findViewById(R.id.etWalletName);
        EditText etBalance = dialogView.findViewById(R.id.etWalletBalance);
        Spinner spinnerIcon = dialogView.findViewById(R.id.spinnerWalletIcon);
        Spinner spinnerColor = dialogView.findViewById(R.id.spinnerWalletColor);
        Button btnSave = dialogView.findViewById(R.id.btnSaveWallet);

        String[] iconKeys = {"cash", "bank", "saving"};
        String[] colorHexes = {"#4CAF50", "#2196F3", "#F44336", "#FF9800", "#9C27B0"};

        if (walletToEdit != null) {
            etName.setText(walletToEdit.getName());
            etBalance.setText(String.valueOf((long) walletToEdit.getBalance()));
        }

        btnSave.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String balanceStr = etBalance.getText().toString().trim();
            if (name.isEmpty() || balanceStr.isEmpty()) return;

            double balance = Double.parseDouble(balanceStr);
            if (walletToEdit == null) {
                walletDAO.addWallet(new Wallet(0, name, balance, "cash", "VND", "cash", "#4CAF50"));
            } else {
                walletToEdit.setName(name);
                walletToEdit.setBalance(balance);
                walletDAO.updateWallet(walletToEdit);
            }
            dialog.dismiss();
            refreshData();
        });
        dialog.show();
    }

    private void showDeleteConfirmDialog(Wallet wallet) {
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa ví")
                .setMessage("Xác nhận xóa ví '" + wallet.getName() + "'?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    walletDAO.deleteWallet(wallet.getId());
                    refreshData();
                })
                .setNegativeButton("Hủy", null).show();
    }

    private String formatMoney(double amount) {
        return String.format(Locale.GERMANY, "%,.0f", amount);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (walletDAO != null) walletDAO.close();
        if (transactionDAO != null) transactionDAO.close();
    }
}
