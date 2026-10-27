package com.campus360.repository;

import com.campus360.entity.Notification;
import com.campus360.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientUserOrderByCreatedAtDesc(User recipientUser);

    List<Notification> findByRecipientUser_UserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findTop10ByRecipientUser_UserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByRecipientUser_UserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    long countByRecipientUser_UserIdAndIsReadFalse(Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientUser.userId = :userId")
    void markAllAsReadForUser(@Param("userId") Long userId);

    void deleteByRecipientUser(User recipientUser);
}
