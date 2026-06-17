package com.example.android_app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.android_app.MainActivity;
import com.example.android_app.R;
import com.example.android_app.database.ThongBaoDAO;
import com.example.android_app.model.ThongBao;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class NotificationHelper {

    
    public static final String CHANNEL_REMINDER = "channel_reminder";
    public static final String CHANNEL_TRANSACTION = "channel_transaction";
    public static final String CHANNEL_LOW_BALANCE = "channel_low_balance";

    
    public static final int NOTIF_ID_REMINDER_BASE = 1000;
    public static final int NOTIF_ID_TRANSFER = 2001;
    public static final int NOTIF_ID_TOP_UP = 2002;
    public static final int NOTIF_ID_LOW_BALANCE = 3001;
    public static final int NOTIF_ID_EXPENSE = 2003;

    
    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);

            
            NotificationChannel reminderChannel = new NotificationChannel(
                    CHANNEL_REMINDER,
                    "Nhắc nhở khoản thu/chi",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            reminderChannel.setDescription("Thông báo nhắc nhở các khoản thu/chi sắp đến hạn");

            
            NotificationChannel transactionChannel = new NotificationChannel(
                    CHANNEL_TRANSACTION,
                    "Thông báo giao dịch",
                    NotificationManager.IMPORTANCE_HIGH
            );
            transactionChannel.setDescription("Thông báo khi thực hiện nạp tiền hoặc chuyển tiền");

            
            NotificationChannel lowBalanceChannel = new NotificationChannel(
                    CHANNEL_LOW_BALANCE,
                    "Cảnh báo số dư thấp",
                    NotificationManager.IMPORTANCE_HIGH
            );
            lowBalanceChannel.setDescription("Thông báo khi số dư ví xuống dưới mức tối thiểu");

            manager.createNotificationChannel(reminderChannel);
            manager.createNotificationChannel(transactionChannel);
            manager.createNotificationChannel(lowBalanceChannel);
        }
    }

    
    public static void showReminderNotification(Context context, int notifId,
                                                 String title, String content) {
        
        saveNotificationToDatabase(context, notifId, title, content);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notifId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_REMINDER)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(notifId, builder.build());
        } catch (SecurityException e) {
            
        }
    }

    
    public static void showTopUpNotification(Context context, String walletName, double amount) {
        String amountStr = String.format("%,.0f", amount).replace(",", ".") + " ₫";
        showTransactionNotification(context, NOTIF_ID_TOP_UP,
                "Nạp tiền thành công",
                "Đã nạp " + amountStr + " vào ví " + walletName);
    }

    
    public static void showExpenseNotification(Context context, String walletName, double amount, String category) {
        String amountStr = String.format("%,.0f", amount).replace(",", ".") + " ₫";
        showTransactionNotification(context, NOTIF_ID_EXPENSE,
                "Chi tiêu thành công",
                "Đã chi " + amountStr + " từ ví " + walletName + " cho \"" + category + "\"");
    }

    
    public static void showTransferNotification(Context context, String fromWallet,
                                                  String toWallet, double amount) {
        String amountStr = String.format("%,.0f", amount).replace(",", ".") + " ₫";
        showTransactionNotification(context, NOTIF_ID_TRANSFER,
                "Chuyển tiền thành công",
                "Đã chuyển " + amountStr + " từ " + fromWallet + " sang " + toWallet);
    }

    
    public static void showLowBalanceNotification(Context context, String walletName, double balance) {
        String balanceStr = String.format("%,.0f", balance).replace(",", ".") + " ₫";
        showTransactionNotification(context, NOTIF_ID_LOW_BALANCE,
                "Số dư ví sắp hết",
                "Ví \"" + walletName + "\" chỉ còn " + balanceStr + ". Vui lòng nạp thêm tiền!");
    }

    
    private static void showTransactionNotification(Context context, int notifId,
                                                     String title, String content) {
        
        saveNotificationToDatabase(context, notifId, title, content);

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notifId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_TRANSACTION)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        try {
            NotificationManagerCompat.from(context).notify(notifId, builder.build());
        } catch (SecurityException e) {
            
        }
    }

    
    private static void saveNotificationToDatabase(Context context, int notifId, String title, String content) {
        try {
            ThongBaoDAO dao = new ThongBaoDAO(context);
            dao.open();

            ThongBao tb = new ThongBao();
            tb.setTitle(title);
            tb.setContent(content);

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi"));
            tb.setDate(sdf.format(new Date()));
            tb.setRead(false);

            
            if (notifId == NOTIF_ID_LOW_BALANCE) {
                tb.setType("warning");
            } else if (notifId == NOTIF_ID_TRANSFER || notifId == NOTIF_ID_TOP_UP || notifId == NOTIF_ID_EXPENSE) {
                tb.setType("transaction");
            } else {
                tb.setType("reminder");
            }

            tb.setUserId(0); 

            dao.addNotification(tb);
            dao.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
