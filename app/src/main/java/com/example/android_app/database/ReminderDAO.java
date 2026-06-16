package com.example.android_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.android_app.model.Recurrence;
import com.example.android_app.model.Reminder;
import com.example.android_app.model.ReminderStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho bảng Nhắc hẹn thanh toán (reminders).
 */
public class ReminderDAO {

    private SQLiteDatabase db;
    private final DatabaseHelper dbHelper;
    private final Context context;

    public ReminderDAO(Context context) {
        this.dbHelper = new DatabaseHelper(context);
        this.context = context;
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    // Lấy ID người dùng hiện tại từ SharedPreferences
    private long getCurrentUserId() {
        if (context == null) return 1;
        android.content.SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return prefs.getLong("user_id", 1);
    }

    /**
     * Thêm nhắc hẹn thanh toán mới.
     */
    public long addReminder(Reminder item) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_REMINDER_TITLE, item.getTitle());
        values.put(DatabaseHelper.COL_REMINDER_AMOUNT, item.getEstimatedAmount());
        values.put(DatabaseHelper.COL_REMINDER_DUE_DATE, item.getDueDateString());
        values.put(DatabaseHelper.COL_REMINDER_RECURRENCE, item.getRecurrence() != null ? item.getRecurrence().name() : Recurrence.MONTHLY.name());
        values.put(DatabaseHelper.COL_REMINDER_OFFSET_DAYS, item.getReminderOffsetDays());
        values.put(DatabaseHelper.COL_REMINDER_STATUS, item.getStatus() != null ? item.getStatus().name() : ReminderStatus.PENDING.name());
        values.put(DatabaseHelper.COL_REMINDER_CATEGORY, item.getCategory());
        values.put(DatabaseHelper.COL_USER_ID_FK, getCurrentUserId());
        return db.insert(DatabaseHelper.TABLE_REMINDERS, null, values);
    }

    /**
     * Cập nhật nhắc hẹn thanh toán.
     */
    public int updateReminder(Reminder item) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_REMINDER_TITLE, item.getTitle());
        values.put(DatabaseHelper.COL_REMINDER_AMOUNT, item.getEstimatedAmount());
        values.put(DatabaseHelper.COL_REMINDER_DUE_DATE, item.getDueDateString());
        values.put(DatabaseHelper.COL_REMINDER_RECURRENCE, item.getRecurrence() != null ? item.getRecurrence().name() : Recurrence.MONTHLY.name());
        values.put(DatabaseHelper.COL_REMINDER_OFFSET_DAYS, item.getReminderOffsetDays());
        values.put(DatabaseHelper.COL_REMINDER_STATUS, item.getStatus() != null ? item.getStatus().name() : ReminderStatus.PENDING.name());
        values.put(DatabaseHelper.COL_REMINDER_CATEGORY, item.getCategory());
        return db.update(DatabaseHelper.TABLE_REMINDERS, values,
                DatabaseHelper.COL_REMINDER_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(item.getId()), String.valueOf(getCurrentUserId())});
    }

    /**
     * Xóa một nhắc hẹn thanh toán.
     */
    public int deleteReminder(long id) {
        return db.delete(DatabaseHelper.TABLE_REMINDERS,
                DatabaseHelper.COL_REMINDER_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(id), String.valueOf(getCurrentUserId())});
    }

    /**
     * Đánh dấu nhắc hẹn là đã thanh toán (PAID).
     */
    public int markAsPaid(long id) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_REMINDER_STATUS, ReminderStatus.PAID.name());
        return db.update(DatabaseHelper.TABLE_REMINDERS, values,
                DatabaseHelper.COL_REMINDER_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(id), String.valueOf(getCurrentUserId())});
    }

    /**
     * Lấy nhắc hẹn theo ID.
     */
    public Reminder getReminderById(long id) {
        Reminder item = null;
        Cursor cursor = db.query(DatabaseHelper.TABLE_REMINDERS, null,
                DatabaseHelper.COL_REMINDER_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(id), String.valueOf(getCurrentUserId())}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            item = cursorToItem(cursor);
            cursor.close();
        }
        return item;
    }

    /**
     * Lấy danh sách tất cả nhắc hẹn thanh toán (xếp theo hạn gần nhất).
     */
    public List<Reminder> getAllReminders() {
        List<Reminder> list = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_REMINDERS, null,
                DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(getCurrentUserId())}, null, null,
                DatabaseHelper.COL_REMINDER_DUE_DATE + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    /**
     * Lấy tối đa N nhắc hẹn đang PENDING có hạn gần nhất.
     */
    public List<Reminder> getPendingReminders(int limit) {
        List<Reminder> list = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_REMINDERS, null,
                DatabaseHelper.COL_REMINDER_STATUS + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{ReminderStatus.PENDING.name(), String.valueOf(getCurrentUserId())}, null, null,
                DatabaseHelper.COL_REMINDER_DUE_DATE + " ASC",
                String.valueOf(limit));
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    private Reminder cursorToItem(Cursor cursor) {
        Reminder item = new Reminder();
        item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REMINDER_ID)));
        item.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REMINDER_TITLE)));
        item.setEstimatedAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REMINDER_AMOUNT)));
        item.setDueDateFromString(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REMINDER_DUE_DATE)));
        
        String recStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REMINDER_RECURRENCE));
        try {
            item.setRecurrence(Recurrence.valueOf(recStr));
        } catch (IllegalArgumentException e) {
            item.setRecurrence(Recurrence.MONTHLY);
        }

        item.setReminderOffsetDays(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REMINDER_OFFSET_DAYS)));

        String statusStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REMINDER_STATUS));
        try {
            item.setStatus(ReminderStatus.valueOf(statusStr));
        } catch (IllegalArgumentException e) {
            item.setStatus(ReminderStatus.PENDING);
        }

        item.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_REMINDER_CATEGORY)));
        item.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID_FK)));
        return item;
    }
}
