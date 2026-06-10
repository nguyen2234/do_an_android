package com.example.android_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.android_app.model.NganSach;
import java.util.ArrayList;
import java.util.List;

public class NganSachDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private Context context;

    public NganSachDAO(Context context) {
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

    public long addNganSach(NganSach budget) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_BUDGET_NAME, budget.getName());
        values.put(DatabaseHelper.COL_BUDGET_AMOUNT, budget.getAmount());
        values.put(DatabaseHelper.COL_BUDGET_SPENT, budget.getSpentAmount());
        values.put(DatabaseHelper.COL_BUDGET_START, budget.getStartDate());
        values.put(DatabaseHelper.COL_BUDGET_END, budget.getEndDate());
        values.put(DatabaseHelper.COL_BUDGET_CATEGORIES, budget.getCategoryIds());
        values.put(DatabaseHelper.COL_USER_ID_FK, getCurrentUserId());

        return db.insert(DatabaseHelper.TABLE_BUDGETS, null, values);
    }

    public int updateSpentAmount(int id, double newSpentAmount) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_BUDGET_SPENT, newSpentAmount);
        return db.update(DatabaseHelper.TABLE_BUDGETS, values,
                DatabaseHelper.COL_BUDGET_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(id), String.valueOf(getCurrentUserId())});
    }

    public List<NganSach> getAllBudgets() {
        List<NganSach> list = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BUDGETS, null,
                DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(getCurrentUserId())}, null, null, DatabaseHelper.COL_BUDGET_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                NganSach b = new NganSach();
                b.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_ID)));
                b.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_NAME)));
                b.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_AMOUNT)));
                b.setSpentAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_SPENT)));
                b.setStartDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_START)));
                b.setEndDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_END)));
                b.setCategoryIds(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_CATEGORIES)));
                list.add(b);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }
}
