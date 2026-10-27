package com.campus360.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long assignmentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(length = 255)
    private String questionFilePath;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Assignment() {}

    public Assignment(Long assignmentId, Subject subject, Teacher teacher, String title,
                      String description, LocalDate dueDate, String questionFilePath, LocalDateTime createdAt) {
        this.assignmentId = assignmentId;
        this.subject = subject;
        this.teacher = teacher;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.questionFilePath = questionFilePath;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
    }

    public static AssignmentBuilder builder() {
        return new AssignmentBuilder();
    }

    public static class AssignmentBuilder {
        private Long assignmentId;
        private Subject subject;
        private Teacher teacher;
        private String title;
        private String description;
        private LocalDate dueDate;
        private String questionFilePath;
        private LocalDateTime createdAt = LocalDateTime.now();

        public AssignmentBuilder assignmentId(Long assignmentId) { this.assignmentId = assignmentId; return this; }
        public AssignmentBuilder subject(Subject subject) { this.subject = subject; return this; }
        public AssignmentBuilder teacher(Teacher teacher) { this.teacher = teacher; return this; }
        public AssignmentBuilder title(String title) { this.title = title; return this; }
        public AssignmentBuilder description(String description) { this.description = description; return this; }
        public AssignmentBuilder dueDate(LocalDate dueDate) { this.dueDate = dueDate; return this; }
        public AssignmentBuilder questionFilePath(String questionFilePath) { this.questionFilePath = questionFilePath; return this; }
        public AssignmentBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public Assignment build() {
            return new Assignment(assignmentId, subject, teacher, title, description, dueDate, questionFilePath, createdAt);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public Teacher getTeacher() { return teacher; }
    public void setTeacher(Teacher teacher) { this.teacher = teacher; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public String getQuestionFilePath() { return questionFilePath; }
    public void setQuestionFilePath(String questionFilePath) { this.questionFilePath = questionFilePath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
