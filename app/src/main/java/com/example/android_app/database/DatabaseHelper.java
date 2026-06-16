package com.example.android_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Lớp trợ giúp thao tác với SQLite.
 * Chịu trách nhiệm tạo bảng và nâng cấp phiên bản cơ sở dữ liệu.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Tên database và phiên bản
    private static final String DATABASE_NAME = "ExpenseManager.db";
    private static final int DATABASE_VERSION = 14;

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
    public static final String COL_USER_PIN = "transaction_pin";

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_USER_USERNAME + " TEXT UNIQUE NOT NULL, " +
                    COL_USER_PASSWORD + " TEXT NOT NULL, " +
                    COL_USER_EMAIL + " TEXT, " +
                    COL_USER_AVATAR + " TEXT, " +
                    COL_USER_THEME + " INTEGER DEFAULT 0, " +
                    COL_USER_FULLNAME + " TEXT, " +
                    COL_USER_PIN + " TEXT" +
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
    public static final String COL_WALLET_MIN_BALANCE = "min_balance";

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
    public static final String COL_TRANS_CATEGORY_ID = "category_id";
    public static final String COL_TRANS_TYPE = "type"; // "income" hoặc "expense"
    public static final String COL_TRANS_DATE = "date";
    public static final String COL_TRANS_NOTE = "note";
    public static final String COL_TRANS_WALLET_ID = "wallet_id";

    private static final String CREATE_TABLE_TRANSACTIONS =
            "CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                    COL_TRANS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_TRANS_TITLE + " TEXT NOT NULL, " +
                    COL_TRANS_AMOUNT + " REAL NOT NULL, " +
                    COL_TRANS_CATEGORY_ID + " INTEGER, " +
                    COL_TRANS_TYPE + " TEXT, " +
                    COL_TRANS_DATE + " TEXT, " +
                    COL_TRANS_NOTE + " TEXT, " +
                    COL_TRANS_WALLET_ID + " INTEGER, " +
                    COL_USER_ID_FK + " INTEGER, " +
                    "FOREIGN KEY(" + COL_TRANS_WALLET_ID + ") REFERENCES " + TABLE_WALLETS + "(" + COL_WALLET_ID + "), " +
                    "FOREIGN KEY(" + COL_TRANS_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COL_CATEGORY_ID + ")" +
                    ");";

    // --- BẢNG NGÂN SÁCH (BUDGETS) ---
    public static final String TABLE_BUDGETS = "budgets";
    public static final String COL_BUDGET_ID = "id";
    public static final String COL_BUDGET_NAME = "name";
    public static final String COL_BUDGET_AMOUNT = "amount";
    public static final String COL_BUDGET_SPENT = "spent_amount";
    public static final String COL_BUDGET_START = "start_date";
    public static final String COL_BUDGET_END = "end_date";
    public static final String COL_BUDGET_CATEGORIES = "category_ids"; // Deprecated (chỉ dùng trong migration)

    private static final String CREATE_TABLE_BUDGETS =
            "CREATE TABLE " + TABLE_BUDGETS + " (" +
                    COL_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_BUDGET_NAME + " TEXT NOT NULL, " +
                    COL_BUDGET_AMOUNT + " REAL NOT NULL, " +
                    COL_BUDGET_SPENT + " REAL DEFAULT 0, " +
                    COL_BUDGET_START + " TEXT, " +
                    COL_BUDGET_END + " TEXT, " +
                    COL_USER_ID_FK + " INTEGER" +
                    ");";

    // --- BẢNG TRUNG GIAN NGÂN SÁCH - DANH MỤC (BUDGET_CATEGORIES) [MỚI] ---
    public static final String TABLE_BUDGET_CATEGORIES = "budget_categories";
    public static final String COL_BC_BUDGET_ID = "budget_id";
    public static final String COL_BC_CATEGORY_ID = "category_id";

    private static final String CREATE_TABLE_BUDGET_CATEGORIES =
            "CREATE TABLE " + TABLE_BUDGET_CATEGORIES + " (" +
                    COL_BC_BUDGET_ID + " INTEGER, " +
                    COL_BC_CATEGORY_ID + " INTEGER, " +
                    "PRIMARY KEY (" + COL_BC_BUDGET_ID + ", " + COL_BC_CATEGORY_ID + "), " +
                    "FOREIGN KEY (" + COL_BC_BUDGET_ID + ") REFERENCES " + TABLE_BUDGETS + "(" + COL_BUDGET_ID + ") ON DELETE CASCADE, " +
                    "FOREIGN KEY (" + COL_BC_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COL_CATEGORY_ID + ") ON DELETE CASCADE" +
                    ");";

    // --- BẢNG GIAO DỊCH DỰ KIẾN (PLANNED TRANSACTIONS) ---
    public static final String TABLE_PLANNED = "planned_transactions";
    public static final String COL_PLANNED_ID = "id";
    public static final String COL_PLANNED_TITLE = "title";
    public static final String COL_PLANNED_AMOUNT = "amount";
    public static final String COL_PLANNED_CATEGORY_ID = "category_id";
    public static final String COL_PLANNED_TYPE = "type";
    public static final String COL_PLANNED_DUE_DATE = "due_date";
    public static final String COL_PLANNED_STATUS = "status";
    public static final String COL_PLANNED_NOTE = "note";
    public static final String COL_PLANNED_WALLET_ID = "wallet_id";

    private static final String CREATE_TABLE_PLANNED =
            "CREATE TABLE " + TABLE_PLANNED + " (" +
                    COL_PLANNED_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_PLANNED_TITLE + " TEXT NOT NULL, " +
                    COL_PLANNED_AMOUNT + " REAL NOT NULL, " +
                    COL_PLANNED_CATEGORY_ID + " INTEGER, " +
                    COL_PLANNED_TYPE + " TEXT, " +
                    COL_PLANNED_DUE_DATE + " TEXT, " +
                    COL_PLANNED_STATUS + " TEXT DEFAULT 'pending', " +
                    COL_PLANNED_NOTE + " TEXT, " +
                    COL_PLANNED_WALLET_ID + " INTEGER, " +
                    COL_USER_ID_FK + " INTEGER, " +
                    "FOREIGN KEY(" + COL_PLANNED_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COL_CATEGORY_ID + ")" +
                    ");";

    // --- BẢNG THÔNG BÁO (NOTIFICATIONS) ---
    public static final String TABLE_NOTIFICATIONS = "notifications";
    public static final String COL_NOTIFICATION_ID = "id";
    public static final String COL_NOTIFICATION_TITLE = "title";
    public static final String COL_NOTIFICATION_CONTENT = "content";
    public static final String COL_NOTIFICATION_DATE = "date";
    public static final String COL_NOTIFICATION_IS_READ = "is_read";
    public static final String COL_NOTIFICATION_TYPE = "type";

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

    // --- BẢNG NHẮC HẸN THANH TOÁN (REMINDERS) ---
    public static final String TABLE_REMINDERS = "reminders";
    public static final String COL_REMINDER_ID = "id";
    public static final String COL_REMINDER_TITLE = "title";
    public static final String COL_REMINDER_AMOUNT = "estimated_amount";
    public static final String COL_REMINDER_DUE_DATE = "due_date";
    public static final String COL_REMINDER_RECURRENCE = "recurrence";
    public static final String COL_REMINDER_OFFSET_DAYS = "offset_days";
    public static final String COL_REMINDER_STATUS = "status";
    public static final String COL_REMINDER_CATEGORY_ID = "category_id";

    private static final String CREATE_TABLE_REMINDERS =
            "CREATE TABLE " + TABLE_REMINDERS + " (" +
                    COL_REMINDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_REMINDER_TITLE + " TEXT NOT NULL, " +
                    COL_REMINDER_AMOUNT + " REAL NOT NULL, " +
                    COL_REMINDER_DUE_DATE + " TEXT NOT NULL, " +
                    COL_REMINDER_RECURRENCE + " TEXT NOT NULL, " +
                    COL_REMINDER_OFFSET_DAYS + " INTEGER DEFAULT 0, " +
                    COL_REMINDER_STATUS + " TEXT DEFAULT 'PENDING', " +
                    COL_REMINDER_CATEGORY_ID + " INTEGER, " +
                    COL_USER_ID_FK + " INTEGER, " +
                    "FOREIGN KEY(" + COL_REMINDER_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COL_CATEGORY_ID + ")" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_WALLETS);
        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_TRANSACTIONS);
        db.execSQL(CREATE_TABLE_BUDGETS);
        db.execSQL(CREATE_TABLE_BUDGET_CATEGORIES);
        db.execSQL(CREATE_TABLE_PLANNED);
        db.execSQL(CREATE_TABLE_NOTIFICATIONS);
        db.execSQL(CREATE_TABLE_REMINDERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nâng cấp khéo léo để giữ lại dữ liệu cũ
        if (oldVersion < 7) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_WALLETS + " ADD COLUMN " + COL_WALLET_MIN_BALANCE + " REAL DEFAULT 0");
            } catch (Exception e) {}
            try {
                db.execSQL(CREATE_TABLE_PLANNED);
            } catch (Exception e) {}
        }
        
        try {
            db.execSQL(CREATE_TABLE_BUDGETS);
        } catch (Exception e) {}

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

        if (oldVersion < 9) {
            try {
                db.execSQL(CREATE_TABLE_NOTIFICATIONS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (oldVersion < 10) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_USER_FULLNAME + " TEXT");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (oldVersion < 11) {
            // Hệ thống danh mục chung không thay đổi cấu trúc bảng
        }

        if (oldVersion < 12) {
            try {
                db.execSQL(CREATE_TABLE_REMINDERS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (oldVersion < 13) {
            try {
                db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN " + COL_USER_PIN + " TEXT");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Thực hiện nâng cấp từ v13 lên v14: Chuẩn hóa 1NF ngân sách và thay đổi category TEXT -> category_id INTEGER
        if (oldVersion < 14) {
            db.beginTransaction();
            try {
                // 1. Tạo bảng trung gian budget_categories
                db.execSQL("CREATE TABLE budget_categories (" +
                        "budget_id INTEGER, " +
                        "category_id INTEGER, " +
                        "PRIMARY KEY (budget_id, category_id), " +
                        "FOREIGN KEY (budget_id) REFERENCES budgets(id) ON DELETE CASCADE, " +
                        "FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE" +
                        ");");

                // 2. Tách chuỗi category_ids và lưu vào budget_categories
                Cursor budgetsCursor = db.rawQuery("SELECT id, category_ids, user_id FROM budgets", null);
                if (budgetsCursor != null) {
                    while (budgetsCursor.moveToNext()) {
                        long budgetId = budgetsCursor.getLong(0);
                        String categoryNamesStr = budgetsCursor.getString(1);
                        long userId = budgetsCursor.getLong(2);

                        if (categoryNamesStr != null && !categoryNamesStr.trim().isEmpty()) {
                            String[] names = categoryNamesStr.split(",");
                            for (String name : names) {
                                String cleanName = name.trim();
                                if (!cleanName.isEmpty()) {
                                    // Tìm id danh mục tương ứng
                                    Cursor catCursor = db.rawQuery(
                                            "SELECT id FROM categories WHERE name = ? AND user_id = ?",
                                            new String[]{cleanName, String.valueOf(userId)});
                                    long catId = -1;
                                    if (catCursor != null && catCursor.moveToFirst()) {
                                        catId = catCursor.getLong(0);
                                    } else {
                                        Cursor catCursorFallback = db.rawQuery(
                                                "SELECT id FROM categories WHERE name = ?",
                                                new String[]{cleanName});
                                        if (catCursorFallback != null && catCursorFallback.moveToFirst()) {
                                            catId = catCursorFallback.getLong(0);
                                        }
                                        if (catCursorFallback != null) catCursorFallback.close();
                                    }
                                    if (catCursor != null) catCursor.close();

                                    // Nếu không tìm thấy, thêm mới danh mục để tránh mất liên kết
                                    if (catId == -1) {
                                        ContentValues catValues = new ContentValues();
                                        catValues.put("name", cleanName);
                                        catValues.put("type", "expense");
                                        catValues.put("user_id", userId);
                                        catId = db.insert("categories", null, catValues);
                                    }

                                    if (catId != -1) {
                                        ContentValues bcValues = new ContentValues();
                                        bcValues.put("budget_id", budgetId);
                                        bcValues.put("category_id", catId);
                                        db.insert("budget_categories", null, bcValues);
                                    }
                                }
                            }
                        }
                    }
                    budgetsCursor.close();
                }

                // 3. Xóa cột category_ids khỏi bảng budgets
                db.execSQL("CREATE TABLE budgets_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "name TEXT NOT NULL, " +
                        "amount REAL NOT NULL, " +
                        "spent_amount REAL DEFAULT 0, " +
                        "start_date TEXT, " +
                        "end_date TEXT, " +
                        "user_id INTEGER" +
                        ");");
                db.execSQL("INSERT INTO budgets_new (id, name, amount, spent_amount, start_date, end_date, user_id) " +
                        "SELECT id, name, amount, spent_amount, start_date, end_date, user_id FROM budgets;");
                db.execSQL("DROP TABLE budgets;");
                db.execSQL("ALTER TABLE budgets_new RENAME TO budgets;");

                // 4. Chuyển đổi bảng transactions (category TEXT -> category_id INTEGER)
                db.execSQL("CREATE TABLE transactions_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT NOT NULL, " +
                        "amount REAL NOT NULL, " +
                        "category_id INTEGER, " +
                        "type TEXT, " +
                        "date TEXT, " +
                        "note TEXT, " +
                        "wallet_id INTEGER, " +
                        "user_id INTEGER, " +
                        "FOREIGN KEY(wallet_id) REFERENCES wallets(id), " +
                        "FOREIGN KEY(category_id) REFERENCES categories(id)" +
                        ");");

                Cursor transCursor = db.rawQuery("SELECT id, title, amount, category, type, date, note, wallet_id, user_id FROM transactions", null);
                if (transCursor != null) {
                    while (transCursor.moveToNext()) {
                        long id = transCursor.getLong(0);
                        String title = transCursor.getString(1);
                        double amount = transCursor.getDouble(2);
                        String catName = transCursor.getString(3);
                        String type = transCursor.getString(4);
                        String date = transCursor.getString(5);
                        String note = transCursor.getString(6);
                        long walletId = transCursor.getLong(7);
                        long userId = transCursor.getLong(8);

                        long catId = -1;
                        if (catName != null && !catName.trim().isEmpty()) {
                            Cursor catCursor = db.rawQuery(
                                    "SELECT id FROM categories WHERE name = ? AND user_id = ?",
                                    new String[]{catName.trim(), String.valueOf(userId)});
                            if (catCursor != null && catCursor.moveToFirst()) {
                                catId = catCursor.getLong(0);
                            } else {
                                Cursor catCursorFallback = db.rawQuery(
                                        "SELECT id FROM categories WHERE name = ?",
                                        new String[]{catName.trim()});
                                if (catCursorFallback != null && catCursorFallback.moveToFirst()) {
                                    catId = catCursorFallback.getLong(0);
                                }
                                if (catCursorFallback != null) catCursorFallback.close();
                            }
                            if (catCursor != null) catCursor.close();

                            if (catId == -1) {
                                ContentValues catValues = new ContentValues();
                                catValues.put("name", catName.trim());
                                catValues.put("type", type != null ? type : "expense");
                                catValues.put("user_id", userId);
                                catId = db.insert("categories", null, catValues);
                            }
                        }

                        ContentValues transValues = new ContentValues();
                        transValues.put("id", id);
                        transValues.put("title", title);
                        transValues.put("amount", amount);
                        transValues.put("category_id", catId != -1 ? catId : null);
                        transValues.put("type", type);
                        transValues.put("date", date);
                        transValues.put("note", note);
                        transValues.put("wallet_id", walletId);
                        transValues.put("user_id", userId);
                        db.insert("transactions_new", null, transValues);
                    }
                    transCursor.close();
                }
                db.execSQL("DROP TABLE transactions;");
                db.execSQL("ALTER TABLE transactions_new RENAME TO transactions;");

                // 5. Chuyển đổi bảng planned_transactions (category TEXT -> category_id INTEGER)
                db.execSQL("CREATE TABLE planned_transactions_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT NOT NULL, " +
                        "amount REAL NOT NULL, " +
                        "category_id INTEGER, " +
                        "type TEXT, " +
                        "due_date TEXT, " +
                        "status TEXT DEFAULT 'pending', " +
                        "note TEXT, " +
                        "wallet_id INTEGER, " +
                        "user_id INTEGER, " +
                        "FOREIGN KEY(category_id) REFERENCES categories(id)" +
                        ");");

                Cursor plannedCursor = db.rawQuery("SELECT id, title, amount, category, type, due_date, status, note, wallet_id, user_id FROM planned_transactions", null);
                if (plannedCursor != null) {
                    while (plannedCursor.moveToNext()) {
                        long id = plannedCursor.getLong(0);
                        String title = plannedCursor.getString(1);
                        double amount = plannedCursor.getDouble(2);
                        String catName = plannedCursor.getString(3);
                        String type = plannedCursor.getString(4);
                        String dueDate = plannedCursor.getString(5);
                        String status = plannedCursor.getString(6);
                        String note = plannedCursor.getString(7);
                        long walletId = plannedCursor.getLong(8);
                        long userId = plannedCursor.getLong(9);

                        long catId = -1;
                        if (catName != null && !catName.trim().isEmpty()) {
                            Cursor catCursor = db.rawQuery(
                                    "SELECT id FROM categories WHERE name = ? AND user_id = ?",
                                    new String[]{catName.trim(), String.valueOf(userId)});
                            if (catCursor != null && catCursor.moveToFirst()) {
                                catId = catCursor.getLong(0);
                            } else {
                                Cursor catCursorFallback = db.rawQuery(
                                        "SELECT id FROM categories WHERE name = ?",
                                        new String[]{catName.trim()});
                                if (catCursorFallback != null && catCursorFallback.moveToFirst()) {
                                    catId = catCursorFallback.getLong(0);
                                }
                                if (catCursorFallback != null) catCursorFallback.close();
                            }
                            if (catCursor != null) catCursor.close();

                            if (catId == -1) {
                                ContentValues catValues = new ContentValues();
                                catValues.put("name", catName.trim());
                                catValues.put("type", type != null ? type : "expense");
                                catValues.put("user_id", userId);
                                catId = db.insert("categories", null, catValues);
                            }
                        }

                        ContentValues pValues = new ContentValues();
                        pValues.put("id", id);
                        pValues.put("title", title);
                        pValues.put("amount", amount);
                        pValues.put("category_id", catId != -1 ? catId : null);
                        pValues.put("type", type);
                        pValues.put("due_date", dueDate);
                        pValues.put("status", status);
                        pValues.put("note", note);
                        pValues.put("wallet_id", walletId);
                        pValues.put("user_id", userId);
                        db.insert("planned_transactions_new", null, pValues);
                    }
                    plannedCursor.close();
                }
                db.execSQL("DROP TABLE planned_transactions;");
                db.execSQL("ALTER TABLE planned_transactions_new RENAME TO planned_transactions;");

                // 6. Chuyển đổi bảng reminders (category TEXT -> category_id INTEGER)
                db.execSQL("CREATE TABLE reminders_new (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "title TEXT NOT NULL, " +
                        "estimated_amount REAL NOT NULL, " +
                        "due_date TEXT NOT NULL, " +
                        "recurrence TEXT NOT NULL, " +
                        "offset_days INTEGER DEFAULT 0, " +
                        "status TEXT DEFAULT 'PENDING', " +
                        "category_id INTEGER, " +
                        "user_id INTEGER, " +
                        "FOREIGN KEY(category_id) REFERENCES categories(id)" +
                        ");");

                Cursor remCursor = db.rawQuery("SELECT id, title, estimated_amount, due_date, recurrence, offset_days, status, category, user_id FROM reminders", null);
                if (remCursor != null) {
                    while (remCursor.moveToNext()) {
                        long id = remCursor.getLong(0);
                        String title = remCursor.getString(1);
                        double amount = remCursor.getDouble(2);
                        String dueDate = remCursor.getString(3);
                        String recurrence = remCursor.getString(4);
                        int offsetDays = remCursor.getInt(5);
                        String status = remCursor.getString(6);
                        String catName = remCursor.getString(7);
                        long userId = remCursor.getLong(8);

                        long catId = -1;
                        if (catName != null && !catName.trim().isEmpty()) {
                            Cursor catCursor = db.rawQuery(
                                    "SELECT id FROM categories WHERE name = ? AND user_id = ?",
                                    new String[]{catName.trim(), String.valueOf(userId)});
                            if (catCursor != null && catCursor.moveToFirst()) {
                                catId = catCursor.getLong(0);
                            } else {
                                Cursor catCursorFallback = db.rawQuery(
                                        "SELECT id FROM categories WHERE name = ?",
                                        new String[]{catName.trim()});
                                if (catCursorFallback != null && catCursorFallback.moveToFirst()) {
                                    catId = catCursorFallback.getLong(0);
                                }
                                if (catCursorFallback != null) catCursorFallback.close();
                            }
                            if (catCursor != null) catCursor.close();

                            if (catId == -1) {
                                ContentValues catValues = new ContentValues();
                                catValues.put("name", catName.trim());
                                catValues.put("type", "expense");
                                catValues.put("user_id", userId);
                                catId = db.insert("categories", null, catValues);
                            }
                        }

                        ContentValues rValues = new ContentValues();
                        rValues.put("id", id);
                        rValues.put("title", title);
                        rValues.put("estimated_amount", amount);
                        rValues.put("due_date", dueDate);
                        rValues.put("recurrence", recurrence);
                        rValues.put("offset_days", offsetDays);
                        rValues.put("status", status);
                        rValues.put("category_id", catId != -1 ? catId : null);
                        rValues.put("user_id", userId);
                        db.insert("reminders_new", null, rValues);
                    }
                    remCursor.close();
                }
                db.execSQL("DROP TABLE reminders;");
                db.execSQL("ALTER TABLE reminders_new RENAME TO reminders;");

                db.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                db.endTransaction();
            }
        }
    }
}
