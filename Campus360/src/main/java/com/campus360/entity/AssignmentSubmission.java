package com.campus360.entity;

import com.campus360.enums.AssignmentStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "assignment_submissions")
public class AssignmentSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long submissionId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignment_id", nullable = true)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false, length = 150)
    private String assignmentTitle;

    @Column(nullable = false, length = 255)
    private String filePath;

    @Column(length = 500)
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AssignmentStatus status = AssignmentStatus.SUBMITTED;

    @Column(nullable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    public AssignmentSubmission() {}

    public AssignmentSubmission(Long submissionId, Assignment assignment, Student student, Subject subject,
                                String assignmentTitle, String filePath, String remarks,
                                AssignmentStatus status, LocalDateTime submittedAt) {
        this.submissionId = submissionId;
        this.assignment = assignment;
        this.student = student;
        this.subject = subject;
        this.assignmentTitle = assignmentTitle;
        this.filePath = filePath;
        this.remarks = remarks;
        this.status = status != null ? status : AssignmentStatus.SUBMITTED;
        this.submittedAt = submittedAt != null ? submittedAt : LocalDateTime.now();
    }

    public static AssignmentSubmissionBuilder builder() {
        return new AssignmentSubmissionBuilder();
    }

    public static class AssignmentSubmissionBuilder {
        private Long submissionId;
        private Assignment assignment;
        private Student student;
        private Subject subject;
        private String assignmentTitle;
        private String filePath;
        private String remarks;
        private AssignmentStatus status = AssignmentStatus.SUBMITTED;
        private LocalDateTime submittedAt = LocalDateTime.now();

        public AssignmentSubmissionBuilder submissionId(Long submissionId) { this.submissionId = submissionId; return this; }
        public AssignmentSubmissionBuilder assignment(Assignment assignment) { this.assignment = assignment; return this; }
        public AssignmentSubmissionBuilder student(Student student) { this.student = student; return this; }
        public AssignmentSubmissionBuilder subject(Subject subject) { this.subject = subject; return this; }
        public AssignmentSubmissionBuilder assignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; return this; }
        public AssignmentSubmissionBuilder filePath(String filePath) { this.filePath = filePath; return this; }
        public AssignmentSubmissionBuilder remarks(String remarks) { this.remarks = remarks; return this; }
        public AssignmentSubmissionBuilder status(AssignmentStatus status) { this.status = status; return this; }
        public AssignmentSubmissionBuilder submittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; return this; }

        public AssignmentSubmission build() {
            return new AssignmentSubmission(submissionId, assignment, student, subject, assignmentTitle, filePath, remarks, status, submittedAt);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (submittedAt == null) {
            submittedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = AssignmentStatus.SUBMITTED;
        }
    }

    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }
    public Assignment getAssignment() { return assignment; }
    public void setAssignment(Assignment assignment) { this.assignment = assignment; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public String getAssignmentTitle() { return assignmentTitle; }
    public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public AssignmentStatus getStatus() { return status; }
    public void setStatus(AssignmentStatus status) { this.status = status; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }
}
