package com.example.android_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.android_app.model.ThongBao;
import java.util.ArrayList;
import java.util.List;


public class ThongBaoDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private Context context;

    public ThongBaoDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
        this.context = context;
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    
    private long getCurrentUserId() {
        if (context == null) return 1;
        android.content.SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return prefs.getLong("user_id", 1);
    }

    
    public long addNotification(ThongBao thongBao) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_NOTIFICATION_TITLE, thongBao.getTitle());
        values.put(DatabaseHelper.COL_NOTIFICATION_CONTENT, thongBao.getContent());
        values.put(DatabaseHelper.COL_NOTIFICATION_DATE, thongBao.getDate());
        values.put(DatabaseHelper.COL_NOTIFICATION_IS_READ, thongBao.isRead() ? 1 : 0);
        values.put(DatabaseHelper.COL_NOTIFICATION_TYPE, thongBao.getType());
        values.put(DatabaseHelper.COL_USER_ID_FK, thongBao.getUserId() > 0 ? thongBao.getUserId() : getCurrentUserId());

        return db.insert(DatabaseHelper.TABLE_NOTIFICATIONS, null, values);
    }

    
    public List<ThongBao> getAllNotifications() {
        List<ThongBao> list = new ArrayList<>();
        long currentUserId = getCurrentUserId();
        
        Cursor cursor = db.query(DatabaseHelper.TABLE_NOTIFICATIONS, null,
                DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(currentUserId)}, null, null,
                DatabaseHelper.COL_NOTIFICATION_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                ThongBao tb = new ThongBao();
                tb.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIFICATION_ID)));
                tb.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIFICATION_TITLE)));
                tb.setContent(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIFICATION_CONTENT)));
                tb.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIFICATION_DATE)));
                tb.setRead(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIFICATION_IS_READ)) == 1);
                tb.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NOTIFICATION_TYPE)));
                tb.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID_FK)));
                list.add(tb);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    
    public int getUnreadCount() {
        long currentUserId = getCurrentUserId();
        String query = "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_NOTIFICATIONS +
                " WHERE " + DatabaseHelper.COL_USER_ID_FK + " = ? AND " +
                DatabaseHelper.COL_NOTIFICATION_IS_READ + " = 0";
        
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(currentUserId)});
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    
    public int markAsRead(int notificationId) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_NOTIFICATION_IS_READ, 1);
        return db.update(DatabaseHelper.TABLE_NOTIFICATIONS, values,
                DatabaseHelper.COL_NOTIFICATION_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(notificationId), String.valueOf(getCurrentUserId())});
    }

    
    public int markAllAsRead() {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_NOTIFICATION_IS_READ, 1);
        return db.update(DatabaseHelper.TABLE_NOTIFICATIONS, values,
                DatabaseHelper.COL_USER_ID_FK + " = ? AND " + DatabaseHelper.COL_NOTIFICATION_IS_READ + " = 0",
                new String[]{String.valueOf(getCurrentUserId())});
    }

    
    public int deleteNotification(int notificationId) {
        return db.delete(DatabaseHelper.TABLE_NOTIFICATIONS,
                DatabaseHelper.COL_NOTIFICATION_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(notificationId), String.valueOf(getCurrentUserId())});
    }

    
    public int clearAllNotifications() {
        return db.delete(DatabaseHelper.TABLE_NOTIFICATIONS,
                DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(getCurrentUserId())});
    }
}
