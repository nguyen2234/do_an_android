package com.example.android_app;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.android_app.adapter.ThongBaoAdapter;
import com.example.android_app.database.ThongBaoDAO;
import com.example.android_app.model.ThongBao;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity hiển thị và quản lý Trung tâm thông báo.
 */
public class ThongBaoActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private LinearLayout layoutEmpty;
    private MaterialButton btnDeleteAll;
    private TextView btnMarkAllRead;
    
    private ThongBaoDAO notificationDAO;
    private ThongBaoAdapter adapter;
    private List<ThongBao> notificationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_bao);

        // Khởi tạo DAO
        notificationDAO = new ThongBaoDAO(this);
        notificationDAO.open();

        // Ánh xạ views
        View btnBack = findViewById(R.id.btnBackNotification);
        btnMarkAllRead = findViewById(R.id.btnMarkAllRead);
        layoutEmpty = findViewById(R.id.layoutEmptyNotification);
        rvNotifications = findViewById(R.id.rvNotifications);
        btnDeleteAll = findViewById(R.id.btnDeleteAllNotifications);

        // Nút quay lại
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        // Thiết lập RecyclerView
        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ThongBaoAdapter(this, notificationList, this::showNotificationDetail);
        rvNotifications.setAdapter(adapter);

        // Đọc tất cả
        if (btnMarkAllRead != null) {
            btnMarkAllRead.setOnClickListener(v -> markAllNotificationsAsRead());
        }

        // Xóa tất cả
        if (btnDeleteAll != null) {
            btnDeleteAll.setOnClickListener(v -> confirmDeleteAllNotifications());
        }

        // Load dữ liệu
        loadNotifications();
    }

    private void loadNotifications() {
        if (notificationDAO == null) return;
        
        notificationList = notificationDAO.getAllNotifications();
        adapter.updateData(notificationList);

        // Kiểm tra xem danh sách có trống không để cập nhật giao diện
        if (notificationList.isEmpty()) {
            rvNotifications.setVisibility(View.GONE);
            btnDeleteAll.setVisibility(View.GONE);
            if (btnMarkAllRead != null) {
                btnMarkAllRead.setVisibility(View.GONE);
            }
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            rvNotifications.setVisibility(View.VISIBLE);
            btnDeleteAll.setVisibility(View.VISIBLE);
            if (btnMarkAllRead != null) {
                btnMarkAllRead.setVisibility(View.VISIBLE);
            }
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    /**
     * Hiển thị hộp thoại chi tiết thông báo khi người dùng click vào.
     */
    private void showNotificationDetail(ThongBao thongBao) {
        // Đánh dấu là đã đọc trong DB
        if (!thongBao.isRead()) {
            notificationDAO.markAsRead(thongBao.getId());
            loadNotifications(); // Reload dữ liệu để cập nhật chấm xanh biến mất
        }

        // Hiển thị Dialog chi tiết
        new AlertDialog.Builder(this)
                .setTitle(thongBao.getTitle())
                .setMessage(thongBao.getContent() + "\n\nThời gian: " + thongBao.getDate())
                .setPositiveButton("Đóng", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Đánh dấu toàn bộ thông báo là đã đọc.
     */
    private void markAllNotificationsAsRead() {
        int result = notificationDAO.markAllAsRead();
        if (result > 0) {
            Toast.makeText(this, "Đã đánh dấu đã đọc tất cả thông báo", Toast.LENGTH_SHORT).show();
            loadNotifications();
        } else {
            Toast.makeText(this, "Không có thông báo mới nào để đánh dấu", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Xác nhận trước khi xóa toàn bộ lịch sử thông báo.
     */
    private void confirmDeleteAllNotifications() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa toàn bộ thông báo")
                .setMessage("Bạn có chắc chắn muốn xóa toàn bộ lịch sử thông báo không? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    notificationDAO.clearAllNotifications();
                    Toast.makeText(this, "Đã xóa toàn bộ lịch sử thông báo", Toast.LENGTH_SHORT).show();
                    loadNotifications();
                })
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (notificationDAO != null) {
            notificationDAO.close();
        }
    }
}
