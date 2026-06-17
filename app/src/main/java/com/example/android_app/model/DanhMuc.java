package com.example.android_app.model;


public class DanhMuc {
    private long id;            
    private String name;        
    private String icon;        
    private String type;        
    private int color;          

    public DanhMuc() {
        
    }

    public DanhMuc(long id, String name, String icon, String type, int color) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.type = type;
        this.color = color;
    }

    
    public DanhMuc(long id, String name, String icon, int color) {
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.type = "general";
        this.color = color;
    }

    
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public String getLoai() { return type; }
    public void setLoai(String type) { this.type = type; }

    public int getColor() { return color; }
    public void setColor(int color) { this.color = color; }
}
