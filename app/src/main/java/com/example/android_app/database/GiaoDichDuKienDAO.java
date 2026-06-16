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

    private long getOrCreateCategoryId(String categoryName, String type) {
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
            catValues.put("type", type != null ? type : "expense");
            catValues.put("user_id", getCurrentUserId());
            catId = db.insert("categories", null, catValues);
        }
        return catId;
    }

    /**
     * Thêm một giao dịch dự kiến mới.
     */
    public long addPlanned(GiaoDichDuKien item) {
        long catId = item.getCategoryId();
        if (catId <= 0 && item.getCategory() != null && !item.getCategory().isEmpty()) {
            catId = getOrCreateCategoryId(item.getCategory(), item.getType());
            item.setCategoryId(catId);
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_PLANNED_TITLE, item.getTitle());
        values.put(DatabaseHelper.COL_PLANNED_AMOUNT, item.getAmount());
        values.put(DatabaseHelper.COL_PLANNED_CATEGORY_ID, catId > 0 ? catId : null);
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
        long catId = item.getCategoryId();
        if (catId <= 0 && item.getCategory() != null && !item.getCategory().isEmpty()) {
            catId = getOrCreateCategoryId(item.getCategory(), item.getType());
            item.setCategoryId(catId);
        }

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_PLANNED_TITLE, item.getTitle());
        values.put(DatabaseHelper.COL_PLANNED_AMOUNT, item.getAmount());
        values.put(DatabaseHelper.COL_PLANNED_CATEGORY_ID, catId > 0 ? catId : null);
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
        String sql = "SELECT p.id, p.title, p.amount, p.category_id, c.name AS category_name, p.type, p.due_date, p.status, p.note, p.wallet_id " +
                "FROM planned_transactions p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "WHERE p.status = ? AND p.user_id = ? " +
                "ORDER BY p.due_date ASC";
        Cursor cursor = db.rawQuery(sql, new String[]{"pending", String.valueOf(getCurrentUserId())});
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
        String sql = "SELECT p.id, p.title, p.amount, p.category_id, c.name AS category_name, p.type, p.due_date, p.status, p.note, p.wallet_id " +
                "FROM planned_transactions p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "WHERE p.user_id = ? " +
                "ORDER BY p.due_date ASC";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(getCurrentUserId())});
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
        String sql = "SELECT p.id, p.title, p.amount, p.category_id, c.name AS category_name, p.type, p.due_date, p.status, p.note, p.wallet_id " +
                "FROM planned_transactions p " +
                "LEFT JOIN categories c ON p.category_id = c.id " +
                "WHERE p.status = 'pending' AND (p.due_date = ? OR p.due_date = ?) AND p.user_id = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{today, tomorrow, String.valueOf(getCurrentUserId())});
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
        item.setId(cursor.getLong(0));
        item.setTitle(cursor.getString(1));
        item.setAmount(cursor.getDouble(2));
        item.setCategoryId(cursor.getLong(3));
        item.setCategory(cursor.getString(4) != null ? cursor.getString(4) : "Chưa phân loại");
        item.setType(cursor.getString(5));
        item.setDueDate(cursor.getString(6));
        item.setStatus(cursor.getString(7));
        item.setNote(cursor.getString(8));
        item.setWalletId(cursor.getLong(9));
        return item;
    }
}
