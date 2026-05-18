package com.example.android_app.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.android_app.database.GiaoDichDuKienDAO;
import com.example.android_app.model.GiaoDichDuKien;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * WorkManager Worker: Chạy ngầm mỗi ngày để kiểm tra các khoản thu/chi sắp đến hạn
 * và gửi thông báo nhắc nhở người dùng.
 */
public class ReminderWorker extends Worker {

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi"));

        // Lấy ngày hôm nay và ngày mai
        Calendar today = Calendar.getInstance();
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);

        String todayStr = sdf.format(today.getTime());
        String tomorrowStr = sdf.format(tomorrow.getTime());

        // Truy vấn các khoản đến hạn
        GiaoDichDuKienDAO dao = new GiaoDichDuKienDAO(context);
        dao.open();
        List<GiaoDichDuKien> dueSoon = dao.getDueSoon(todayStr, tomorrowStr);
        dao.close();

        // Gửi thông báo cho từng khoản đến hạn
        for (int i = 0; i < dueSoon.size(); i++) {
            GiaoDichDuKien item = dueSoon.get(i);
            String amountStr = String.format("%,.0f", item.getAmount()).replace(",", ".") + " ₫";
            String typeLabel = item.isExpense() ? "chi" : "thu";
            boolean isDueToday = todayStr.equals(item.getDueDate());

            String title = isDueToday
                    ? "⏰ Nhắc nhở: Khoản " + typeLabel + " đến hạn hôm nay!"
                    : "📅 Nhắc nhở: Khoản " + typeLabel + " đến hạn ngày mai";

            String content = item.getTitle() + " — " + amountStr;
            if (item.getNote() != null && !item.getNote().isEmpty()) {
                content += " (" + item.getNote() + ")";
            }

            NotificationHelper.showReminderNotification(
                    context,
                    NotificationHelper.NOTIF_ID_REMINDER_BASE + i,
                    title,
                    content
            );
        }

        return Result.success();
    }
}
