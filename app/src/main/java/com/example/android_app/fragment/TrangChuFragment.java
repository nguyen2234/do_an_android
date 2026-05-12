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
import com.example.android_app.adapter.GiaoDichAdapter;
import com.example.android_app.database.GiaoDichDAO;
import com.example.android_app.model.GiaoDich;
import java.util.ArrayList;
import java.util.List;

public class TrangChuFragment extends Fragment {

    private GiaoDichDAO transactionDAO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trang_chu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Khởi tạo DAO
        transactionDAO = new GiaoDichDAO(getContext());
        transactionDAO.open();

        TextView tvTotalBalance = view.findViewById(R.id.tvTongSoDu);
        TextView tvTongThuNhap = view.findViewById(R.id.tvThuNhap);
        TextView tvTongChiTieu = view.findViewById(R.id.tvChiTieu);
        TextView tvUserName = view.findViewById(R.id.tvTenNguoiDung);
        RecyclerView rvTransactions = view.findViewById(R.id.rvGiaoDich);

        // Thiết lập RecyclerView
        if (rvTransactions != null) {
            rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTransactions.setNestedScrollingEnabled(false);

            // Lấy dữ liệu từ DB
            List<GiaoDich> transactions = transactionDAO.getAllTransactions();
            
            // Tính toán tổng thu chi
            double totalIncome = 0;
            double totalExpense = 0;
            for (GiaoDich t : transactions) {
                if (t.isIncome()) totalIncome += t.getSoTien();
                if (t.isExpense()) totalExpense += t.getSoTien();
            }
            double balance = totalIncome - totalExpense;

            // Hiển thị danh sách
            GiaoDichAdapter adapter = new GiaoDichAdapter(getContext(), transactions);
            rvTransactions.setAdapter(adapter);

            // Cập nhật text tổng quan
            if (tvTotalBalance != null) {
                tvTotalBalance.setText(getString(R.string.format_currency_neutral, dinhDangTien(balance)));
            }
            if (tvTongThuNhap != null) {
                tvTongThuNhap.setText(getString(R.string.format_currency_income, dinhDangTien(totalIncome)));
            }
            if (tvTongChiTieu != null) {
                tvTongChiTieu.setText(getString(R.string.format_currency_expense, dinhDangTien(totalExpense)));
            }
        }

        if (tvUserName != null) {
            android.content.SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
            String username = prefs.getString("username", "Người dùng");
            tvUserName.setText(username);
        }

        // View tvSeeAll = view.findViewById(R.id.tvSeeAll);
        // if (tvSeeAll != null) {
        //     tvSeeAll.setOnClickListener(v -> {
        //         // TODO: Chuyển sang màn hình tất cả giao dịch
        //     });
        // }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (transactionDAO != null) {
            transactionDAO.close();
        }
    }

    private String dinhDangTien(double amount) {
        return String.format("%,.0f", amount).replace(",", ".");
    }
}
