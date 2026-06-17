package com.example.android_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.android_app.model.NguoiDung;

public class NguoiDungDAO {
    private SQLiteDatabase db;
    private DatabaseHelper dbHelper;

    public NguoiDungDAO(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    public void open() {
        db = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    
    public long registerUser(NguoiDung user) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_USERNAME, user.getUsername());
        values.put(DatabaseHelper.COL_USER_PASSWORD, user.getPassword());
        values.put(DatabaseHelper.COL_USER_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COL_USER_AVATAR, user.getAvatar());
        values.put(DatabaseHelper.COL_USER_THEME, user.getThemeMode());
        values.put(DatabaseHelper.COL_USER_FULLNAME, user.getFullname());
        values.put(DatabaseHelper.COL_USER_PIN, user.getTransactionPin());
        return db.insert(DatabaseHelper.TABLE_USERS, null, values);
    }

    
    public boolean checkUsernameExist(String username) {
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, new String[]{DatabaseHelper.COL_USER_ID},
                DatabaseHelper.COL_USER_USERNAME + "=?",
                new String[]{username}, null, null, null);
        
        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    
    public NguoiDung checkLogin(String username, String password) {
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                DatabaseHelper.COL_USER_USERNAME + "=? AND " + DatabaseHelper.COL_USER_PASSWORD + "=?",
                new String[]{username, password}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            NguoiDung user = new NguoiDung();
            user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_USERNAME)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL)));
            user.setAvatar(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_AVATAR)));
            user.setThemeMode(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_THEME)));
            user.setFullname(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_FULLNAME)));
            user.setTransactionPin(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PIN)));
            cursor.close();
            return user;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    
    public NguoiDung getUserById(long id) {
        Cursor cursor = db.query(DatabaseHelper.TABLE_USERS, null,
                DatabaseHelper.COL_USER_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            NguoiDung user = new NguoiDung();
            user.setId(cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_ID)));
            user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_USERNAME)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL)));
            user.setAvatar(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_AVATAR)));
            user.setThemeMode(cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_THEME)));
            user.setFullname(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_FULLNAME)));
            user.setTransactionPin(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PIN)));
            cursor.close();
            return user;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    
    public int updateUser(NguoiDung user) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_USERNAME, user.getUsername());
        values.put(DatabaseHelper.COL_USER_PASSWORD, user.getPassword());
        values.put(DatabaseHelper.COL_USER_EMAIL, user.getEmail());
        values.put(DatabaseHelper.COL_USER_AVATAR, user.getAvatar());
        values.put(DatabaseHelper.COL_USER_THEME, user.getThemeMode());
        values.put(DatabaseHelper.COL_USER_FULLNAME, user.getFullname());
        values.put(DatabaseHelper.COL_USER_PIN, user.getTransactionPin());

        return db.update(DatabaseHelper.TABLE_USERS, values,
                DatabaseHelper.COL_USER_ID + "=?",
                new String[]{String.valueOf(user.getId())});
    }

    
    public int updateUserProfile(long userId, String username, String email, String avatar) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_USERNAME, username);
        values.put(DatabaseHelper.COL_USER_EMAIL, email);
        if (avatar != null) {
            values.put(DatabaseHelper.COL_USER_AVATAR, avatar);
        }
        return db.update(DatabaseHelper.TABLE_USERS, values,
                DatabaseHelper.COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
    }

    
    public int updateTransactionPin(long userId, String newPin) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_USER_PIN, newPin);
        return db.update(DatabaseHelper.TABLE_USERS, values,
                DatabaseHelper.COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)});
    }
}
