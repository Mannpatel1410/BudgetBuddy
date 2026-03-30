package service;

import dao.NotificationDAO;
import model.Notification;

import java.util.List;

public class NotificationService {
    private NotificationDAO notificationDAO = new NotificationDAO();

    public List<Notification> getNotifications(long userId) {
        return notificationDAO.findByUserId(userId);
    }

    public List<Notification> getUnread(long userId) {
        return notificationDAO.findUnreadByUserId(userId);
    }

    public void markAsRead(long notificationId) {
        notificationDAO.markAsRead(notificationId);
    }
}