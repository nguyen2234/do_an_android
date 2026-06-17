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

    
    private long getCurrentUserId() {
        if (context == null) return 1;
        android.content.SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return prefs.getLong("user_id", 1);
    }

    private long getOrCreateCategoryId(String categoryName) {
        if (categoryName == null || categoryName.trim().isEmpty()) {
            return 0;
        }
        String name = categoryName.trim();
        long catId = 0;
        Cursor catCursor = db.rawQuery(
                "SELECT id FROM categories WHERE name = ? AND user_id = ?",
                new String[]{name, String.valueOf(getCurrentUserId())});
        if (catCursor != null && catCursor.moveToFirst()) {
            catId = catCursor.getLong(0);
        } else {
            Cursor catCursorFallback = db.rawQuery(
                    "SELECT id FROM categories WHERE name = ?",
                    new String[]{name});
            if (catCursorFallback != null && catCursorFallback.moveToFirst()) {
                catId = catCursorFallback.getLong(0);
            }
            if (catCursorFallback != null) catCursorFallback.close();
        }
        if (catCursor != null) catCursor.close();

        if (catId <= 0) {
            ContentValues catValues = new ContentValues();
            catValues.put("name", name);
            catValues.put("type", "expense"); 
            catValues.put("user_id", getCurrentUserId());
            catId = db.insert("categories", null, catValues);
        }
        return catId;
    }

    
    public long addReminder(Reminder item) {
        long catId = item.getCategoryId();
        if (catId <= 0 && item.getCategory() != null && !item.getCategory().isEmpty()) {
            catId = getOrCreateCategoryId(item.getCategory());
            item.setCategoryId(catId);
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_REMINDER_TITLE, item.getTitle());
        values.put(DatabaseHelper.COL_REMINDER_AMOUNT, item.getEstimatedAmount());
        values.put(DatabaseHelper.COL_REMINDER_DUE_DATE, item.getDueDateString());
        values.put(DatabaseHelper.COL_REMINDER_RECURRENCE, item.getRecurrence() != null ? item.getRecurrence().name() : Recurrence.MONTHLY.name());
        values.put(DatabaseHelper.COL_REMINDER_OFFSET_DAYS, item.getReminderOffsetDays());
        values.put(DatabaseHelper.COL_REMINDER_STATUS, item.getStatus() != null ? item.getStatus().name() : ReminderStatus.PENDING.name());
        values.put(DatabaseHelper.COL_REMINDER_CATEGORY_ID, catId > 0 ? catId : null);
        values.put(DatabaseHelper.COL_USER_ID_FK, getCurrentUserId());
        return db.insert(DatabaseHelper.TABLE_REMINDERS, null, values);
    }

    
    public int updateReminder(Reminder item) {
        long catId = item.getCategoryId();
        if (catId <= 0 && item.getCategory() != null && !item.getCategory().isEmpty()) {
            catId = getOrCreateCategoryId(item.getCategory());
            item.setCategoryId(catId);
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_REMINDER_TITLE, item.getTitle());
        values.put(DatabaseHelper.COL_REMINDER_AMOUNT, item.getEstimatedAmount());
        values.put(DatabaseHelper.COL_REMINDER_DUE_DATE, item.getDueDateString());
        values.put(DatabaseHelper.COL_REMINDER_RECURRENCE, item.getRecurrence() != null ? item.getRecurrence().name() : Recurrence.MONTHLY.name());
        values.put(DatabaseHelper.COL_REMINDER_OFFSET_DAYS, item.getReminderOffsetDays());
        values.put(DatabaseHelper.COL_REMINDER_STATUS, item.getStatus() != null ? item.getStatus().name() : ReminderStatus.PENDING.name());
        values.put(DatabaseHelper.COL_REMINDER_CATEGORY_ID, catId > 0 ? catId : null);
        return db.update(DatabaseHelper.TABLE_REMINDERS, values,
                DatabaseHelper.COL_REMINDER_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(item.getId()), String.valueOf(getCurrentUserId())});
    }

    
    public int deleteReminder(long id) {
        return db.delete(DatabaseHelper.TABLE_REMINDERS,
                DatabaseHelper.COL_REMINDER_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(id), String.valueOf(getCurrentUserId())});
    }

    
    public int markAsPaid(long id) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_REMINDER_STATUS, ReminderStatus.PAID.name());
        return db.update(DatabaseHelper.TABLE_REMINDERS, values,
                DatabaseHelper.COL_REMINDER_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(id), String.valueOf(getCurrentUserId())});
    }

    
    public Reminder getReminderById(long id) {
        Reminder item = null;
        String sql = "SELECT r.id, r.title, r.estimated_amount, r.due_date, r.recurrence, r.offset_days, r.status, r.category_id, c.name AS category_name, r.user_id " +
                "FROM reminders r " +
                "LEFT JOIN categories c ON r.category_id = c.id " +
                "WHERE r.id = ? AND r.user_id = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(id), String.valueOf(getCurrentUserId())});
        if (cursor != null && cursor.moveToFirst()) {
            item = cursorToItem(cursor);
            cursor.close();
        }
        return item;
    }

    
    public List<Reminder> getAllReminders() {
        List<Reminder> list = new ArrayList<>();
        String sql = "SELECT r.id, r.title, r.estimated_amount, r.due_date, r.recurrence, r.offset_days, r.status, r.category_id, c.name AS category_name, r.user_id " +
                "FROM reminders r " +
                "LEFT JOIN categories c ON r.category_id = c.id " +
                "WHERE r.user_id = ? " +
                "ORDER BY r.due_date ASC";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(getCurrentUserId())});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    
    public List<Reminder> getPendingReminders(int limit) {
        List<Reminder> list = new ArrayList<>();
        String sql = "SELECT r.id, r.title, r.estimated_amount, r.due_date, r.recurrence, r.offset_days, r.status, r.category_id, c.name AS category_name, r.user_id " +
                "FROM reminders r " +
                "LEFT JOIN categories c ON r.category_id = c.id " +
                "WHERE r.status = ? AND r.user_id = ? " +
                "ORDER BY r.due_date ASC " +
                "LIMIT ?";
        Cursor cursor = db.rawQuery(sql, new String[]{ReminderStatus.PENDING.name(), String.valueOf(getCurrentUserId()), String.valueOf(limit)});
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
        item.setId(cursor.getLong(0));
        item.setTitle(cursor.getString(1));
        item.setEstimatedAmount(cursor.getDouble(2));
        item.setDueDateFromString(cursor.getString(3));
        
        String recStr = cursor.getString(4);
        try {
            item.setRecurrence(Recurrence.valueOf(recStr));
        } catch (IllegalArgumentException e) {
            item.setRecurrence(Recurrence.MONTHLY);
        }

        item.setReminderOffsetDays(cursor.getInt(5));

        String statusStr = cursor.getString(6);
        try {
            item.setStatus(ReminderStatus.valueOf(statusStr));
        } catch (IllegalArgumentException e) {
            item.setStatus(ReminderStatus.PENDING);
        }

        item.setCategoryId(cursor.getLong(7));
        item.setCategory(cursor.getString(8) != null ? cursor.getString(8) : "Chưa phân loại");
        item.setUserId(cursor.getLong(9));
        return item;
    }
}
