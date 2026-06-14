package com.example.android_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.android_app.model.DanhMuc;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) cho thao tác với bảng Danh mục (Categories).
 */
public class DanhMucDAO {

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private Context context;

    public DanhMucDAO(Context context) {
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
     * Thêm danh mục mới.
     * @param category Danh mục cần thêm
     * @return ID của danh mục, hoặc -1 nếu lỗi
     */
    public long addCategory(DanhMuc category) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_CATEGORY_NAME, category.getName());
        values.put(DatabaseHelper.COL_CATEGORY_ICON, category.getIcon());
        values.put(DatabaseHelper.COL_CATEGORY_TYPE, category.getLoai());
        values.put(DatabaseHelper.COL_CATEGORY_COLOR, category.getColor());
        values.put(DatabaseHelper.COL_USER_ID_FK, getCurrentUserId());

        return db.insert(DatabaseHelper.TABLE_CATEGORIES, null, values);
    }

    /**
     * Cập nhật danh mục
     */
    public int updateCategory(DanhMuc category) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_CATEGORY_NAME, category.getName());
        values.put(DatabaseHelper.COL_CATEGORY_ICON, category.getIcon());
        values.put(DatabaseHelper.COL_CATEGORY_TYPE, category.getLoai());
        values.put(DatabaseHelper.COL_CATEGORY_COLOR, category.getColor());

        return db.update(DatabaseHelper.TABLE_CATEGORIES, values,
                DatabaseHelper.COL_CATEGORY_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(category.getId()), String.valueOf(getCurrentUserId())});
    }

    /**
     * Xóa danh mục
     */
    public int deleteCategory(long id) {
        return db.delete(DatabaseHelper.TABLE_CATEGORIES,
                DatabaseHelper.COL_CATEGORY_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(id), String.valueOf(getCurrentUserId())});
    }

    /**
     * Lấy danh sách danh mục theo loại (thu nhập hoặc chi tiêu).
     * Giữ lại để tương thích với code cũ (NganSach module dùng "expense").
     * @param type "income" hoặc "expense"
     * @return Danh sách danh mục
     */
    public List<DanhMuc> getCategoriesByType(String type) {
        List<DanhMuc> categories = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES, null,
                DatabaseHelper.COL_CATEGORY_TYPE + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{type, String.valueOf(getCurrentUserId())}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                categories.add(mapCursorToCategory(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return categories;
    }

    /**
     * Lấy tất cả danh mục (không phân biệt loại).
     * Đây là phương thức chính sau khi danh mục trở thành CHUNG.
     * @return Danh sách tất cả danh mục của user
     */
    public List<DanhMuc> getAllCategories() {
        List<DanhMuc> categories = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES, null,
                DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(getCurrentUserId())}, null, null,
                DatabaseHelper.COL_CATEGORY_NAME + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                categories.add(mapCursorToCategory(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return categories;
    }

    /**
     * Lấy số lượng danh mục hiện có của người dùng.
     * Dùng trong Onboarding để kiểm tra điều kiện enable nút "Tiếp tục".
     * @return Số lượng danh mục (>= 0)
     */
    public int getCategoryCount() {
        Cursor cursor = db.rawQuery(
                "SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_CATEGORIES +
                " WHERE " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(getCurrentUserId())});
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    /**
     * Helper: Ánh xạ Cursor thành đối tượng DanhMuc.
     */
    private DanhMuc mapCursorToCategory(Cursor cursor) {
        DanhMuc c = new DanhMuc();
        c.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_ID)));
        c.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_NAME)));
        c.setIcon(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_ICON)));
        c.setLoai(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_TYPE)));
        c.setColor(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_COLOR)));
        return c;
    }
}
