package com.example.android_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.android_app.model.GiaoDichDuKien;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object cho bảng Giao dịch Dự kiến (planned_transactions).
 */
public class GiaoDichDuKienDAO {

    private SQLiteDatabase db;
    private final DatabaseHelper dbHelper;
    private Context context;

    public GiaoDichDuKienDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
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
     * Thêm một giao dịch dự kiến mới.
     */
    public long addPlanned(GiaoDichDuKien item) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_PLANNED_TITLE, item.getTitle());
        values.put(DatabaseHelper.COL_PLANNED_AMOUNT, item.getAmount());
        values.put(DatabaseHelper.COL_PLANNED_CATEGORY, item.getCategory());
        values.put(DatabaseHelper.COL_PLANNED_TYPE, item.getType());
        values.put(DatabaseHelper.COL_PLANNED_DUE_DATE, item.getDueDate());
        values.put(DatabaseHelper.COL_PLANNED_STATUS, item.getStatus() != null ? item.getStatus() : "pending");
        values.put(DatabaseHelper.COL_PLANNED_NOTE, item.getNote());
        values.put(DatabaseHelper.COL_PLANNED_WALLET_ID, item.getWalletId());
        values.put(DatabaseHelper.COL_USER_ID_FK, getCurrentUserId());
        return db.insert(DatabaseHelper.TABLE_PLANNED, null, values);
    }

    /**
     * Cập nhật giao dịch dự kiến.
     */
    public int updatePlanned(GiaoDichDuKien item) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_PLANNED_TITLE, item.getTitle());
        values.put(DatabaseHelper.COL_PLANNED_AMOUNT, item.getAmount());
        values.put(DatabaseHelper.COL_PLANNED_CATEGORY, item.getCategory());
        values.put(DatabaseHelper.COL_PLANNED_TYPE, item.getType());
        values.put(DatabaseHelper.COL_PLANNED_DUE_DATE, item.getDueDate());
        values.put(DatabaseHelper.COL_PLANNED_STATUS, item.getStatus());
        values.put(DatabaseHelper.COL_PLANNED_NOTE, item.getNote());
        values.put(DatabaseHelper.COL_PLANNED_WALLET_ID, item.getWalletId());
        return db.update(DatabaseHelper.TABLE_PLANNED, values,
                DatabaseHelper.COL_PLANNED_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(item.getId()), String.valueOf(getCurrentUserId())});
    }

    /**
     * Xóa một giao dịch dự kiến.
     */
    public int deletePlanned(long id) {
        return db.delete(DatabaseHelper.TABLE_PLANNED,
                DatabaseHelper.COL_PLANNED_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(id), String.valueOf(getCurrentUserId())});
    }

    /**
     * Đánh dấu giao dịch dự kiến là hoàn thành.
     */
    public int markCompleted(long id) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_PLANNED_STATUS, "completed");
        return db.update(DatabaseHelper.TABLE_PLANNED, values,
                DatabaseHelper.COL_PLANNED_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(id), String.valueOf(getCurrentUserId())});
    }

    /**
     * Lấy tất cả giao dịch dự kiến đang pending.
     */
    public List<GiaoDichDuKien> getAllPending() {
        List<GiaoDichDuKien> list = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLANNED, null,
                DatabaseHelper.COL_PLANNED_STATUS + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{"pending", String.valueOf(getCurrentUserId())}, null, null,
                DatabaseHelper.COL_PLANNED_DUE_DATE + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    /**
     * Lấy tất cả giao dịch dự kiến (mọi trạng thái).
     */
    public List<GiaoDichDuKien> getAll() {
        List<GiaoDichDuKien> list = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLANNED, null,
                DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(getCurrentUserId())}, null, null,
                DatabaseHelper.COL_PLANNED_DUE_DATE + " ASC");
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    /**
     * Lấy các khoản đến hạn hôm nay hoặc ngày mai (dùng cho Worker nhắc nhở).
     * @param today Ngày hôm nay theo định dạng dd/MM/yyyy
     * @param tomorrow Ngày mai theo định dạng dd/MM/yyyy
     */
    public List<GiaoDichDuKien> getDueSoon(String today, String tomorrow) {
        List<GiaoDichDuKien> list = new ArrayList<>();
        String where = DatabaseHelper.COL_PLANNED_STATUS + " = 'pending' AND (" +
                DatabaseHelper.COL_PLANNED_DUE_DATE + " = ? OR " +
                DatabaseHelper.COL_PLANNED_DUE_DATE + " = ?) AND " +
                DatabaseHelper.COL_USER_ID_FK + " = ?";
        Cursor cursor = db.query(DatabaseHelper.TABLE_PLANNED, null, where,
                new String[]{today, tomorrow, String.valueOf(getCurrentUserId())}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(cursorToItem(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    private GiaoDichDuKien cursorToItem(Cursor cursor) {
        GiaoDichDuKien item = new GiaoDichDuKien();
        item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PLANNED_ID)));
        item.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PLANNED_TITLE)));
        item.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PLANNED_AMOUNT)));
        item.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PLANNED_CATEGORY)));
        item.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PLANNED_TYPE)));
        item.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PLANNED_DUE_DATE)));
        item.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PLANNED_STATUS)));
        item.setNote(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_PLANNED_NOTE)));
        int walletIdx = cursor.getColumnIndex(DatabaseHelper.COL_PLANNED_WALLET_ID);
        if (walletIdx != -1) item.setWalletId(cursor.getLong(walletIdx));
        return item;
    }
}
