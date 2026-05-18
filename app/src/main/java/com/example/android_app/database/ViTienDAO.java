package com.example.android_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.android_app.model.ViTien;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) cho thao tác với bảng Ví (Wallets).
 */
public class ViTienDAO {

    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public ViTienDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Mở kết nối cơ sở dữ liệu
    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    // Đóng kết nối
    public void close() {
        dbHelper.close();
    }

    /**
     * Thêm một ví mới vào cơ sở dữ liệu.
     */
    public long addWallet(ViTien wallet) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_WALLET_NAME, wallet.getName());
        values.put(DatabaseHelper.COL_WALLET_BALANCE, wallet.getBalance());
        values.put(DatabaseHelper.COL_WALLET_TYPE, wallet.getLoai());
        values.put(DatabaseHelper.COL_WALLET_CURRENCY, wallet.getCurrency());
        values.put(DatabaseHelper.COL_WALLET_ICON, wallet.getIcon());
        values.put(DatabaseHelper.COL_WALLET_COLOR, wallet.getColor());
        values.put(DatabaseHelper.COL_WALLET_MIN_BALANCE, wallet.getMinBalance());

        return db.insert(DatabaseHelper.TABLE_WALLETS, null, values);
    }

    /**
     * Cập nhật thông tin một ví đã có.
     */
    public int updateWallet(ViTien wallet) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_WALLET_NAME, wallet.getName());
        values.put(DatabaseHelper.COL_WALLET_BALANCE, wallet.getBalance());
        values.put(DatabaseHelper.COL_WALLET_TYPE, wallet.getLoai());
        values.put(DatabaseHelper.COL_WALLET_CURRENCY, wallet.getCurrency());
        values.put(DatabaseHelper.COL_WALLET_ICON, wallet.getIcon());
        values.put(DatabaseHelper.COL_WALLET_COLOR, wallet.getColor());
        values.put(DatabaseHelper.COL_WALLET_MIN_BALANCE, wallet.getMinBalance());

        return db.update(DatabaseHelper.TABLE_WALLETS, values,
                DatabaseHelper.COL_WALLET_ID + " = ?",
                new String[]{String.valueOf(wallet.getId())});
    }

    /**
     * Xóa một ví khỏi cơ sở dữ liệu.
     */
    public int deleteWallet(long id) {
        return db.delete(DatabaseHelper.TABLE_WALLETS,
                DatabaseHelper.COL_WALLET_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    /**
     * Lấy danh sách tất cả các ví.
     */
    public List<ViTien> getAllWallets() {
        List<ViTien> wallets = new ArrayList<>();
        // Truy vấn tất cả dữ liệu từ bảng ví
        Cursor cursor = db.query(DatabaseHelper.TABLE_WALLETS, null, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                ViTien wallet = new ViTien();
                wallet.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WALLET_ID)));
                wallet.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WALLET_NAME)));
                wallet.setBalance(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WALLET_BALANCE)));
                wallet.setLoai(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WALLET_TYPE)));
                wallet.setCurrency(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WALLET_CURRENCY)));
                
                int iconIndex = cursor.getColumnIndex(DatabaseHelper.COL_WALLET_ICON);
                if (iconIndex != -1 && !cursor.isNull(iconIndex)) {
                    wallet.setIcon(cursor.getString(iconIndex));
                } else {
                    wallet.setIcon("cash");
                }

                int colorIndex = cursor.getColumnIndex(DatabaseHelper.COL_WALLET_COLOR);
                if (colorIndex != -1 && !cursor.isNull(colorIndex)) {
                    wallet.setColor(cursor.getString(colorIndex));
                } else {
                    wallet.setColor("#4CAF50");
                }

                int minBalIdx = cursor.getColumnIndex(DatabaseHelper.COL_WALLET_MIN_BALANCE);
                if (minBalIdx != -1) {
                    wallet.setMinBalance(cursor.getDouble(minBalIdx));
                }

                wallets.add(wallet);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return wallets;
    }

    /**
     * Lấy thông tin một ví theo ID.
     */
    public ViTien getWalletById(long id) {
        Cursor cursor = db.query(DatabaseHelper.TABLE_WALLETS, null,
                DatabaseHelper.COL_WALLET_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            ViTien wallet = new ViTien();
            wallet.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WALLET_ID)));
            wallet.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WALLET_NAME)));
            wallet.setBalance(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WALLET_BALANCE)));
            wallet.setLoai(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WALLET_TYPE)));
            wallet.setCurrency(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WALLET_CURRENCY)));
            int iconIdx = cursor.getColumnIndex(DatabaseHelper.COL_WALLET_ICON);
            if (iconIdx != -1) wallet.setIcon(cursor.getString(iconIdx));
            int colorIdx = cursor.getColumnIndex(DatabaseHelper.COL_WALLET_COLOR);
            if (colorIdx != -1) wallet.setColor(cursor.getString(colorIdx));
            int minBalIdx = cursor.getColumnIndex(DatabaseHelper.COL_WALLET_MIN_BALANCE);
            if (minBalIdx != -1) wallet.setMinBalance(cursor.getDouble(minBalIdx));
            cursor.close();
            return wallet;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    /**
     * Cập nhật hạn mức số dư tối thiểu của ví.
     */
    public int updateMinBalance(long walletId, double minBalance) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_WALLET_MIN_BALANCE, minBalance);
        return db.update(DatabaseHelper.TABLE_WALLETS, values,
                DatabaseHelper.COL_WALLET_ID + " = ?",
                new String[]{String.valueOf(walletId)});
    }

    /**
     * Cập nhật số dư cho một ví.
     */
    public int updateBalance(long walletId, double newBalance) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_WALLET_BALANCE, newBalance);
        return db.update(DatabaseHelper.TABLE_WALLETS, values, 
                DatabaseHelper.COL_WALLET_ID + " = ?", 
                new String[]{String.valueOf(walletId)});
    }
}
