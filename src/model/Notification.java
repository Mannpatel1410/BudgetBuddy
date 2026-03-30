package model;

import java.time.LocalDateTime;

public class Notification {
    private long id;
    private long userId;
    private long budgetId;
    private String message;
    private String alertType;
    private boolean isRead;
    private LocalDateTime createdAt;

    public Notification() {}

    public Notification(long userId, long budgetId, String message, String alertType) {
        this.userId = userId;
        this.budgetId = budgetId;
        this.message = message;
        this.alertType = alertType;
        this.isRead = false;
        this.createdAt = LocalDateTime.now();
    }

    public void markRead() {
        this.isRead = true;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public long getBudgetId() { return budgetId; }
    public void setBudgetId(long budgetId) { this.budgetId = budgetId; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getAlertType() { return alertType; }
    public void setAlertType(String alertType) { this.alertType = alertType; }
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
