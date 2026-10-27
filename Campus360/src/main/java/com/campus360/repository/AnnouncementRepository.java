package com.campus360.repository;

import com.campus360.entity.Announcement;
import com.campus360.entity.ClassGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByCreatedByTeacher_TeacherIdOrderByCreatedAtDesc(Long teacherId);

    // Active announcements for a specific class or global (targetClassGroup is null)
    @Query("SELECT a FROM Announcement a WHERE " +
           "(a.targetClassGroup IS NULL OR a.targetClassGroup.classId = :classId) AND " +
           "a.publishDate <= :today AND a.expiryDate >= :today ORDER BY a.publishDate DESC, a.createdAt DESC")
    List<Announcement> findActiveForClass(@Param("classId") Long classId, @Param("today") LocalDate today);

    // Past / expired announcements for a specific class or global
    @Query("SELECT a FROM Announcement a WHERE " +
           "(a.targetClassGroup IS NULL OR a.targetClassGroup.classId = :classId) AND " +
           "a.expiryDate < :today ORDER BY a.expiryDate DESC")
    List<Announcement> findPastForClass(@Param("classId") Long classId, @Param("today") LocalDate today);

    // Find announcements that need notification dispatched
    @Query("SELECT a FROM Announcement a WHERE a.publishDate <= :today AND a.expiryDate >= :today AND a.isNotificationSent = false")
    List<Announcement> findPendingNotificationAnnouncements(@Param("today") LocalDate today);

    List<Announcement> findAllByOrderByCreatedAtDesc();

    void deleteByCreatedByTeacher(com.campus360.entity.Teacher teacher);
}
