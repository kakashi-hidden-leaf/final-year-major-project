package com.campus360.entity;

import com.campus360.enums.IssueStatus;
import com.campus360.enums.IssueTargetRole;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "issue_tickets")
public class IssueTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IssueTargetRole targetRole;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_teacher_id")
    private Teacher assignedTeacher; // only when targetRole == TEACHER

    @Column(nullable = false, length = 100)
    private String issueCategory; // Attendance Discrepancy, Medical Leave, Document Correction, Other

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String proofFilePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IssueStatus status = IssueStatus.OPEN;

    @Column(columnDefinition = "TEXT")
    private String resolutionRemarks;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime resolvedAt;

    public IssueTicket() {}

    public IssueTicket(Long ticketId, Student student, IssueTargetRole targetRole, Teacher assignedTeacher,
                       String issueCategory, String description, String proofFilePath,
                       IssueStatus status, String resolutionRemarks, LocalDateTime createdAt, LocalDateTime resolvedAt) {
        this.ticketId = ticketId;
        this.student = student;
        this.targetRole = targetRole;
        this.assignedTeacher = assignedTeacher;
        this.issueCategory = issueCategory;
        this.description = description;
        this.proofFilePath = proofFilePath;
        this.status = status != null ? status : IssueStatus.OPEN;
        this.resolutionRemarks = resolutionRemarks;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.resolvedAt = resolvedAt;
    }

    public static IssueTicketBuilder builder() {
        return new IssueTicketBuilder();
    }

    public static class IssueTicketBuilder {
        private Long ticketId;
        private Student student;
        private IssueTargetRole targetRole;
        private Teacher assignedTeacher;
        private String issueCategory;
        private String description;
        private String proofFilePath;
        private IssueStatus status = IssueStatus.OPEN;
        private String resolutionRemarks;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime resolvedAt;

        public IssueTicketBuilder ticketId(Long ticketId) { this.ticketId = ticketId; return this; }
        public IssueTicketBuilder student(Student student) { this.student = student; return this; }
        public IssueTicketBuilder targetRole(IssueTargetRole targetRole) { this.targetRole = targetRole; return this; }
        public IssueTicketBuilder assignedTeacher(Teacher assignedTeacher) { this.assignedTeacher = assignedTeacher; return this; }
        public IssueTicketBuilder issueCategory(String issueCategory) { this.issueCategory = issueCategory; return this; }
        public IssueTicketBuilder description(String description) { this.description = description; return this; }
        public IssueTicketBuilder proofFilePath(String proofFilePath) { this.proofFilePath = proofFilePath; return this; }
        public IssueTicketBuilder status(IssueStatus status) { this.status = status; return this; }
        public IssueTicketBuilder resolutionRemarks(String resolutionRemarks) { this.resolutionRemarks = resolutionRemarks; return this; }
        public IssueTicketBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public IssueTicketBuilder resolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; return this; }

        public IssueTicket build() {
            return new IssueTicket(ticketId, student, targetRole, assignedTeacher, issueCategory, description, proofFilePath, status, resolutionRemarks, createdAt, resolvedAt);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = IssueStatus.OPEN;
        }
    }

    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public IssueTargetRole getTargetRole() { return targetRole; }
    public void setTargetRole(IssueTargetRole targetRole) { this.targetRole = targetRole; }
    public Teacher getAssignedTeacher() { return assignedTeacher; }
    public void setAssignedTeacher(Teacher assignedTeacher) { this.assignedTeacher = assignedTeacher; }
    public String getIssueCategory() { return issueCategory; }
    public void setIssueCategory(String issueCategory) { this.issueCategory = issueCategory; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getProofFilePath() { return proofFilePath; }
    public void setProofFilePath(String proofFilePath) { this.proofFilePath = proofFilePath; }
    public IssueStatus getStatus() { return status; }
    public void setStatus(IssueStatus status) { this.status = status; }
    public String getResolutionRemarks() { return resolutionRemarks; }
    public void setResolutionRemarks(String resolutionRemarks) { this.resolutionRemarks = resolutionRemarks; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
}
