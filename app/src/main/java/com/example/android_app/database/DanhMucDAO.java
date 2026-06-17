package com.example.android_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.android_app.model.DanhMuc;

import java.util.ArrayList;
import java.util.List;


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

    
    private long getCurrentUserId() {
        if (context == null) return 1;
        android.content.SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return prefs.getLong("user_id", 1);
    }

    
    public long addCategory(DanhMuc category) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_CATEGORY_NAME, category.getName());
        values.put(DatabaseHelper.COL_CATEGORY_ICON, category.getIcon());
        values.put(DatabaseHelper.COL_CATEGORY_TYPE, category.getLoai());
        values.put(DatabaseHelper.COL_CATEGORY_COLOR, category.getColor());
        values.put(DatabaseHelper.COL_USER_ID_FK, getCurrentUserId());

        return db.insert(DatabaseHelper.TABLE_CATEGORIES, null, values);
    }

    
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

    
    public int deleteCategory(long id) {
        return db.delete(DatabaseHelper.TABLE_CATEGORIES,
                DatabaseHelper.COL_CATEGORY_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(id), String.valueOf(getCurrentUserId())});
    }

    
    public boolean isCategoryUsed(long categoryId) {
        Cursor cursor;

        
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_BUDGET_CATEGORIES +
                " WHERE " + DatabaseHelper.COL_BC_CATEGORY_ID + " = ?", new String[]{String.valueOf(categoryId)});
        if (cursor != null) {
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }

        
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_TRANSACTIONS +
                " WHERE " + DatabaseHelper.COL_TRANS_CATEGORY_ID + " = ?", new String[]{String.valueOf(categoryId)});
        if (cursor != null) {
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }

        
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_PLANNED +
                " WHERE " + DatabaseHelper.COL_PLANNED_CATEGORY_ID + " = ?", new String[]{String.valueOf(categoryId)});
        if (cursor != null) {
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }

        
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_REMINDERS +
                " WHERE " + DatabaseHelper.COL_REMINDER_CATEGORY_ID + " = ?", new String[]{String.valueOf(categoryId)});
        if (cursor != null) {
            if (cursor.moveToFirst() && cursor.getInt(0) > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }

        return false;
    }

    
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
