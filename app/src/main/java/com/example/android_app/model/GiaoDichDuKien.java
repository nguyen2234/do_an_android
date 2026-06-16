package com.example.android_app.model;

/**
 * Model đại diện cho một Giao dịch Dự kiến (khoản thu/chi đến hạn).
 */
public class GiaoDichDuKien {
    private long id;
    private String title;       // Tên khoản (vd: Tiền điện, Tiền thuê nhà)
    private double amount;      // Số tiền
    private long categoryId;    // ID danh mục của giao dịch dự kiến
    private String categoryName;// Tên danh mục của giao dịch dự kiến (lấy từ JOIN)
    private String type;        // "income" (thu) hoặc "expense" (chi)
    private String dueDate;     // Ngày đến hạn (dd/MM/yyyy)
    private String status;      // "pending" hoặc "completed"
    private String note;        // Ghi chú
    private long walletId;      // ID ví liên kết

    public GiaoDichDuKien() {}

    public GiaoDichDuKien(long id, String title, double amount, long categoryId, String categoryName,
                           String type, String dueDate, String status, String note, long walletId) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.type = type;
        this.dueDate = dueDate;
        this.status = status;
        this.note = note;
        this.walletId = walletId;
    }

    public GiaoDichDuKien(long id, String title, double amount, String category,
                           String type, String dueDate, String status, String note, long walletId) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.categoryName = category;
        this.categoryId = 0;
        this.type = type;
        this.dueDate = dueDate;
        this.status = status;
        this.note = note;
        this.walletId = walletId;
    }

    // Getters & Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }

    // Tương thích ngược với mã nguồn cũ gọi getCategory() để lấy tên hiển thị
    public String getCategory() { return categoryName; }
    public void setCategory(String categoryName) { this.categoryName = categoryName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public long getWalletId() { return walletId; }
    public void setWalletId(long walletId) { this.walletId = walletId; }

    /** Kiểm tra khoản này là chi tiêu không */
    public boolean isExpense() { return "expense".equals(type); }

    /** Kiểm tra khoản này còn pending không */
    public boolean isPending() { return "pending".equals(status); }
}
