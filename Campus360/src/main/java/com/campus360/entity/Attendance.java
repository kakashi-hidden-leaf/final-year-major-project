package com.campus360.entity;

import com.campus360.enums.AttendanceStatus;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"student_id", "subject_id", "attendance_date"})
})
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attendanceId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttendanceStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_teacher_id")
    private Teacher recordedByTeacher;

    @Column(nullable = false)
    private LocalDateTime recordedAt = LocalDateTime.now();

    public Attendance() {}

    public Attendance(Long attendanceId, Student student, Subject subject, LocalDate attendanceDate,
                      AttendanceStatus status, Teacher recordedByTeacher, LocalDateTime recordedAt) {
        this.attendanceId = attendanceId;
        this.student = student;
        this.subject = subject;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.recordedByTeacher = recordedByTeacher;
        this.recordedAt = recordedAt != null ? recordedAt : LocalDateTime.now();
    }

    public static AttendanceBuilder builder() {
        return new AttendanceBuilder();
    }

    public static class AttendanceBuilder {
        private Long attendanceId;
        private Student student;
        private Subject subject;
        private LocalDate attendanceDate;
        private AttendanceStatus status;
        private Teacher recordedByTeacher;
        private LocalDateTime recordedAt = LocalDateTime.now();

        public AttendanceBuilder attendanceId(Long attendanceId) { this.attendanceId = attendanceId; return this; }
        public AttendanceBuilder student(Student student) { this.student = student; return this; }
        public AttendanceBuilder subject(Subject subject) { this.subject = subject; return this; }
        public AttendanceBuilder attendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; return this; }
        public AttendanceBuilder status(AttendanceStatus status) { this.status = status; return this; }
        public AttendanceBuilder recordedByTeacher(Teacher recordedByTeacher) { this.recordedByTeacher = recordedByTeacher; return this; }
        public AttendanceBuilder recordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; return this; }

        public Attendance build() {
            return new Attendance(attendanceId, student, subject, attendanceDate, status, recordedByTeacher, recordedAt);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (recordedAt == null) {
            recordedAt = LocalDateTime.now();
        }
    }

    public Long getAttendanceId() { return attendanceId; }
    public void setAttendanceId(Long attendanceId) { this.attendanceId = attendanceId; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public AttendanceStatus getStatus() { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }
    public Teacher getRecordedByTeacher() { return recordedByTeacher; }
    public void setRecordedByTeacher(Teacher recordedByTeacher) { this.recordedByTeacher = recordedByTeacher; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }
}
