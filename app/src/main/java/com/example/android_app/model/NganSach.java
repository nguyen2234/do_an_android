package com.example.android_app.model;

public class NganSach {
    private int id;
    private String name;
    private double amount;
    private double spentAmount;
    private String startDate;
    private String endDate;
    private String categoryIds;

    public NganSach() {
    }

    public NganSach(int id, String name, double amount, double spentAmount, String startDate, String endDate, String categoryIds) {
        this.id = id;
        this.name = name;
        this.amount = amount;
        this.spentAmount = spentAmount;
        this.startDate = startDate;
        this.endDate = endDate;
        this.categoryIds = categoryIds;
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

    public String getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(String categoryIds) {
        this.categoryIds = categoryIds;
    }
}
