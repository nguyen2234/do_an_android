package com.example.android_app.model;


public class NguoiDung {
    private long id;
    private String username;
    private String password;
    private String email;
    private String avatar;
    private int themeMode; 
    private String fullname;
    private String transactionPin;

    public NguoiDung() {
    }

    public NguoiDung(long id, String username, String password, String email, String avatar, int themeMode) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.avatar = avatar;
        this.themeMode = themeMode;
    }

    public NguoiDung(long id, String username, String password, String email, String avatar, int themeMode, String fullname) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.avatar = avatar;
        this.themeMode = themeMode;
        this.fullname = fullname;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public int getThemeMode() { return themeMode; }
    public void setThemeMode(int themeMode) { this.themeMode = themeMode; }

    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getTransactionPin() { return transactionPin; }
    public void setTransactionPin(String transactionPin) { this.transactionPin = transactionPin; }
}
