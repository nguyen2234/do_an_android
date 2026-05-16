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

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long addWallet(ViTien wallet) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_WALLET_NAME, wallet.getName());
        values.put(DatabaseHelper.COL_WALLET_BALANCE, wallet.getBalance());
        values.put(DatabaseHelper.COL_WALLET_TYPE, wallet.getLoai());
        values.put(DatabaseHelper.COL_WALLET_CURRENCY, wallet.getCurrency());
        values.put(DatabaseHelper.COL_WALLET_ICON, wallet.getIcon());
        values.put(DatabaseHelper.COL_WALLET_COLOR, wallet.getColor());
        values.put(DatabaseHelper.COL_WALLET_ACCOUNT_NUMBER, wallet.getAccountNumber());
        values.put(DatabaseHelper.COL_WALLET_BANK_NAME, wallet.getBankName());
        return db.insert(DatabaseHelper.TABLE_WALLETS, null, values);
    }

    public int updateWallet(ViTien wallet) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_WALLET_NAME, wallet.getName());
        values.put(DatabaseHelper.COL_WALLET_BALANCE, wallet.getBalance());
        values.put(DatabaseHelper.COL_WALLET_TYPE, wallet.getLoai());
        values.put(DatabaseHelper.COL_WALLET_CURRENCY, wallet.getCurrency());
        values.put(DatabaseHelper.COL_WALLET_ICON, wallet.getIcon());
        values.put(DatabaseHelper.COL_WALLET_COLOR, wallet.getColor());
        values.put(DatabaseHelper.COL_WALLET_ACCOUNT_NUMBER, wallet.getAccountNumber());
        values.put(DatabaseHelper.COL_WALLET_BANK_NAME, wallet.getBankName());
        return db.update(DatabaseHelper.TABLE_WALLETS, values,
                DatabaseHelper.COL_WALLET_ID + " = ?",
                new String[]{String.valueOf(wallet.getId())});
    }

    public int deleteWallet(long id) {
        return db.delete(DatabaseHelper.TABLE_WALLETS,
                DatabaseHelper.COL_WALLET_ID + " = ?",
                new String[]{String.valueOf(id)});
    }

    public ViTien getWalletById(long id) {
        Cursor cursor = db.query(DatabaseHelper.TABLE_WALLETS, null,
                DatabaseHelper.COL_WALLET_ID + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            ViTien wallet = mapCursorToWallet(cursor);
            cursor.close();
            return wallet;
        }
        return null;
    }

    public List<ViTien> getAllWallets() {
        List<ViTien> wallets = new ArrayList<>();
        Cursor cursor = db.query(DatabaseHelper.TABLE_WALLETS, null, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                ViTien wallet = mapCursorToWallet(cursor);
                wallets.add(wallet);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return wallets;
    }

    private ViTien mapCursorToWallet(Cursor cursor) {
        ViTien wallet = new ViTien();
        wallet.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WALLET_ID)));
        wallet.setName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WALLET_NAME)));
        wallet.setBalance(cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WALLET_BALANCE)));
        wallet.setLoai(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WALLET_TYPE)));
        wallet.setCurrency(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WALLET_CURRENCY)));
        wallet.setIcon(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WALLET_ICON)));
        wallet.setColor(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WALLET_COLOR)));
        wallet.setAccountNumber(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WALLET_ACCOUNT_NUMBER)));
        wallet.setBankName(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_WALLET_BANK_NAME)));
        return wallet;
    }

    /**
     * THỰC HIỆN CHUYỂN TIỀN GIỮA HAI VÍ
     */
    public boolean transferMoney(long fromWalletId, long toWalletId, double amount) {
        db.beginTransaction();
        try {
            ViTien fromWallet = getWalletById(fromWalletId);
            ViTien toWallet = getWalletById(toWalletId);

            if (fromWallet == null || toWallet == null || fromWallet.getBalance() < amount) {
                return false;
            }

            updateBalance(fromWalletId, fromWallet.getBalance() - amount);
            updateBalance(toWalletId, toWallet.getBalance() + amount);

            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public int updateBalance(long walletId, double newBalance) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_WALLET_BALANCE, newBalance);
        return db.update(DatabaseHelper.TABLE_WALLETS, values,
                DatabaseHelper.COL_WALLET_ID + " = ?",
                new String[]{String.valueOf(walletId)});
    }
}
