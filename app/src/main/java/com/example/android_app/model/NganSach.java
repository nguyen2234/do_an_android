package com.example.android_app.model;

import java.util.ArrayList;
import java.util.List;

public class NganSach {
    private int id;
    private String name;
    private double amount;
    private double spentAmount;
    private String startDate;
    private String endDate;
    private List<DanhMuc> categories = new ArrayList<>();

    public NganSach() {
    }

    public NganSach(int id, String name, double amount, double spentAmount, String startDate, String endDate, String categoryIdsStr) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.spentAmount = spentAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        setCategoryIds(categoryIdsStr);
    }

    public NganSach(int id, String name, double amount, double spentAmount, String startDate, String endDate, List<DanhMuc> categories) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.spentAmount = spentAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        if (categories != null) {
            this.categories = categories;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public List<DanhMuc> getCategories() {
        return categories;
    }

    public void setCategories(List<DanhMuc> categories) {
        if (categories != null) {
            this.categories = categories;
        }
    }

    // Tương thích ngược với mã nguồn cũ gọi getCategoryIds() trả về chuỗi tên danh mục phân tách bằng dấu phẩy
    public String getCategoryIds() {
        if (categories == null || categories.isEmpty()) return "";
        List<String> names = new ArrayList<>();
        for (DanhMuc c : categories) {
            names.add(c.getName());
        }
        return android.text.TextUtils.join(", ", names);
    }

    // Tương thích ngược với mã nguồn cũ gọi setCategoryIds(String)
    public void setCategoryIds(String categoryIdsStr) {
        this.categories = new ArrayList<>();
        if (categoryIdsStr != null && !categoryIdsStr.trim().isEmpty()) {
            for (String name : categoryIdsStr.split(",")) {
                String cleanName = name.trim();
                if (!cleanName.isEmpty()) {
                    this.categories.add(new DanhMuc(0, cleanName, "", "general", 0));
                }
            }
        }
    }
}
