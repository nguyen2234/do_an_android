package com.example.android_app.model;

/**
 * Lớp đại diện cho một Danh mục (thu nhập hoặc chi tiêu).
 */
public class Category {
    private long id;            // ID danh mục trong cơ sở dữ liệu
    private String name;        // Tên danh mục (vd: Ăn uống, Mua sắm)
    private String icon;        // Tên icon (dùng để map với drawable)
    private String type;        // Loại danh mục: "income" (thu nhập) hoặc "expense" (chi tiêu)
    private int color;          // Mã màu hiển thị cho danh mục

    public Category() {
        // Constructor mặc định cần thiết cho một số thao tác
    }

    public Category(long id, String name, String icon, String type, int color) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.type = type;
        this.color = color;
    }

    // Các hàm Getter và Setter (Lấy và Gán giá trị)
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
}
