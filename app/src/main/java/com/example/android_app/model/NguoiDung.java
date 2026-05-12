package com.example.android_app.model;

/**
 * Lớp đại diện cho Người dùng (NguoiDung) trong hệ thống.
 */
public class NguoiDung {
    private long id;
    private String username;
    private String password;
    private String email;
    private String avatar;
    private int themeMode; // 0: Auto, 1: Light, 2: Dark

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
}
