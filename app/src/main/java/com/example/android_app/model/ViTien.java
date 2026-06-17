package com.example.android_app.model;


public class ViTien {
    private long id;            
    private String name;        
    private double balance;     
    private String type;        
    private String currency;    
    private String icon;        
    private String color;       
    private double minBalance;  

    public ViTien() {
        
    }

    public ViTien(long id, String name, double balance, String type, String currency, String icon, String color, double minBalance) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.type = type;
        this.currency = currency;
        this.icon = icon;
        this.color = color;
        this.minBalance = minBalance;
    }

    
    public ViTien(long id, String name, double balance, String type, String currency) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.type = type;
        this.currency = currency;
        this.icon = "cash";
        this.color = "#4CAF50"; 
    }

    
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

    public double getMinBalance() { return minBalance; }
    public void setMinBalance(double minBalance) { this.minBalance = minBalance; }
}
