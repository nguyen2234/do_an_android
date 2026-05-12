package com.example.android_app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Lớp trợ giúp thao tác với SQLite.
 * Chịu trách nhiệm tạo bảng và nâng cấp phiên bản cơ sở dữ liệu.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Tên database và phiên bản
    private static final String DATABASE_NAME = "ExpenseManager.db";
    private static final int DATABASE_VERSION = 5; // Tăng version lên 5 để thêm note cho categories

    // --- BẢNG NGƯỜI DÙNG (USERS) ---
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_USERNAME = "username";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_AVATAR = "avatar";
    public static final String COL_USER_THEME = "theme_mode";

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_USERNAME + " TEXT UNIQUE NOT NULL, " +
                    COL_USER_PASSWORD + " TEXT NOT NULL, " +
                    COL_USER_EMAIL + " TEXT, " +
                    COL_USER_AVATAR + " TEXT, " +
                    COL_USER_THEME + " INTEGER DEFAULT 0" +
                    ");";

    // --- BẢNG VÍ (WALLETS) ---
    public static final String TABLE_WALLETS = "wallets";
    public static final String COL_WALLET_ID = "id";
    public static final String COL_WALLET_NAME = "name";
    public static final String COL_WALLET_BALANCE = "balance";
    public static final String COL_WALLET_TYPE = "type";
    public static final String COL_WALLET_CURRENCY = "currency";
    public static final String COL_WALLET_ICON = "icon";
    public static final String COL_WALLET_COLOR = "color";

    private static final String CREATE_TABLE_WALLETS =
            "CREATE TABLE " + TABLE_WALLETS + " (" +
                    COL_WALLET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_WALLET_NAME + " TEXT NOT NULL, " +
                    COL_WALLET_BALANCE + " REAL NOT NULL, " +
                    COL_WALLET_TYPE + " TEXT, " +
                    COL_WALLET_CURRENCY + " TEXT, " +
                    COL_WALLET_ICON + " TEXT, " +
                    COL_WALLET_COLOR + " TEXT" +
                    ");";

    // --- BẢNG DANH MỤC (CATEGORIES) ---
    public static final String TABLE_CATEGORIES = "categories";
    public static final String COL_CATEGORY_ID = "id";
    public static final String COL_CATEGORY_NAME = "name";
    public static final String COL_CATEGORY_ICON = "icon";
    public static final String COL_CATEGORY_TYPE = "type"; // "income" hoặc "expense"
    public static final String COL_CATEGORY_COLOR = "color";
    public static final String COL_CATEGORY_NOTE = "note";

    private static final String CREATE_TABLE_CATEGORIES =
            "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                    COL_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_CATEGORY_NAME + " TEXT NOT NULL, " +
                    COL_CATEGORY_ICON + " TEXT, " +
                    COL_CATEGORY_TYPE + " TEXT, " +
                    COL_CATEGORY_COLOR + " INTEGER, " +
                    COL_CATEGORY_NOTE + " TEXT" +
                    ");";

    // --- BẢNG GIAO DỊCH (TRANSACTIONS) ---
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COL_TRANS_ID = "id";
    public static final String COL_TRANS_TITLE = "title";
    public static final String COL_TRANS_AMOUNT = "amount";
    public static final String COL_TRANS_CATEGORY = "category"; // Có thể lưu tên hoặc ID danh mục
    public static final String COL_TRANS_TYPE = "type"; // "income" hoặc "expense"
    public static final String COL_TRANS_DATE = "date";
    public static final String COL_TRANS_NOTE = "note";
    public static final String COL_TRANS_WALLET_ID = "wallet_id"; // Khóa ngoại trỏ tới ví

    private static final String CREATE_TABLE_TRANSACTIONS =
            "CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                    COL_TRANS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_TRANS_TITLE + " TEXT NOT NULL, " +
                    COL_TRANS_AMOUNT + " REAL NOT NULL, " +
                    COL_TRANS_CATEGORY + " TEXT, " +
                    COL_TRANS_TYPE + " TEXT, " +
                    COL_TRANS_DATE + " TEXT, " +
                    COL_TRANS_NOTE + " TEXT, " +
                    COL_TRANS_WALLET_ID + " INTEGER, " +
                    "FOREIGN KEY(" + COL_TRANS_WALLET_ID + ") REFERENCES " + TABLE_WALLETS + "(" + COL_WALLET_ID + ")" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tạo các bảng khi database được khởi tạo lần đầu
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_WALLETS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Xóa bảng cũ nếu tồn tại và tạo lại khi có phiên bản mới
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WALLETS);
        onCreate(db);
    }
}
