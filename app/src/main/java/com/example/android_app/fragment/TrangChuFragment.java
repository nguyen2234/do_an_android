package com.example.android_app.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.widget.ProgressBar;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android_app.ChuyenTienActivity;
import com.example.android_app.NapTienActivity;
import com.example.android_app.R;
import com.example.android_app.adapter.GiaoDichAdapter;
import com.example.android_app.database.GiaoDichDAO;
import com.example.android_app.database.NganSachDAO;
import com.example.android_app.model.GiaoDich;
import com.example.android_app.model.NganSach;
import com.example.android_app.fragment.GiaoDichDuKienFragment;
import com.example.android_app.database.ThongBaoDAO;
import com.example.android_app.ThongBaoActivity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrangChuFragment extends Fragment {

    private GiaoDichDAO transactionDAO;
    private NganSachDAO budgetDAO;
    private ThongBaoDAO notificationDAO;
    private TextView tvNotificationBadge;

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
        budgetDAO = new NganSachDAO(getContext());
        notificationDAO = new ThongBaoDAO(getContext());
        transactionDAO.open();
        budgetDAO.open();
        notificationDAO.open();

        TextView tvTotalBalance = view.findViewById(R.id.tvTongSoDu);
        TextView tvTongThuNhap = view.findViewById(R.id.tvThuNhap);
        TextView tvTongChiTieu = view.findViewById(R.id.tvChiTieu);
        TextView tvUserName = view.findViewById(R.id.tvTenNguoiDung);
        RecyclerView rvTransactions = view.findViewById(R.id.rvGiaoDich);

        TextView tvBudgetUsedHome = view.findViewById(R.id.tvBudgetUsedHome);
        TextView tvBudgetTotalHome = view.findViewById(R.id.tvBudgetTotalHome);
        TextView tvBudgetStatusHome = view.findViewById(R.id.tvBudgetStatusHome);
        ProgressBar pbBudgetHome = view.findViewById(R.id.pbBudgetHome);

        tinhToanNganSachThang(tvBudgetUsedHome, tvBudgetTotalHome, tvBudgetStatusHome, pbBudgetHome);

        // Thiết lập RecyclerView
        if (rvTransactions != null) {
            rvTransactions.setLayoutManager(new LinearLayoutManager(getContext()));
            rvTransactions.setNestedScrollingEnabled(false);

            // Lấy dữ liệu từ DB
            List<GiaoDich> transactions = transactionDAO.getAllTransactions();
            
            // Tính toán tổng thu chi từ tất cả giao dịch
            double totalIncome = 0;
            double totalExpense = 0;
            for (GiaoDich t : transactions) {
                if (t.isIncome()) totalIncome += t.getSoTien();
                if (t.isExpense()) totalExpense += t.getSoTien();
            }
            double balance = totalIncome - totalExpense;

            // Lấy 5 giao dịch gần đây nhất để hiển thị
            List<GiaoDich> recentTransactions = transactionDAO.getRecentTransactions(5);

            // Hiển thị danh sách
            GiaoDichAdapter adapter = new GiaoDichAdapter(getContext(), recentTransactions);
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
            String displayName = prefs.getString("fullname", "");
            if (displayName.isEmpty()) {
                displayName = prefs.getString("username", "Người dùng");
            }
            tvUserName.setText(displayName);
        }

        // Ánh xạ nút Thông báo
        View layoutNotification = view.findViewById(R.id.layoutNotification);
        tvNotificationBadge = view.findViewById(R.id.tvNotificationBadge);

        if (layoutNotification != null) {
            layoutNotification.setOnClickListener(v -> {
                startActivity(new Intent(getContext(), ThongBaoActivity.class));
            });
        }

        // Nút Nạp tiền
        View cardNapTien = view.findViewById(R.id.cardNapTien);
        if (cardNapTien != null) {
            cardNapTien.setOnClickListener(v ->
                    startActivity(new Intent(getContext(), NapTienActivity.class)));
        }

        // Nút Chuyển tiền
        View cardChuyenTien = view.findViewById(R.id.cardChuyenTien);
        if (cardChuyenTien != null) {
            cardChuyenTien.setOnClickListener(v ->
                    startActivity(new Intent(getContext(), ChuyenTienActivity.class)));
        }

        // Nút xem khoản đến hạn
        View tvXemDuKien = view.findViewById(R.id.tvXemDuKien);
        if (tvXemDuKien != null) {
            tvXemDuKien.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragmentContainer, new GiaoDichDuKienFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật số lượng thông báo chưa đọc khi quay lại màn hình
        updateNotificationBadge();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (transactionDAO != null) transactionDAO.close();
        if (budgetDAO != null) budgetDAO.close();
        if (notificationDAO != null) notificationDAO.close();
    }

    private void updateNotificationBadge() {
        if (tvNotificationBadge == null || notificationDAO == null) return;
        try {
            int unreadCount = notificationDAO.getUnreadCount();
            if (unreadCount > 0) {
                if (unreadCount > 9) {
                    tvNotificationBadge.setText("9+");
                } else {
                    tvNotificationBadge.setText(String.valueOf(unreadCount));
                }
                tvNotificationBadge.setVisibility(View.VISIBLE);
            } else {
                tvNotificationBadge.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tinhToanNganSachThang(TextView tvUsed, TextView tvTotal, TextView tvStatus, ProgressBar pb) {
        if (tvUsed == null || tvTotal == null || tvStatus == null || pb == null) return;

        double totalAmount = 0;
        double totalSpent = 0;

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi"));
            Date today = new Date();

            List<NganSach> list = budgetDAO.getAllBudgets();
            for (NganSach b : list) {
                Date start = sdf.parse(b.getStartDate());
                Date end = sdf.parse(b.getEndDate());

                if (!today.before(start) && !today.after(end)) {
                    totalAmount += b.getAmount();
                    totalSpent += b.getSpentAmount();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        tvUsed.setText(dinhDangTien(totalSpent) + " ₫");
        tvTotal.setText(dinhDangTien(totalAmount) + " ₫");

        int progress = 0;
        if (totalAmount > 0) {
            progress = (int) ((totalSpent / totalAmount) * 100);
        }
        if (progress > 100) progress = 100;
        pb.setProgress(progress);

        tvStatus.setText("Đã dùng " + progress + "% ngân sách");
    }

    private String dinhDangTien(double amount) {
        return String.format("%,.0f", amount).replace(",", ".");
    }
}
