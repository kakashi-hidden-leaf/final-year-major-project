package com.campus360.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_documents")
public class StudentDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(nullable = false, length = 150)
    private String documentTitle;

    @Column(length = 50)
    private String documentType; // e.g., Admission Receipt, 12th Marksheet, Bonafide

    @Column(nullable = false, length = 255)
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_staff_id")
    private OfficeStaff uploadedByStaff;

    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    public StudentDocument() {}

    public StudentDocument(Long documentId, Student student, String documentTitle, String documentType,
                           String filePath, OfficeStaff uploadedByStaff, LocalDateTime uploadedAt) {
        this.documentId = documentId;
        this.student = student;
        this.documentTitle = documentTitle;
        this.documentType = documentType;
        this.filePath = filePath;
        this.uploadedByStaff = uploadedByStaff;
        this.uploadedAt = uploadedAt != null ? uploadedAt : LocalDateTime.now();
    }

    public static StudentDocumentBuilder builder() {
        return new StudentDocumentBuilder();
    }

    public static class StudentDocumentBuilder {
        private Long documentId;
        private Student student;
        private String documentTitle;
        private String documentType;
        private String filePath;
        private OfficeStaff uploadedByStaff;
        private LocalDateTime uploadedAt = LocalDateTime.now();

        public StudentDocumentBuilder documentId(Long documentId) { this.documentId = documentId; return this; }
        public StudentDocumentBuilder student(Student student) { this.student = student; return this; }
        public StudentDocumentBuilder documentTitle(String documentTitle) { this.documentTitle = documentTitle; return this; }
        public StudentDocumentBuilder documentType(String documentType) { this.documentType = documentType; return this; }
        public StudentDocumentBuilder filePath(String filePath) { this.filePath = filePath; return this; }
        public StudentDocumentBuilder uploadedByStaff(OfficeStaff uploadedByStaff) { this.uploadedByStaff = uploadedByStaff; return this; }
        public StudentDocumentBuilder uploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; return this; }

        public StudentDocument build() {
            return new StudentDocument(documentId, student, documentTitle, documentType, filePath, uploadedByStaff, uploadedAt);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }

    public Long getDocumentId() { return documentId; }
    public void setDocumentId(Long documentId) { this.documentId = documentId; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public String getDocumentTitle() { return documentTitle; }
    public void setDocumentTitle(String documentTitle) { this.documentTitle = documentTitle; }
    public String getDocumentType() { return documentType; }
    public void setDocumentType(String documentType) { this.documentType = documentType; }
    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public OfficeStaff getUploadedByStaff() { return uploadedByStaff; }
    public void setUploadedByStaff(OfficeStaff uploadedByStaff) { this.uploadedByStaff = uploadedByStaff; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
