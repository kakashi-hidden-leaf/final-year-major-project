package com.campus360.service;

import com.campus360.entity.*;
import com.campus360.enums.NotificationType;
import com.campus360.repository.AnnouncementRepository;
import com.campus360.repository.StudentRepository;
import com.campus360.repository.TeacherScheduleRepository;
import com.campus360.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CampusSchedulerService {

    private static final Logger log = LoggerFactory.getLogger(CampusSchedulerService.class);

    private final TeacherScheduleRepository scheduleRepository;
    private final AnnouncementRepository announcementRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public CampusSchedulerService(TeacherScheduleRepository scheduleRepository,
                                  AnnouncementRepository announcementRepository,
                                  StudentRepository studentRepository,
                                  UserRepository userRepository,
                                  NotificationService notificationService) {
        this.scheduleRepository = scheduleRepository;
        this.announcementRepository = announcementRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @PostConstruct
    public void onStartup() {
        log.info("Running initial startup activation for announcements...");
        activateScheduledAnnouncements();
    }

    /**
     * 1. 15-Minute Prior Lecture Reminder (Runs every 60 seconds)
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void checkAndSendLectureReminders() {
        LocalDate today = LocalDate.now();
        DayOfWeek currentDay = today.getDayOfWeek();
        LocalTime now = LocalTime.now();
        LocalTime windowStart = now.plusMinutes(14);
        LocalTime windowEnd = now.plusMinutes(16);

        List<TeacherSchedule> slots = scheduleRepository.findSchedulesNeedingReminder(
                currentDay, windowStart, windowEnd, today
        );

        for (TeacherSchedule slot : slots) {
            triggerLectureReminder(slot, today);
        }
    }

    /**
     * 2. Scheduled Announcement Activator (Runs every 60 seconds & on startup)
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void activateScheduledAnnouncements() {
        LocalDate today = LocalDate.now();
        List<Announcement> pendingAnnouncements = announcementRepository.findPendingNotificationAnnouncements(today);

        for (Announcement announcement : pendingAnnouncements) {
            List<Student> targetStudents;
            if (announcement.getTargetClassGroup() != null) {
                targetStudents = studentRepository.findByClassGroup(announcement.getTargetClassGroup());
            } else {
                targetStudents = studentRepository.findAll();
            }

            for (Student student : targetStudents) {
                if (student.getUser() != null) {
                    notificationService.sendNotification(
                            student.getUser(),
                            "New Announcement: " + announcement.getTitle(),
                            announcement.getMessage(),
                            NotificationType.ANNOUNCEMENT
                    );
                }
            }

            announcement.setNotificationSent(true);
            announcementRepository.save(announcement);
            log.info("Dispatched notifications for announcement id: {} to {} students",
                    announcement.getAnnouncementId(), targetStudents.size());
        }
    }

    private void triggerLectureReminder(TeacherSchedule slot, LocalDate date) {
        String startTimeStr = slot.getStartTime().format(TIME_FORMATTER);
        String classGroupName = slot.getClassGroup().getDisplayName();
        String subjectName = slot.getSubject().getSubjectName();
        String floorNumber = slot.getCampusLocation().getFloorNumber();
        String roomNumber = slot.getCampusLocation().getRoomNumber();
        String buildingName = slot.getCampusLocation().getBuildingName();

        // Exact text required by prompt:
        String exactMessage = String.format(
                "Reminder: You have a lecture at %s for %s (%s) at %s, %s (%s).",
                startTimeStr, classGroupName, subjectName, floorNumber, roomNumber, buildingName
        );

        notificationService.sendNotification(
                slot.getTeacher().getUser(),
                "Upcoming Lecture Reminder",
                exactMessage,
                NotificationType.LECTURE_REMINDER
        );

        slot.setLastRemindedDate(date);
        scheduleRepository.save(slot);
        log.info("Sent 15-minute lecture reminder to teacher {}: {}", slot.getTeacher().getFullName(), exactMessage);
    }

    @Transactional
    public boolean simulateLectureAlert(Long teacherId) {
        List<TeacherSchedule> schedules = scheduleRepository.findByTeacher_TeacherIdOrderByDayOfWeekAscStartTimeAsc(teacherId);
        if (schedules.isEmpty()) {
            return false;
        }

        DayOfWeek todayDay = LocalDate.now().getDayOfWeek();
        TeacherSchedule targetSlot = schedules.stream()
                .filter(s -> s.getDayOfWeek() == todayDay)
                .findFirst()
                .orElse(schedules.get(0));

        triggerLectureReminder(targetSlot, LocalDate.now());
        return true;
    }
}
