package com.example.android_app.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model đại diện cho một Nhắc hẹn thanh toán (Payment Reminder).
 */
public class Reminder {
    private long id;
    private String title;                  // Tên khoản chi
    private double estimatedAmount;        // Số tiền dự kiến
    private LocalDateTime dueDate;         // Ngày giờ đến hạn
    private Recurrence recurrence;         // Tần suất lặp lại (MONTHLY, QUARTERLY, YEARLY)
    private int reminderOffsetDays;        // Thời gian nhắc trước (số ngày)
    private ReminderStatus status;         // Trạng thái (PENDING, PAID)
    private long categoryId;               // ID danh mục liên kết
    private String categoryName;           // Tên danh mục liên kết (lấy từ JOIN)
    private long userId;                   // ID người dùng liên kết

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public Reminder() {
        this.status = ReminderStatus.PENDING;
        this.reminderOffsetDays = 0;
    }

    public Reminder(long id, String title, double estimatedAmount, LocalDateTime dueDate,
                    Recurrence recurrence, int reminderOffsetDays, ReminderStatus status,
                    long categoryId, String categoryName, long userId) {
        this.id = id;
        this.title = title;
        this.estimatedAmount = estimatedAmount;
        this.dueDate = dueDate;
        this.recurrence = recurrence;
        this.reminderOffsetDays = reminderOffsetDays;
        this.status = status != null ? status : ReminderStatus.PENDING;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.userId = userId;
    }

    public Reminder(long id, String title, double estimatedAmount, LocalDateTime dueDate,
                    Recurrence recurrence, int reminderOffsetDays, ReminderStatus status,
                    String categoryName, long userId) {
        this.id = id;
        this.title = title;
        this.estimatedAmount = estimatedAmount;
        this.dueDate = dueDate;
        this.recurrence = recurrence;
        this.reminderOffsetDays = reminderOffsetDays;
        this.status = status != null ? status : ReminderStatus.PENDING;
        this.categoryId = 0;
        this.categoryName = categoryName;
        this.userId = userId;
    }

    // Getters & Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public double getEstimatedAmount() { return estimatedAmount; }
    public void setEstimatedAmount(double estimatedAmount) { this.estimatedAmount = estimatedAmount; }

    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }

    public Recurrence getRecurrence() { return recurrence; }
    public void setRecurrence(Recurrence recurrence) { this.recurrence = recurrence; }

    public int getReminderOffsetDays() { return reminderOffsetDays; }
    public void setReminderOffsetDays(int reminderOffsetDays) { this.reminderOffsetDays = reminderOffsetDays; }

    public ReminderStatus getStatus() { return status; }
    public void setStatus(ReminderStatus status) { this.status = status; }

    public long getCategoryId() { return categoryId; }
    public void setCategoryId(long categoryId) { this.categoryId = categoryId; }

    // Tương thích ngược với mã nguồn cũ gọi getCategory() để lấy tên hiển thị
    public String getCategory() { return categoryName; }
    public void setCategory(String categoryName) { this.categoryName = categoryName; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }

    // Helpers cho Cơ sở dữ liệu SQLite
    public String getDueDateString() {
        if (dueDate == null) return null;
        return dueDate.format(formatter);
    }

    public void setDueDateFromString(String dueDateStr) {
        if (dueDateStr == null || dueDateStr.isEmpty()) {
            this.dueDate = null;
        } else {
            this.dueDate = LocalDateTime.parse(dueDateStr, formatter);
        }
    }

    public boolean isPending() {
        return ReminderStatus.PENDING == status;
    }

    public boolean isPaid() {
        return ReminderStatus.PAID == status;
    }
}
