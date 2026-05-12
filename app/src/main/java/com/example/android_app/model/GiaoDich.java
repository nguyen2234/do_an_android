package com.example.android_app.model;

/**
 * Lớp đại diện cho một Giao dịch.
 */
public class GiaoDich {
    private long id;            // ID giao dịch trong cơ sở dữ liệu
    private String title;       // Tiêu đề/Tên giao dịch
    private double amount;      // Số tiền giao dịch
    private String category;    // Tên danh mục của giao dịch
    private String type;        // Loại: "income" (thu nhập) hoặc "expense" (chi tiêu)
    private String date;        // Ngày thực hiện giao dịch (định dạng dd/MM/yyyy)
    private String note;        // Ghi chú thêm
    private long walletId;      // ID của ví thực hiện giao dịch này

    public GiaoDich() {
        // Constructor mặc định
    }

    public GiaoDich(long id, String title, double amount, String category,
                       String type, String date, String note, long walletId) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.type = type;
        this.date = date;
        this.note = note;
        this.walletId = walletId;
    }

    // Các hàm Getter và Setter (Lấy và Gán giá trị)
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getSoTien() { return amount; }
    public void setSoTien(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getLoai() { return type; }
    public void setLoai(String type) { this.type = type; }

    public String getNgay() { return date; }
    public void setNgay(String date) { this.date = date; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public long getWalletId() { return walletId; }
    public void setWalletId(long walletId) { this.walletId = walletId; }

    // Kiểm tra loại giao dịch
    public boolean isExpense() { return "expense".equals(type); }
    public boolean isIncome() { return "income".equals(type); }
}
