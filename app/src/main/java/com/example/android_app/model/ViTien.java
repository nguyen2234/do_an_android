package com.example.android_app.model;

/**
 * Lớp đại diện cho một Ví tiền.
 */
public class ViTien {
    private long id;            // ID ví trong cơ sở dữ liệu
    private String name;        // Tên ví (vd: Tiền mặt, Thẻ ngân hàng)
    private double balance;     // Số dư hiện tại của ví
    private String type;        // Loại ví: "cash" (tiền mặt), "bank" (ngân hàng), "saving" (tiết kiệm)
    private String currency;    // Đơn vị tiền tệ (vd: VNĐ, USD)
    private String icon;        // Tên icon
    private String color;       // Màu sắc (mã hex)

    public ViTien() {
        // Constructor mặc định
    }

    public ViTien(long id, String name, double balance, String type, String currency, String icon, String color) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.type = type;
        this.currency = currency;
        this.icon = icon;
        this.color = color;
    }

    // Constructor cũ cho tương thích (hoặc gán mặc định icon/color)
    public ViTien(long id, String name, double balance, String type, String currency) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.type = type;
        this.currency = currency;
        this.icon = "cash";
        this.color = "#4CAF50"; // Màu xanh mặc định
    }

    // Các hàm Getter và Setter (Lấy và Gán giá trị)
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }

    public String getLoai() { return type; }
    public void setLoai(String type) { this.type = type; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
