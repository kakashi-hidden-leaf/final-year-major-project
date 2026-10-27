package com.campus360.entity;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "teacher_schedules")
public class TeacherSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long scheduleId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassGroup classGroup;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "location_id", nullable = false)
    private CampusLocation campusLocation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(name = "last_reminded_date")
    private LocalDate lastRemindedDate;

    public TeacherSchedule() {}

    public TeacherSchedule(Long scheduleId, Teacher teacher, ClassGroup classGroup, Subject subject,
                           CampusLocation campusLocation, DayOfWeek dayOfWeek, LocalTime startTime,
                           LocalTime endTime, LocalDate lastRemindedDate) {
        this.scheduleId = scheduleId;
        this.teacher = teacher;
        this.classGroup = classGroup;
        this.subject = subject;
        this.campusLocation = campusLocation;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lastRemindedDate = lastRemindedDate;
    }

    public static TeacherScheduleBuilder builder() {
        return new TeacherScheduleBuilder();
    }

    public static class TeacherScheduleBuilder {
        private Long scheduleId;
        private Teacher teacher;
        private ClassGroup classGroup;
        private Subject subject;
        private CampusLocation campusLocation;
        private DayOfWeek dayOfWeek;
        private LocalTime startTime;
        private LocalTime endTime;
        private LocalDate lastRemindedDate;

        public TeacherScheduleBuilder scheduleId(Long scheduleId) { this.scheduleId = scheduleId; return this; }
        public TeacherScheduleBuilder teacher(Teacher teacher) { this.teacher = teacher; return this; }
        public TeacherScheduleBuilder classGroup(ClassGroup classGroup) { this.classGroup = classGroup; return this; }
        public TeacherScheduleBuilder subject(Subject subject) { this.subject = subject; return this; }
        public TeacherScheduleBuilder campusLocation(CampusLocation campusLocation) { this.campusLocation = campusLocation; return this; }
        public TeacherScheduleBuilder dayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; return this; }
        public TeacherScheduleBuilder startTime(LocalTime startTime) { this.startTime = startTime; return this; }
        public TeacherScheduleBuilder endTime(LocalTime endTime) { this.endTime = endTime; return this; }
        public TeacherScheduleBuilder lastRemindedDate(LocalDate lastRemindedDate) { this.lastRemindedDate = lastRemindedDate; return this; }

        public TeacherSchedule build() {
            return new TeacherSchedule(scheduleId, teacher, classGroup, subject, campusLocation, dayOfWeek, startTime, endTime, lastRemindedDate);
        }
    }

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
    public ClassGroup getClassGroup() { return classGroup; }
    public void setClassGroup(ClassGroup classGroup) { this.classGroup = classGroup; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public CampusLocation getCampusLocation() { return campusLocation; }
    public void setCampusLocation(CampusLocation campusLocation) { this.campusLocation = campusLocation; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    public LocalDate getLastRemindedDate() { return lastRemindedDate; }
    public void setLastRemindedDate(LocalDate lastRemindedDate) { this.lastRemindedDate = lastRemindedDate; }
}
