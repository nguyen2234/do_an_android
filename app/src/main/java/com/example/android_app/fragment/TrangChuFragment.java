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
import com.example.android_app.MainActivity;
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
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.LinearLayout;

public class TrangChuFragment extends Fragment {

    private GiaoDichDAO transactionDAO;
    private NganSachDAO budgetDAO;
    private ThongBaoDAO notificationDAO;
    private com.example.android_app.database.ReminderDAO reminderDAO;
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
        reminderDAO = new com.example.android_app.database.ReminderDAO(getContext());
        transactionDAO.open();
        budgetDAO.open();
        notificationDAO.open();
        reminderDAO.open();

        // Nạp dữ liệu lần đầu
        loadHomeData();

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



        // Click vào widget Nhắc hẹn thanh toán -> sang màn hình chính của nhắc hẹn
        View cardPaymentReminderWidget = view.findViewById(R.id.cardPaymentReminderWidget);
        if (cardPaymentReminderWidget != null) {
            cardPaymentReminderWidget.setOnClickListener(v -> {
                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                        .replace(R.id.fragmentContainer, new com.example.android_app.fragment.PaymentReminderFragment())
                        .addToBackStack(null)
                        .commit();
            });
        }

        // Click vào widget Giao dịch gần đây -> sang tab Thống kê
        View cardGiaoDichGanDay = view.findViewById(R.id.cardGiaoDichGanDay);
        if (cardGiaoDichGanDay != null) {
            cardGiaoDichGanDay.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).setSelectedItemId(R.id.nav_stats);
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật số lượng thông báo chưa đọc khi quay lại màn hình
        updateNotificationBadge();
        // Cập nhật lại dữ liệu màn hình chính (số dư, giao dịch, ngân sách...)
        loadHomeData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (transactionDAO != null) transactionDAO.close();
        if (budgetDAO != null) budgetDAO.close();
        if (notificationDAO != null) notificationDAO.close();
        if (reminderDAO != null) reminderDAO.close();
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

