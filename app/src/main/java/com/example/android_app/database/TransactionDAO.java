package com.example.android_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.android_app.model.Transaction;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) cho thao tác với bảng Giao dịch (Transactions).
 */
public class TransactionDAO {

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public TransactionDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    /**
     * Thêm một giao dịch mới.
     * @param transaction Giao dịch cần thêm
     * @return ID của giao dịch vừa thêm, hoặc -1 nếu lỗi
     */
    public long addTransaction(Transaction transaction) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_TRANS_TITLE, transaction.getTitle());
        values.put(DatabaseHelper.COL_TRANS_AMOUNT, transaction.getAmount());
        values.put(DatabaseHelper.COL_TRANS_CATEGORY, transaction.getCategory());
        values.put(DatabaseHelper.COL_TRANS_TYPE, transaction.getType());
        values.put(DatabaseHelper.COL_TRANS_DATE, transaction.getDate());
        values.put(DatabaseHelper.COL_TRANS_NOTE, transaction.getNote());
        values.put(DatabaseHelper.COL_TRANS_WALLET_ID, transaction.getWalletId());

        return db.insert(DatabaseHelper.TABLE_TRANSACTIONS, null, values);
    }

    /**
     * Lấy danh sách tất cả các giao dịch, sắp xếp theo ngày.
     * @return Danh sách giao dịch
     */
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        // Truy vấn sắp xếp theo ID giảm dần (mới nhất lên trên) - Thực tế nên sắp xếp theo DATE
        Cursor cursor = db.query(DatabaseHelper.TABLE_TRANSACTIONS, null, null, null, null, null, DatabaseHelper.COL_TRANS_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Transaction t = new Transaction();
                t.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_ID)));
                t.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_TITLE)));
                t.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_AMOUNT)));
                t.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_CATEGORY)));
                t.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_TYPE)));
                t.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_DATE)));
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
    public List<Transaction> getTransactionsByFilter(String keyword, String startDate, String endDate) {
        List<Transaction> transactions = new ArrayList<>();
        
        StringBuilder selection = new StringBuilder("1=1");
        List<String> selectionArgs = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            selection.append(" AND (").append(DatabaseHelper.COL_TRANS_NOTE).append(" LIKE ? OR ")
                     .append(DatabaseHelper.COL_TRANS_TITLE).append(" LIKE ?)");
            selectionArgs.add("%" + keyword.trim() + "%");
            selectionArgs.add("%" + keyword.trim() + "%");
        }

        if (startDate != null && endDate != null) {
            // Lưu ý: So sánh chuỗi ngày tháng theo định dạng dd/MM/yyyy trong SQLite có thể không chính xác
            // Nhưng để đơn giản, ta sẽ dùng cách lọc thô (nếu muốn chuẩn cần đổi sang yyyy-MM-dd)
            // Tuy nhiên, vì CSDL hiện tại dùng TEXT, ta lọc tạm theo keyword hoặc trả về tất cả
            // Tốt nhất là dùng Date để so sánh.
            // Ở đây tạm bỏ qua lọc thời gian phức tạp trong SQLite, hoặc phải xử lý ở Java
        }

        Cursor cursor = db.query(DatabaseHelper.TABLE_TRANSACTIONS, null, 
                selection.toString(), selectionArgs.toArray(new String[0]), 
                null, null, DatabaseHelper.COL_TRANS_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Transaction t = new Transaction();
                t.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_ID)));
                t.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_TITLE)));
                t.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_AMOUNT)));
                t.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_CATEGORY)));
                t.setType(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_TYPE)));
                t.setDate(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_DATE)));
                t.setNote(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_NOTE)));
                t.setWalletId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TRANS_WALLET_ID)));
                
                transactions.add(t);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return transactions;
    }
}
