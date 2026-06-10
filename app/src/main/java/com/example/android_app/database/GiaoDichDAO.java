package com.example.android_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.android_app.model.GiaoDich;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) cho thao tác với bảng Giao dịch (Transactions).
 */
public class GiaoDichDAO {

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;
    private Context context;

    public GiaoDichDAO(Context context) {
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
     * Thêm một giao dịch mới.
     * @param transaction Giao dịch cần thêm
     * @return ID của giao dịch vừa thêm, hoặc -1 nếu lỗi
     */
    public long addTransaction(GiaoDich transaction) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_TRANS_TITLE, transaction.getTitle());
        values.put(DatabaseHelper.COL_TRANS_AMOUNT, transaction.getSoTien());
        values.put(DatabaseHelper.COL_TRANS_CATEGORY, transaction.getCategory());
        values.put(DatabaseHelper.COL_TRANS_TYPE, transaction.getLoai());
        values.put(DatabaseHelper.COL_TRANS_DATE, transaction.getNgay());
        values.put(DatabaseHelper.COL_TRANS_NOTE, transaction.getNote());
        values.put(DatabaseHelper.COL_TRANS_WALLET_ID, transaction.getWalletId());
        values.put(DatabaseHelper.COL_USER_ID_FK, getCurrentUserId());

        return db.insert(DatabaseHelper.TABLE_TRANSACTIONS, null, values);
    }

    /**
     * Lấy danh sách tất cả các giao dịch, sắp xếp theo ngày.
     * @return Danh sách giao dịch
     */
    public List<GiaoDich> getAllTransactions() {
        List<GiaoDich> transactions = new ArrayList<>();
        // Truy vấn sắp xếp theo ID giảm dần (mới nhất lên trên) - Thực tế nên sắp xếp theo DATE
        Cursor cursor = db.query(DatabaseHelper.TABLE_TRANSACTIONS, null,
                DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(getCurrentUserId())}, null, null, DatabaseHelper.COL_TRANS_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                GiaoDich t = new GiaoDich();
                t.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_ID)));
                t.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_TITLE)));
                t.setSoTien(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_AMOUNT)));
                t.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_CATEGORY)));
                t.setLoai(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_TYPE)));
                t.setNgay(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_DATE)));
                t.setNote(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_NOTE)));
                t.setWalletId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_WALLET_ID)));
                
                transactions.add(t);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return transactions;
    }

    /**
     * Lấy danh sách giao dịch dựa trên từ khóa tìm kiếm và khoảng thời gian.
     */
    public List<GiaoDich> getTransactionsByFilter(String keyword, String startDate, String endDate) {
        List<GiaoDich> transactions = new ArrayList<>();
        
        StringBuilder selection = new StringBuilder(DatabaseHelper.COL_USER_ID_FK + " = ?");
        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(String.valueOf(getCurrentUserId()));

        if (keyword != null && !keyword.trim().isEmpty()) {
            selection.append(" AND (").append(DatabaseHelper.COL_TRANS_NOTE).append(" LIKE ? OR ")
                     .append(DatabaseHelper.COL_TRANS_TITLE).append(" LIKE ?)");
            selectionArgs.add("%" + keyword.trim() + "%");
            selectionArgs.add("%" + keyword.trim() + "%");
        }

        Cursor cursor = db.query(DatabaseHelper.TABLE_TRANSACTIONS, null, 
                selection.toString(), selectionArgs.toArray(new String[0]), 
                null, null, DatabaseHelper.COL_TRANS_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                GiaoDich t = new GiaoDich();
                t.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_ID)));
                t.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_TITLE)));
                t.setSoTien(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_AMOUNT)));
                t.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_CATEGORY)));
                t.setLoai(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_TYPE)));
                t.setNgay(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_DATE)));
                t.setNote(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_NOTE)));
                t.setWalletId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_WALLET_ID)));
                
                transactions.add(t);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return transactions;
    }

    /**
     * Lấy danh sách giao dịch gần đây với số lượng giới hạn.
     */
    public List<GiaoDich> getRecentTransactions(int limit) {
        List<GiaoDich> transactions = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_TRANSACTIONS, null,
                DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(getCurrentUserId())}, null, null, DatabaseHelper.COL_TRANS_ID + " DESC", String.valueOf(limit));

        if (cursor != null && cursor.moveToFirst()) {
            do {
                GiaoDich t = new GiaoDich();
                t.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_ID)));
                t.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_TITLE)));
                t.setSoTien(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_AMOUNT)));
                t.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_CATEGORY)));
                t.setLoai(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_TYPE)));
                t.setNgay(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_DATE)));
                t.setNote(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_NOTE)));
                t.setWalletId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_WALLET_ID)));
                
                transactions.add(t);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return transactions;
    }
}
