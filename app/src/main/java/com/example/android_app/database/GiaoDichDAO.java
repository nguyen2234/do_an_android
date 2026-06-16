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
        
        long catId = transaction.getCategoryId();
        if (catId <= 0 && transaction.getCategory() != null && !transaction.getCategory().isEmpty()) {
            Cursor catCursor = db.rawQuery(
                    "SELECT id FROM categories WHERE name = ? AND user_id = ?",
                    new String[]{transaction.getCategory().trim(), String.valueOf(getCurrentUserId())});
            if (catCursor != null && catCursor.moveToFirst()) {
                catId = catCursor.getLong(0);
            } else {
                Cursor catCursorFallback = db.rawQuery(
                        "SELECT id FROM categories WHERE name = ?",
                        new String[]{transaction.getCategory().trim()});
                if (catCursorFallback != null && catCursorFallback.moveToFirst()) {
                    catId = catCursorFallback.getLong(0);
                }
                if (catCursorFallback != null) catCursorFallback.close();
            }
            if (catCursor != null) catCursor.close();

            if (catId <= 0) {
                ContentValues catValues = new ContentValues();
                catValues.put("name", transaction.getCategory().trim());
                catValues.put("type", transaction.getLoai() != null ? transaction.getLoai() : "expense");
                catValues.put("user_id", getCurrentUserId());
                catId = db.insert("categories", null, catValues);
            }
        }
        
        values.put(DatabaseHelper.COL_TRANS_CATEGORY_ID, catId > 0 ? catId : null);
        values.put(DatabaseHelper.COL_TRANS_TYPE, transaction.getLoai());
        values.put(DatabaseHelper.COL_TRANS_DATE, transaction.getNgay());
        values.put(DatabaseHelper.COL_TRANS_NOTE, transaction.getNote());
        values.put(DatabaseHelper.COL_TRANS_WALLET_ID, transaction.getWalletId());
        values.put(DatabaseHelper.COL_USER_ID_FK, getCurrentUserId());

        return db.insert(DatabaseHelper.TABLE_TRANSACTIONS, null, values);
    }

    /**
     * Lấy danh sách tất cả các giao dịch, sắp xếp theo ID giảm dần (mới nhất lên trên).
     * @return Danh sách giao dịch
     */
    public List<GiaoDich> getAllTransactions() {
        List<GiaoDich> transactions = new ArrayList<>();
        String sql = "SELECT t.id, t.title, t.amount, t.category_id, c.name AS category_name, t.type, t.date, t.note, t.wallet_id " +
                "FROM transactions t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE t.user_id = ? " +
                "ORDER BY t.id DESC";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(getCurrentUserId())});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                GiaoDich t = new GiaoDich();
                t.setId(cursor.getLong(0));
                t.setTitle(cursor.getString(1));
                t.setSoTien(cursor.getDouble(2));
                t.setCategoryId(cursor.getLong(3));
                t.setCategory(cursor.getString(4) != null ? cursor.getString(4) : "Chưa phân loại");
                t.setLoai(cursor.getString(5));
                t.setNgay(cursor.getString(6));
                t.setNote(cursor.getString(7));
                t.setWalletId(cursor.getLong(8));
                
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
        
        StringBuilder sql = new StringBuilder(
                "SELECT t.id, t.title, t.amount, t.category_id, c.name AS category_name, t.type, t.date, t.note, t.wallet_id " +
                "FROM transactions t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE t.user_id = ?"
        );
        List<String> selectionArgs = new ArrayList<>();
        selectionArgs.add(String.valueOf(getCurrentUserId()));

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (t.note LIKE ? OR t.title LIKE ?)");
            selectionArgs.add("%" + keyword.trim() + "%");
            selectionArgs.add("%" + keyword.trim() + "%");
        }
        
        sql.append(" ORDER BY t.id DESC");

        Cursor cursor = db.rawQuery(sql.toString(), selectionArgs.toArray(new String[0]));

        if (cursor != null && cursor.moveToFirst()) {
            do {
                GiaoDich t = new GiaoDich();
                t.setId(cursor.getLong(0));
                t.setTitle(cursor.getString(1));
                t.setSoTien(cursor.getDouble(2));
                t.setCategoryId(cursor.getLong(3));
                t.setCategory(cursor.getString(4) != null ? cursor.getString(4) : "Chưa phân loại");
                t.setLoai(cursor.getString(5));
                t.setNgay(cursor.getString(6));
                t.setNote(cursor.getString(7));
                t.setWalletId(cursor.getLong(8));
                
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
        String sql = "SELECT t.id, t.title, t.amount, t.category_id, c.name AS category_name, t.type, t.date, t.note, t.wallet_id " +
                "FROM transactions t " +
                "LEFT JOIN categories c ON t.category_id = c.id " +
                "WHERE t.user_id = ? " +
                "ORDER BY t.id DESC " +
                "LIMIT ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(getCurrentUserId()), String.valueOf(limit)});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                GiaoDich t = new GiaoDich();
                t.setId(cursor.getLong(0));
                t.setTitle(cursor.getString(1));
                t.setSoTien(cursor.getDouble(2));
                t.setCategoryId(cursor.getLong(3));
                t.setCategory(cursor.getString(4) != null ? cursor.getString(4) : "Chưa phân loại");
                t.setLoai(cursor.getString(5));
                t.setNgay(cursor.getString(6));
                t.setNote(cursor.getString(7));
                t.setWalletId(cursor.getLong(8));
                
                transactions.add(t);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return transactions;
    }
}
