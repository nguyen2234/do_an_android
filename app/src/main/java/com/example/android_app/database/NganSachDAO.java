package com.example.android_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.android_app.model.NganSach;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        android.content.SharedPreferences prefs =
                context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return prefs.getLong("user_id", 1);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  THÊM MỚI
    // ──────────────────────────────────────────────────────────────────────────

    public long addNganSach(NganSach budget) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_BUDGET_NAME,       budget.getName());
        values.put(DatabaseHelper.COL_BUDGET_AMOUNT,     budget.getAmount());
        values.put(DatabaseHelper.COL_BUDGET_SPENT,      budget.getSpentAmount());
        values.put(DatabaseHelper.COL_BUDGET_START,      budget.getStartDate());
        values.put(DatabaseHelper.COL_BUDGET_END,        budget.getEndDate());
        values.put(DatabaseHelper.COL_BUDGET_CATEGORIES, budget.getCategoryIds());
        values.put(DatabaseHelper.COL_USER_ID_FK,        getCurrentUserId());
        return db.insert(DatabaseHelper.TABLE_BUDGETS, null, values);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  CẬP NHẬT
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Cập nhật số tiền đã chi của ngân sách (được gọi tự động khi thêm giao dịch).
     */
    public int updateSpentAmount(int id, double newSpentAmount) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_BUDGET_SPENT, newSpentAmount);
        return db.update(
                DatabaseHelper.TABLE_BUDGETS, values,
                DatabaseHelper.COL_BUDGET_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(id), String.valueOf(getCurrentUserId())});
    }

    /**
     * Cập nhật toàn bộ thông tin ngân sách (dùng cho chức năng Sửa).
     * Lưu ý: KHÔNG cập nhật spent_amount để tránh mất dữ liệu đã chi.
     *
     * @param budget Đối tượng NganSach đã chỉnh sửa (id phải hợp lệ)
     * @return Số hàng bị ảnh hưởng (> 0 là thành công)
     */
    public int updateNganSach(NganSach budget) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_BUDGET_NAME,       budget.getName());
        values.put(DatabaseHelper.COL_BUDGET_AMOUNT,     budget.getAmount());
        values.put(DatabaseHelper.COL_BUDGET_START,      budget.getStartDate());
        values.put(DatabaseHelper.COL_BUDGET_END,        budget.getEndDate());
        values.put(DatabaseHelper.COL_BUDGET_CATEGORIES, budget.getCategoryIds());
        return db.update(
                DatabaseHelper.TABLE_BUDGETS, values,
                DatabaseHelper.COL_BUDGET_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(budget.getId()), String.valueOf(getCurrentUserId())});
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  XÓA
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Xóa một ngân sách theo ID.
     *
     * ⚠ QUAN TRỌNG: Chỉ xóa bản ghi trong bảng 'budgets'.
     * Tất cả giao dịch (bảng 'transactions') KHÔNG bị xóa theo.
     *
     * @param budgetId ID ngân sách cần xóa
     * @return Số hàng bị ảnh hưởng (> 0 là thành công)
     */
    public int deleteNganSach(int budgetId) {
        return db.delete(
                DatabaseHelper.TABLE_BUDGETS,
                DatabaseHelper.COL_BUDGET_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(budgetId), String.valueOf(getCurrentUserId())});
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  TRUY VẤN
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Lấy thông tin một ngân sách theo ID.
     *
     * @param budgetId ID ngân sách cần lấy
     * @return Đối tượng NganSach, hoặc null nếu không tìm thấy
     */
    public NganSach getBudgetById(int budgetId) {
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_BUDGETS, null,
                DatabaseHelper.COL_BUDGET_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(budgetId), String.valueOf(getCurrentUserId())},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            NganSach b = mapCursorToBudget(cursor);
            cursor.close();
            return b;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    /**
     * Lấy danh sách tất cả ngân sách của người dùng, sắp xếp mới nhất lên trên.
     */
    public List<NganSach> getAllBudgets() {
        List<NganSach> list = new ArrayList<>();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_BUDGETS, null,
                DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(getCurrentUserId())},
                null, null,
                DatabaseHelper.COL_BUDGET_ID + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(mapCursorToBudget(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  UNIQUE CATEGORY – Kiểm tra danh mục độc quyền
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Lấy tập hợp TÊN danh mục đã được sử dụng trong BẤT KỲ ngân sách nào của người dùng.
     *
     * Dùng khi THÊM MỚI: lọc bỏ tất cả danh mục đã có ngân sách,
     * đảm bảo mỗi danh mục chỉ tồn tại trong đúng 1 ngân sách.
     *
     * @return Set tên danh mục đã được dùng
     */
    public Set<String> getUsedCategoryNames() {
        Set<String> usedNames = new HashSet<>();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_BUDGETS,
                new String[]{DatabaseHelper.COL_BUDGET_CATEGORIES},
                DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(getCurrentUserId())},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String catIds = cursor.getString(0);
                if (catIds != null && !catIds.trim().isEmpty()) {
                    // Tách chuỗi "Ăn uống, Di chuyển" thành từng tên riêng lẻ
                    for (String name : catIds.split(",")) {
                        usedNames.add(name.trim());
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return usedNames;
    }

    /**
     * Lấy tập hợp tên danh mục đã dùng, loại trừ ngân sách có ID chỉ định.
     *
     * Dùng khi CHỈNH SỬA ngân sách: danh mục của chính ngân sách đang sửa
     * sẽ KHÔNG bị lọc bỏ, nên người dùng vẫn có thể chọn lại chúng.
     *
     * @param excludeBudgetId ID ngân sách cần loại trừ khỏi kiểm tra
     * @return Set tên danh mục đã dùng bởi ngân sách KHÁC
     */
    public Set<String> getUsedCategoryNamesExcluding(int excludeBudgetId) {
        Set<String> usedNames = new HashSet<>();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_BUDGETS,
                new String[]{DatabaseHelper.COL_BUDGET_CATEGORIES},
                DatabaseHelper.COL_USER_ID_FK + " = ? AND " + DatabaseHelper.COL_BUDGET_ID + " != ?",
                new String[]{String.valueOf(getCurrentUserId()), String.valueOf(excludeBudgetId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String catIds = cursor.getString(0);
                if (catIds != null && !catIds.trim().isEmpty()) {
                    for (String name : catIds.split(",")) {
                        usedNames.add(name.trim());
                    }
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return usedNames;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  HELPER NỘI BỘ
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Ánh xạ một hàng Cursor thành đối tượng NganSach.
     * Tập trung logic mapping tại một chỗ, tránh lặp code ở nhiều phương thức.
     *
     * @param cursor Con trỏ DB đã được moveToFirst() / moveToNext()
     * @return Đối tượng NganSach tương ứng
     */
    private NganSach mapCursorToBudget(Cursor cursor) {
        NganSach b = new NganSach();
        b.setId(cursor.getInt(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_ID)));
        b.setName(cursor.getString(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_NAME)));
        b.setAmount(cursor.getDouble(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_AMOUNT)));
        b.setSpentAmount(cursor.getDouble(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_SPENT)));
        b.setStartDate(cursor.getString(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_START)));
        b.setEndDate(cursor.getString(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_END)));
        b.setCategoryIds(cursor.getString(
                cursor.getColumnIndexOrThrow(DatabaseHelper.COL_BUDGET_CATEGORIES)));
        return b;
    }
}
