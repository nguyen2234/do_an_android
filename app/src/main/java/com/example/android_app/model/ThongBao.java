package com.example.android_app.model;

/**
 * Lớp model đại diện cho một thông báo (Notification) trong ứng dụng.
 */
public class ThongBao {
    private int id;
    private String title;
    private String content;
    private String date;       // Định dạng dd/MM/yyyy HH:mm
    private boolean isRead;    // true nếu đã đọc, false nếu chưa đọc
    private String type;       // system, warning, transaction, reminder
    private long userId;       // Khóa ngoại trỏ đến bảng users

    // Constructor mặc định
    public ThongBao() {
    }

    // Constructor đầy đủ
    public ThongBao(int id, String title, String content, String date, boolean isRead, String type, long userId) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.date = date;
        this.isRead = isRead;
        this.type = type;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }
}
