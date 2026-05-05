package com.example.android_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.android_app.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) cho thao tác với bảng Danh mục (Categories).
 */
public class CategoryDAO {

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public CategoryDAO(Context context) {
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
    public long addCategory(Category category) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_CATEGORY_NAME, category.getName());
        values.put(DatabaseHelper.COL_CATEGORY_ICON, category.getIcon());
        values.put(DatabaseHelper.COL_CATEGORY_TYPE, category.getType());
        values.put(DatabaseHelper.COL_CATEGORY_COLOR, category.getColor());

        return db.insert(DatabaseHelper.TABLE_CATEGORIES, null, values);
    }

    /**
     * Lấy danh sách danh mục theo loại (thu nhập hoặc chi tiêu).
     * @param type "income" hoặc "expense"
     * @return Danh sách danh mục
     */
    public List<Category> getCategoriesByType(String type) {
        List<Category> categories = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_CATEGORIES, null, 
                DatabaseHelper.COL_CATEGORY_TYPE + " = ?", 
                new String[]{type}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Category c = new Category();
                c.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_ID)));
                c.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_NAME)));
                c.setIcon(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_ICON)));
                c.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_TYPE)));
                c.setColor(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CATEGORY_COLOR)));
                
                categories.add(c);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return categories;
    }
}
