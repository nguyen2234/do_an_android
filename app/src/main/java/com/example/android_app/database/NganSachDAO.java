package com.example.android_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.android_app.model.NganSach;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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

    
    private long getCurrentUserId() {
        if (context == null) return 1;
        android.content.SharedPreferences prefs =
                context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        return prefs.getLong("user_id", 1);
    }

    
    
    

    public long addNganSach(NganSach budget) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_BUDGET_NAME,       budget.getName());
            values.put(DatabaseHelper.COL_BUDGET_AMOUNT,     budget.getAmount());
            values.put(DatabaseHelper.COL_BUDGET_SPENT,      budget.getSpentAmount());
            values.put(DatabaseHelper.COL_BUDGET_START,      budget.getStartDate());
            values.put(DatabaseHelper.COL_BUDGET_END,        budget.getEndDate());
            values.put(DatabaseHelper.COL_USER_ID_FK,        getCurrentUserId());
            
            long budgetId = db.insert(DatabaseHelper.TABLE_BUDGETS, null, values);
            if (budgetId > 0) {
                
                for (com.example.android_app.model.DanhMuc c : budget.getCategories()) {
                    long catId = c.getId();
                    if (catId <= 0) {
                        Cursor catCursor = db.rawQuery(
                                "SELECT id FROM categories WHERE name = ? AND user_id = ?",
                                new String[]{c.getName().trim(), String.valueOf(getCurrentUserId())});
                        if (catCursor != null && catCursor.moveToFirst()) {
                            catId = catCursor.getLong(0);
                        }
                        if (catCursor != null) catCursor.close();
                    }
                    if (catId > 0) {
                        ContentValues bcValues = new ContentValues();
                        bcValues.put(DatabaseHelper.COL_BC_BUDGET_ID, budgetId);
                        bcValues.put(DatabaseHelper.COL_BC_CATEGORY_ID, catId);
                        db.insert(DatabaseHelper.TABLE_BUDGET_CATEGORIES, null, bcValues);
                    }
                }
            }
            db.setTransactionSuccessful();
            return budgetId;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            db.endTransaction();
        }
    }

    
    
    

    
    public int updateSpentAmount(int id, double newSpentAmount) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COL_BUDGET_SPENT, newSpentAmount);
        return db.update(
                DatabaseHelper.TABLE_BUDGETS, values,
                DatabaseHelper.COL_BUDGET_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(id), String.valueOf(getCurrentUserId())});
    }

    
    public int updateNganSach(NganSach budget) {
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_BUDGET_NAME,       budget.getName());
            values.put(DatabaseHelper.COL_BUDGET_AMOUNT,     budget.getAmount());
            values.put(DatabaseHelper.COL_BUDGET_START,      budget.getStartDate());
            values.put(DatabaseHelper.COL_BUDGET_END,        budget.getEndDate());
            
            int rows = db.update(
                    DatabaseHelper.TABLE_BUDGETS, values,
                    DatabaseHelper.COL_BUDGET_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                    new String[]{String.valueOf(budget.getId()), String.valueOf(getCurrentUserId())});
            
            if (rows > 0) {
                
                db.delete(DatabaseHelper.TABLE_BUDGET_CATEGORIES,
                        DatabaseHelper.COL_BC_BUDGET_ID + " = ?",
                        new String[]{String.valueOf(budget.getId())});
                
                
                for (com.example.android_app.model.DanhMuc c : budget.getCategories()) {
                    long catId = c.getId();
                    if (catId <= 0) {
                        Cursor catCursor = db.rawQuery(
                                "SELECT id FROM categories WHERE name = ? AND user_id = ?",
                                new String[]{c.getName().trim(), String.valueOf(getCurrentUserId())});
                        if (catCursor != null && catCursor.moveToFirst()) {
                            catId = catCursor.getLong(0);
                        }
                        if (catCursor != null) catCursor.close();
                    }
                    if (catId > 0) {
                        ContentValues bcValues = new ContentValues();
                        bcValues.put(DatabaseHelper.COL_BC_BUDGET_ID, budget.getId());
                        bcValues.put(DatabaseHelper.COL_BC_CATEGORY_ID, catId);
                        db.insert(DatabaseHelper.TABLE_BUDGET_CATEGORIES, null, bcValues);
                    }
                }
            }
            db.setTransactionSuccessful();
            return rows;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            db.endTransaction();
        }
    }

    
    
    

    public int deleteNganSach(int budgetId) {
        db.beginTransaction();
        try {
            
            db.delete(DatabaseHelper.TABLE_BUDGET_CATEGORIES,
                    DatabaseHelper.COL_BC_BUDGET_ID + " = ?",
                    new String[]{String.valueOf(budgetId)});
            
            int rows = db.delete(
                    DatabaseHelper.TABLE_BUDGETS,
                    DatabaseHelper.COL_BUDGET_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                    new String[]{String.valueOf(budgetId), String.valueOf(getCurrentUserId())});
            
            db.setTransactionSuccessful();
            return rows;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            db.endTransaction();
        }
    }

    
    
    

    public NganSach getBudgetById(int budgetId) {
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_BUDGETS, null,
                DatabaseHelper.COL_BUDGET_ID + " = ? AND " + DatabaseHelper.COL_USER_ID_FK + " = ?",
                new String[]{String.valueOf(budgetId), String.valueOf(getCurrentUserId())},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            NganSach budget = mapCursorToBudget(cursor);
            cursor.close();
            budget.setCategories(getCategoriesForBudget(budget.getId()));
            
            double calculatedSpent = calculateSpentAmountForBudget(budget.getId(), budget.getStartDate(), budget.getEndDate());
            budget.setSpentAmount(calculatedSpent);
            
            return budget;
        }
        if (cursor != null) cursor.close();
        return null;
    }

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
                NganSach budget = mapCursorToBudget(cursor);
                budget.setCategories(getCategoriesForBudget(budget.getId()));
                
                double calculatedSpent = calculateSpentAmountForBudget(budget.getId(), budget.getStartDate(), budget.getEndDate());
                budget.setSpentAmount(calculatedSpent);
                updateSpentAmount(budget.getId(), calculatedSpent);
                
                list.add(budget);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public List<com.example.android_app.model.DanhMuc> getCategoriesForBudget(int budgetId) {
        List<com.example.android_app.model.DanhMuc> list = new ArrayList<>();
        String sql = "SELECT c.id, c.name, c.icon, c.type, c.color " +
                "FROM categories c " +
                "INNER JOIN budget_categories bc ON c.id = bc.category_id " +
                "WHERE bc.budget_id = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(budgetId)});
        if (cursor != null && cursor.moveToFirst()) {
            do {
                com.example.android_app.model.DanhMuc c = new com.example.android_app.model.DanhMuc();
                c.setId(cursor.getInt(0));
                c.setName(cursor.getString(1));
                c.setIcon(cursor.getString(2));
                c.setLoai(cursor.getString(3));
                c.setColor(cursor.getInt(4));
                list.add(c);
            } while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }

    public double calculateSpentAmountForBudget(int budgetId, String startDate, String endDate) {
        double spent = 0;
        
        
        String startDateIso = startDate;
        String endDateIso = endDate;
        try {
            SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi"));
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            
            
            String startClean = startDate.trim().substring(0, Math.min(startDate.trim().length(), 10));
            String endClean = endDate.trim().substring(0, Math.min(endDate.trim().length(), 10));
            
            startDateIso = formatter.format(parser.parse(startClean));
            endDateIso = formatter.format(parser.parse(endClean));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String sql = "SELECT SUM(t.amount) FROM transactions t " +
                "INNER JOIN budget_categories bc ON t.category_id = bc.category_id " +
                "WHERE bc.budget_id = ? " +
                "AND t.type = 'expense' " +
                "AND t.user_id = ? " +
                "AND (substr(t.date, 7, 4) || '-' || substr(t.date, 4, 2) || '-' || substr(t.date, 1, 2)) " +
                "BETWEEN ? AND ?";
        
        Cursor cursor = db.rawQuery(sql, new String[]{
                String.valueOf(budgetId),
                String.valueOf(getCurrentUserId()),
                startDateIso,
                endDateIso
        });
        
        if (cursor != null && cursor.moveToFirst()) {
            spent = cursor.getDouble(0);
            cursor.close();
        }
        return spent;
    }

    
    
    

    public Set<String> getUsedCategoryNames() {
        Set<String> usedNames = new HashSet<>();
        String sql = "SELECT c.name FROM categories c " +
                "INNER JOIN budget_categories bc ON c.id = bc.category_id " +
                "INNER JOIN budgets b ON bc.budget_id = b.id " +
                "WHERE b.user_id = ?";
        Cursor cursor = db.rawQuery(sql, new String[]{String.valueOf(getCurrentUserId())});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String catName = cursor.getString(0);
                if (catName != null) {
                    usedNames.add(catName.trim());
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return usedNames;
    }

    public Set<String> getUsedCategoryNamesExcluding(int excludeBudgetId) {
        Set<String> usedNames = new HashSet<>();
        String sql = "SELECT c.name FROM categories c " +
                "INNER JOIN budget_categories bc ON c.id = bc.category_id " +
                "INNER JOIN budgets b ON bc.budget_id = b.id " +
                "WHERE b.user_id = ? AND b.id != ?";
        Cursor cursor = db.rawQuery(sql, new String[]{
                String.valueOf(getCurrentUserId()),
                String.valueOf(excludeBudgetId)
        });

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String catName = cursor.getString(0);
                if (catName != null) {
                    usedNames.add(catName.trim());
                }
            } while (cursor.moveToNext());
            cursor.close();
        }
        return usedNames;
    }

    
    
    

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
        return b;
    }
}
