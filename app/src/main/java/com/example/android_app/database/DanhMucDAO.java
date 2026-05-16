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

    public DanhMucDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
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
        values.put(DatabaseHelper.COL_CATEGORY_NOTE, category.getNote());

        return db.insert(DatabaseHelper.TABLE_CATEGORIES, null, values);
    }

    /**
     * Lấy danh sách danh mục theo loại (thu nhập hoặc chi tiêu).
     * @param type "income" hoặc "expense"
     * @return Danh sách danh mục
     */
    public List<DanhMuc> getCategoriesByType(String type) {
        List<DanhMuc> categories = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES, null, 
                DatabaseHelper.COL_CATEGORY_TYPE + " = ?", 
                new String[]{type}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                DanhMuc c = new DanhMuc();
                c.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_ID)));
                c.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_NAME)));
                c.setIcon(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_ICON)));
                c.setLoai(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_TYPE)));
                c.setColor(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_COLOR)));
                int noteIndex = cursor.getColumnIndex(DatabaseHelper.COL_CATEGORY_NOTE);
                if (noteIndex != -1) {
                    c.setNote(cursor.getString(noteIndex));
                }
                
                categories.add(c);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return categories;
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
        values.put(DatabaseHelper.COL_CATEGORY_NOTE, category.getNote());

        return db.update(DatabaseHelper.TABLE_CATEGORIES, values, 
                DatabaseHelper.COL_CATEGORY_ID + " = ?", 
                new String[]{String.valueOf(category.getId())});
    }

    /**
     * Xóa danh mục
     */
    public int deleteCategory(long id) {
        return db.delete(DatabaseHelper.TABLE_CATEGORIES, 
                DatabaseHelper.COL_CATEGORY_ID + " = ?", 
                new String[]{String.valueOf(id)});
    }

    /**
     * Lấy tất cả danh mục
     */
    public List<DanhMuc> getAllCategories() {
        List<DanhMuc> categories = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES, null, 
                null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                DanhMuc c = new DanhMuc();
                c.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_ID)));
                c.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_NAME)));
                c.setIcon(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_ICON)));
                c.setLoai(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_TYPE)));
                c.setColor(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_COLOR)));
                int noteIndex = cursor.getColumnIndex(DatabaseHelper.COL_CATEGORY_NOTE);
                if (noteIndex != -1) {
                    c.setNote(cursor.getString(noteIndex));
                }
                categories.add(c);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return categories;
    }
}
