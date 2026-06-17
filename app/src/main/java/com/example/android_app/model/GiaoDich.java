package com.example.android_app.model;


public class GiaoDich {
    private long id;            
    private String title;       
    private double amount;      
    private long categoryId;    
    private String categoryName;
    private String type;        
    private String date;        
    private String note;        
    private long walletId;      

    public GiaoDich() {
        
    }

    public GiaoDich(long id, String title, double amount, long categoryId, String categoryName,
                       String type, String date, String note, long walletId) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.type = type;
        this.date = date;
        this.note = note;
        this.walletId = walletId;
    }

    public GiaoDich(long id, String title, double amount, String category,
                       String type, String date, String note, long walletId) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.categoryName = category;
        this.categoryId = 0;
        this.type = type;
        this.date = date;
        this.note = note;
        this.walletId = walletId;
    }

    
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getSoTien() { return amount; }
    public void setSoTien(double amount) { this.amount = amount; }

    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }

    
    public String getCategory() { return categoryName; }
    public void setCategory(String categoryName) { this.categoryName = categoryName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getLoai() { return type; }
    public void setLoai(String type) { this.type = type; }

    public String getNgay() { return date; }
    public void setNgay(String date) { this.date = date; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public long getWalletId() { return walletId; }
    public void setWalletId(long walletId) { this.walletId = walletId; }

    
    public boolean isExpense() { return "expense".equals(type); }
    public boolean isIncome() { return "income".equals(type); }
}
