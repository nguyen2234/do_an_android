package com.example.android_app.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android_app.R;
import com.example.android_app.adapter.TransactionAdapter;
import com.example.android_app.database.TransactionDAO;
import com.example.android_app.model.Transaction;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private TransactionDAO transactionDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo DAO
        transactionDAO = new TransactionDAO(getContext());
        transactionDAO.open();

        TextView tvTotalBalance = view.findViewById(R.id.tvTotalBalance);
        TextView tvTotalIncome = view.findViewById(R.id.tvTotalIncome);
        TextView tvTotalExpense = view.findViewById(R.id.tvTotalExpense);
        TextView tvUserName = view.findViewById(R.id.tvUserName);
        RecyclerView rvTransactions = view.findViewById(R.id.rvTransactions);

        // Thiết lập RecyclerView
        if (rvTransactions != null) {
            rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTransactions.setNestedScrollingEnabled(false);

            // Lấy dữ liệu từ DB
            List<Transaction> transactions = transactionDAO.getAllTransactions();
            
            // Tính toán tổng thu chi
            double totalIncome = 0;
            double totalExpense = 0;
            for (Transaction t : transactions) {
                if (t.isIncome()) totalIncome += t.getAmount();
                if (t.isExpense()) totalExpense += t.getAmount();
            }
            double balance = totalIncome - totalExpense;

            // Hiển thị danh sách
            TransactionAdapter adapter = new TransactionAdapter(getContext(), transactions);
            rvTransactions.setAdapter(adapter);

            // Cập nhật text tổng quan
            if (tvTotalBalance != null) {
                tvTotalBalance.setText(getString(R.string.format_currency_neutral, formatMoney(balance)));
            }
            if (tvTotalIncome != null) {
                tvTotalIncome.setText(getString(R.string.format_currency_income, formatMoney(totalIncome)));
            }
            if (tvTotalExpense != null) {
                tvTotalExpense.setText(getString(R.string.format_currency_expense, formatMoney(totalExpense)));
            }
        }

        if (tvUserName != null) tvUserName.setText("Nguyễn Văn A");

        View tvSeeAll = view.findViewById(R.id.tvSeeAll);
        if (tvSeeAll != null) {
            tvSeeAll.setOnClickListener(v -> {
                // TODO: Chuyển sang màn hình tất cả giao dịch
            });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (transactionDAO != null) {
            transactionDAO.close();
        }
    }

    private String formatMoney(double amount) {
        return String.format("%,.0f", amount).replace(",", ".");
    }
}