    private void loadHomeData() {
        View view = getView();
        if (view == null) return;

        TextView tvTotalBalance = view.findViewById(R.id.tvTongSoDu);
        TextView tvTongThuNhap = view.findViewById(R.id.tvThuNhap);
        TextView tvTongChiTieu = view.findViewById(R.id.tvChiTieu);
        TextView tvUserName = view.findViewById(R.id.tvTenNguoiDung);
        RecyclerView rvTransactions = view.findViewById(R.id.rvGiaoDich);

        LinearLayout layoutBudgetListHome = view.findViewById(R.id.layoutBudgetListHome);
        TextView tvNoBudgetHome = view.findViewById(R.id.tvNoBudgetHome);

        tinhToanNganSachThang(layoutBudgetListHome, tvNoBudgetHome);
        loadReminderWidgetData(view);

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
            adapter.setOnItemClickListener(transaction -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).setSelectedItemId(R.id.nav_stats);
                }
            });
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
    }

    private void tinhToanNganSachThang(LinearLayout layoutBudgetList, TextView tvNoBudget) {
        if (layoutBudgetList == null || tvNoBudget == null) return;

        layoutBudgetList.removeAllViews();
        List<NganSach> activeBudgets = new ArrayList<>();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi"));
            // Loại bỏ giờ/phút/giây để so sánh chính xác mốc ngày bắt đầu và ngày kết thúc
            Date today = sdf.parse(sdf.format(new Date()));

            List<NganSach> list = budgetDAO.getAllBudgets();
            for (NganSach b : list) {
                Date start = sdf.parse(b.getStartDate());
                Date end = sdf.parse(b.getEndDate());

                if (!today.before(start) && !today.after(end)) {
                    activeBudgets.add(b);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (activeBudgets.isEmpty()) {
            tvNoBudget.setVisibility(View.VISIBLE);
            layoutBudgetList.setVisibility(View.GONE);
        } else {
            tvNoBudget.setVisibility(View.GONE);
            layoutBudgetList.setVisibility(View.VISIBLE);
            LayoutInflater inflater = LayoutInflater.from(getContext());
            for (int i = 0; i < activeBudgets.size(); i++) {
                NganSach b = activeBudgets.get(i);
                View itemView = inflater.inflate(R.layout.item_home_budget, layoutBudgetList, false);

                TextView tvBudgetName = itemView.findViewById(R.id.tvBudgetName);
                TextView tvBudgetLimit = itemView.findViewById(R.id.tvBudgetLimit);
                ProgressBar pbBudget = itemView.findViewById(R.id.pbBudget);
                TextView tvBudgetPercent = itemView.findViewById(R.id.tvBudgetPercent);

                if (tvBudgetName != null) {
                    tvBudgetName.setText(b.getName());
                }
                if (tvBudgetLimit != null) {
                    tvBudgetLimit.setText("Đã chi " + dinhDangTien(b.getSpentAmount()) + " ₫ / " + dinhDangTien(b.getAmount()) + " ₫");
                }

                int progress = 0;
                if (b.getAmount() > 0) {
                    progress = (int) ((b.getSpentAmount() / b.getAmount()) * 100);
                }

                if (pbBudget != null) {
                    pbBudget.setProgress(Math.min(progress, 100));
                    if (progress >= 100) {
                        pbBudget.setProgressTintList(ColorStateList.valueOf(Color.RED));
                    } else {
                        pbBudget.setProgressTintList(null);
                    }
                }

                if (tvBudgetPercent != null) {
                    tvBudgetPercent.setText(progress + "%");
                    if (progress >= 100) {
                        tvBudgetPercent.setTextColor(Color.RED);
                    } else {
                        tvBudgetPercent.setTextColor(getResources().getColor(R.color.colorPrimary));
                    }
                }

                layoutBudgetList.addView(itemView);

                // Thêm đường kẻ phân cách nếu chưa phải phần tử cuối
                if (i < activeBudgets.size() - 1) {
                    View divider = new View(getContext());
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                    );
                    lp.setMargins(0, 4, 0, 4);
                    divider.setLayoutParams(lp);
                    divider.setBackgroundColor(getResources().getColor(R.color.colorDivider));
                    layoutBudgetList.addView(divider);
                }
            }
        }
    }

    private String dinhDangTien(double amount) {
        return String.format("%,.0f", amount).replace(",", ".");
    }

    private void loadReminderWidgetData(View view) {
        LinearLayout layoutReminderListHome = view.findViewById(R.id.layoutReminderListHome);
        TextView tvNoReminderHome = view.findViewById(R.id.tvNoReminderHome);
        if (layoutReminderListHome == null || tvNoReminderHome == null || reminderDAO == null) return;

        layoutReminderListHome.removeAllViews();
        List<com.example.android_app.model.Reminder> pendingReminders = reminderDAO.getPendingReminders(5);

        if (pendingReminders.isEmpty()) {
            tvNoReminderHome.setVisibility(View.VISIBLE);
            layoutReminderListHome.setVisibility(View.GONE);
        } else {
            tvNoReminderHome.setVisibility(View.GONE);
            layoutReminderListHome.setVisibility(View.VISIBLE);
            LayoutInflater inflater = LayoutInflater.from(getContext());
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

            for (int i = 0; i < pendingReminders.size(); i++) {
                com.example.android_app.model.Reminder r = pendingReminders.get(i);
                View itemView = inflater.inflate(R.layout.item_home_reminder, layoutReminderListHome, false);

                TextView tvTitle = itemView.findViewById(R.id.tvReminderTitle);
                TextView tvDue = itemView.findViewById(R.id.tvReminderDue);
                TextView tvAmount = itemView.findViewById(R.id.tvReminderAmount);
                TextView tvRecurrence = itemView.findViewById(R.id.tvReminderRecurrence);

                if (tvTitle != null) tvTitle.setText(r.getTitle());
                if (tvDue != null && r.getDueDate() != null) {
                    tvDue.setText("Đến hạn: " + r.getDueDate().format(dtf));
                }
                if (tvAmount != null) {
                    tvAmount.setText(dinhDangTien(r.getEstimatedAmount()) + " ₫");
                }
                if (tvRecurrence != null) {
                    tvRecurrence.setText("Danh mục: " + (r.getCategory() != null ? r.getCategory() : "Khác"));
                }

                layoutReminderListHome.addView(itemView);

                if (i < pendingReminders.size() - 1) {
                    View divider = new View(getContext());
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            1
                    );
                    lp.setMargins(0, 4, 0, 4);
                    divider.setLayoutParams(lp);
                    divider.setBackgroundColor(getResources().getColor(R.color.colorDivider));
                    layoutReminderListHome.addView(divider);
                }
            }
        }
    }
}
