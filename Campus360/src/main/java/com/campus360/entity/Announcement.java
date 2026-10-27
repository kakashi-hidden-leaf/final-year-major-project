package com.campus360.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
public class Announcement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long announcementId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "target_class_id")
    private ClassGroup targetClassGroup; // null means for All Classes

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by_teacher_id")
    private Teacher createdByTeacher;

    @Column(nullable = false)
    private LocalDate publishDate;

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private boolean isNotificationSent = false;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Announcement() {}

    public Announcement(Long announcementId, String title, String message, ClassGroup targetClassGroup,
                        Teacher createdByTeacher, LocalDate publishDate, LocalDate expiryDate,
                        boolean isNotificationSent, LocalDateTime createdAt) {
        this.announcementId = announcementId;
        this.title = title;
        this.message = message;
        this.targetClassGroup = targetClassGroup;
        this.createdByTeacher = createdByTeacher;
        this.publishDate = publishDate;
        this.expiryDate = expiryDate;
        this.isNotificationSent = isNotificationSent;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public static AnnouncementBuilder builder() {
        return new AnnouncementBuilder();
    }

    public static class AnnouncementBuilder {
        private Long announcementId;
        private String title;
        private String message;
        private ClassGroup targetClassGroup;
        private Teacher createdByTeacher;
        private LocalDate publishDate;
        private LocalDate expiryDate;
        private boolean isNotificationSent = false;
        private LocalDateTime createdAt = LocalDateTime.now();

        public AnnouncementBuilder announcementId(Long announcementId) { this.announcementId = announcementId; return this; }
        public AnnouncementBuilder title(String title) { this.title = title; return this; }
        public AnnouncementBuilder message(String message) { this.message = message; return this; }
        public AnnouncementBuilder targetClassGroup(ClassGroup targetClassGroup) { this.targetClassGroup = targetClassGroup; return this; }
        public AnnouncementBuilder createdByTeacher(Teacher createdByTeacher) { this.createdByTeacher = createdByTeacher; return this; }
        public AnnouncementBuilder publishDate(LocalDate publishDate) { this.publishDate = publishDate; return this; }
        public AnnouncementBuilder expiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; return this; }
        public AnnouncementBuilder isNotificationSent(boolean isNotificationSent) { this.isNotificationSent = isNotificationSent; return this; }
        public AnnouncementBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Announcement build() {
            return new Announcement(announcementId, title, message, targetClassGroup, createdByTeacher, publishDate, expiryDate, isNotificationSent, createdAt);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public boolean isCurrentlyActive() {
        LocalDate today = LocalDate.now();
        return (publishDate == null || !publishDate.isAfter(today)) &&
               (expiryDate == null || !expiryDate.isBefore(today));
    }

    public Long getAnnouncementId() { return announcementId; }
    public void setAnnouncementId(Long announcementId) { this.announcementId = announcementId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public ClassGroup getTargetClassGroup() { return targetClassGroup; }
    public void setTargetClassGroup(ClassGroup targetClassGroup) { this.targetClassGroup = targetClassGroup; }
    public Teacher getCreatedByTeacher() { return createdByTeacher; }
    public void setCreatedByTeacher(Teacher createdByTeacher) { this.createdByTeacher = createdByTeacher; }
    public LocalDate getPublishDate() { return publishDate; }
    public void setPublishDate(LocalDate publishDate) { this.publishDate = publishDate; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public boolean isNotificationSent() { return isNotificationSent; }
    public void setNotificationSent(boolean notificationSent) { isNotificationSent = notificationSent; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
