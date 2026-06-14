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
    private static final int DATABASE_VERSION = 11;

    // Khóa ngoại trỏ tới người dùng
    public static final String COL_USER_ID_FK = "user_id";

    // --- BẢNG NGƯỜI DÙNG (USERS) ---
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "id";
    public static final String COL_USER_USERNAME = "username";
    public static final String COL_USER_PASSWORD = "password";
    public static final String COL_USER_EMAIL = "email";
    public static final String COL_USER_AVATAR = "avatar";
    public static final String COL_USER_THEME = "theme_mode";
    public static final String COL_USER_FULLNAME = "fullname";

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_USERNAME + " TEXT UNIQUE NOT NULL, " +
                    COL_USER_PASSWORD + " TEXT NOT NULL, " +
                    COL_USER_EMAIL + " TEXT, " +
                    COL_USER_AVATAR + " TEXT, " +
                    COL_USER_THEME + " INTEGER DEFAULT 0, " +
                    COL_USER_FULLNAME + " TEXT" +
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
    public static final String COL_WALLET_MIN_BALANCE = "min_balance"; // [MỚI] Hạn mức tối thiểu nhắc nhở

    private static final String CREATE_TABLE_WALLETS =
            "CREATE TABLE " + TABLE_WALLETS + " (" +
                    COL_WALLET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_WALLET_NAME + " TEXT NOT NULL, " +
                    COL_WALLET_BALANCE + " REAL NOT NULL, " +
                    COL_WALLET_TYPE + " TEXT, " +
                    COL_WALLET_CURRENCY + " TEXT, " +
                    COL_WALLET_ICON + " TEXT, " +
                    COL_WALLET_COLOR + " TEXT, " +
                    COL_WALLET_MIN_BALANCE + " REAL DEFAULT 0, " +
                    COL_USER_ID_FK + " INTEGER" +
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
                    COL_CATEGORY_NOTE + " TEXT, " +
                    COL_USER_ID_FK + " INTEGER" +
                    ");";

    // --- BẢNG GIAO DỊCH (TRANSACTIONS) ---
    public static final String TABLE_TRANSACTIONS = "transactions";
    public static final String COL_TRANS_ID = "id";
    public static final String COL_TRANS_TITLE = "title";
    public static final String COL_TRANS_AMOUNT = "amount";
    public static final String COL_TRANS_CATEGORY = "category";
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
                    COL_USER_ID_FK + " INTEGER, " +
                    "FOREIGN KEY(" + COL_TRANS_WALLET_ID + ") REFERENCES " + TABLE_WALLETS + "(" + COL_WALLET_ID + ")" +
                    ");";

    // --- BẢNG NGÂN SÁCH (BUDGETS) ---
    public static final String TABLE_BUDGETS = "budgets";
    public static final String COL_BUDGET_ID = "id";
    public static final String COL_BUDGET_NAME = "name";
    public static final String COL_BUDGET_AMOUNT = "amount";
    public static final String COL_BUDGET_SPENT = "spent_amount";
    public static final String COL_BUDGET_START = "start_date";
    public static final String COL_BUDGET_END = "end_date";
    public static final String COL_BUDGET_CATEGORIES = "category_ids";

    private static final String CREATE_TABLE_BUDGETS =
            "CREATE TABLE " + TABLE_BUDGETS + " (" +
                    COL_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_BUDGET_NAME + " TEXT NOT NULL, " +
                    COL_BUDGET_AMOUNT + " REAL NOT NULL, " +
                    COL_BUDGET_SPENT + " REAL DEFAULT 0, " +
                    COL_BUDGET_START + " TEXT, " +
                    COL_BUDGET_END + " TEXT, " +
                    COL_BUDGET_CATEGORIES + " TEXT, " +
                    COL_USER_ID_FK + " INTEGER" +
                    ");";

    // --- BẢNG GIAO DỊCH DỰ KIẾN (PLANNED TRANSACTIONS) [MỚI] ---
    public static final String TABLE_PLANNED = "planned_transactions";
    public static final String COL_PLANNED_ID = "id";
    public static final String COL_PLANNED_TITLE = "title";
    public static final String COL_PLANNED_AMOUNT = "amount";
    public static final String COL_PLANNED_CATEGORY = "category";
    public static final String COL_PLANNED_TYPE = "type";           // "income" hoặc "expense"
    public static final String COL_PLANNED_DUE_DATE = "due_date";   // Ngày đến hạn (dd/MM/yyyy)
    public static final String COL_PLANNED_STATUS = "status";       // "pending" hoặc "completed"
    public static final String COL_PLANNED_NOTE = "note";
    public static final String COL_PLANNED_WALLET_ID = "wallet_id";

    private static final String CREATE_TABLE_PLANNED =
            "CREATE TABLE " + TABLE_PLANNED + " (" +
                    COL_PLANNED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_PLANNED_TITLE + " TEXT NOT NULL, " +
                    COL_PLANNED_AMOUNT + " REAL NOT NULL, " +
                    COL_PLANNED_CATEGORY + " TEXT, " +
                    COL_PLANNED_TYPE + " TEXT, " +
                    COL_PLANNED_DUE_DATE + " TEXT, " +
                    COL_PLANNED_STATUS + " TEXT DEFAULT 'pending', " +
                    COL_PLANNED_NOTE + " TEXT, " +
                    COL_PLANNED_WALLET_ID + " INTEGER, " +
                    COL_USER_ID_FK + " INTEGER" +
                    ");";

    // --- BẢNG THÔNG BÁO (NOTIFICATIONS) [MỚI] ---
    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String COL_NOTIFICATION_ID = "id";
    public static final String COL_NOTIFICATION_TITLE = "title";
    public static final String COL_NOTIFICATION_CONTENT = "content";
    public static final String COL_NOTIFICATION_DATE = "date";
    public static final String COL_NOTIFICATION_IS_READ = "is_read"; // 0: chưa đọc, 1: đã đọc
    public static final String COL_NOTIFICATION_TYPE = "type";       // system, warning, transaction, reminder

    private static final String CREATE_TABLE_NOTIFICATIONS =
            "CREATE TABLE " + TABLE_NOTIFICATIONS + " (" +
                    COL_NOTIFICATION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_NOTIFICATION_TITLE + " TEXT NOT NULL, " +
                    COL_NOTIFICATION_CONTENT + " TEXT NOT NULL, " +
                    COL_NOTIFICATION_DATE + " TEXT NOT NULL, " +
                    COL_NOTIFICATION_IS_READ + " INTEGER DEFAULT 0, " +
                    COL_NOTIFICATION_TYPE + " TEXT, " +
                    COL_USER_ID_FK + " INTEGER" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_WALLETS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
        db.execSQL(CREATE_TABLE_BUDGETS);
        db.execSQL(CREATE_TABLE_PLANNED);
        db.execSQL(CREATE_TABLE_NOTIFICATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nâng cấp khéo léo để giữ lại dữ liệu cũ
        if (oldVersion < 7) {
            // Thêm cột min_balance vào bảng wallets nếu chưa có
            try {
                db.execSQL("ALTER TABLE " + TABLE_WALLETS + " ADD COLUMN " + COL_WALLET_MIN_BALANCE + " REAL DEFAULT 0");
            } catch (Exception e) {
                // Cột đã tồn tại, bỏ qua
            }
            // Tạo bảng planned_transactions nếu chưa tồn tại
            try {
                db.execSQL(CREATE_TABLE_PLANNED);
            } catch (Exception e) {
                // Bảng đã tồn tại, bỏ qua
            }
        }
        
        // Tạo bảng budgets nếu chưa tồn tại (cho các phiên bản cũ hơn)
        try {
            db.execSQL(CREATE_TABLE_BUDGETS);
        } catch (Exception e) {
            // Bảng đã tồn tại, bỏ qua
        }

        // Thực hiện nâng cấp từ v7 lên v8 để thêm user_id vào tất cả các bảng tài chính
        if (oldVersion < 8) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_WALLETS + " ADD COLUMN " + COL_USER_ID_FK + " INTEGER DEFAULT 1");
                db.execSQL("ALTER TABLE " + TABLE_CATEGORIES + " ADD COLUMN " + COL_USER_ID_FK + " INTEGER DEFAULT 1");
                db.execSQL("ALTER TABLE " + TABLE_TRANSACTIONS + " ADD COLUMN " + COL_USER_ID_FK + " INTEGER DEFAULT 1");
                db.execSQL("ALTER TABLE " + TABLE_BUDGETS + " ADD COLUMN " + COL_USER_ID_FK + " INTEGER DEFAULT 1");
                db.execSQL("ALTER TABLE " + TABLE_PLANNED + " ADD COLUMN " + COL_USER_ID_FK + " INTEGER DEFAULT 1");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Thực hiện nâng cấp lên v9 để tạo bảng notifications
        if (oldVersion < 9) {
            try {
                db.execSQL(CREATE_TABLE_NOTIFICATIONS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Thực hiện nâng cấp lên v10 để thêm cột fullname vào bảng users
        if (oldVersion < 10) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_USER_FULLNAME + " TEXT");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Thực hiện nâng cấp lên v11 (hệ thống danh mục chung mới)
        if (oldVersion < 11) {
            // Không thay đổi cấu trúc bảng để duy trì tương thích dữ liệu cũ.
            // Danh mục chung sẽ sử dụng type = 'general' hoặc kế thừa dữ liệu cũ.
        }
    }
}
