package com.campus360.service;

import com.campus360.entity.Notification;
import com.campus360.entity.User;
import com.campus360.enums.NotificationType;
import com.campus360.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public Notification sendNotification(User recipient, String title, String message, NotificationType type) {
        if (recipient == null) return null;
        Notification notification = Notification.builder()
                .recipientUser(recipient)
                .title(title)
                .message(message)
                .notificationType(type != null ? type : NotificationType.GENERAL)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        return notificationRepository.save(notification);
    }

    @Transactional
    public void sendNotificationToUsers(List<User> recipients, String title, String message, NotificationType type) {
        if (recipients == null || recipients.isEmpty()) return;
        List<Notification> notifications = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (User u : recipients) {
            notifications.add(Notification.builder()
                    .recipientUser(u)
                    .title(title)
                    .message(message)
                    .notificationType(type != null ? type : NotificationType.GENERAL)
                    .isRead(false)
                    .createdAt(now)
                    .build());
        }
        notificationRepository.saveAll(notifications);
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByRecipientUser_UserIdAndIsReadFalse(userId);
    }

    @Transactional(readOnly = true)
    public List<Notification> getRecentNotifications(Long userId) {
        return notificationRepository.findTop10ByRecipientUser_UserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(n -> {
            n.setRead(true);
            notificationRepository.save(n);
        });
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadForUser(userId);
    }
}
