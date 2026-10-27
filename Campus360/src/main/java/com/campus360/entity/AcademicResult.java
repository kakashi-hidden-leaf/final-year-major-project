package com.campus360.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "academic_results")
public class AcademicResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resultId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false, length = 100)
    private String examName;

    @Column(nullable = false)
    private Double marksObtained;

    @Column(nullable = false)
    private Double maxMarks;

    @Column(length = 10)
    private String grade;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_teacher_id")
    private Teacher uploadedByTeacher;

    @Column(nullable = false)
    private LocalDateTime uploadedAt = LocalDateTime.now();

    public AcademicResult() {}

    public AcademicResult(Long resultId, Student student, Subject subject, String examName,
                          Double marksObtained, Double maxMarks, String grade,
                          Teacher uploadedByTeacher, LocalDateTime uploadedAt) {
        this.resultId = resultId;
        this.student = student;
        this.subject = subject;
        this.examName = examName;
        this.marksObtained = marksObtained;
        this.maxMarks = maxMarks;
        this.grade = grade;
        this.uploadedByTeacher = uploadedByTeacher;
        this.uploadedAt = uploadedAt != null ? uploadedAt : LocalDateTime.now();
    }

    public static AcademicResultBuilder builder() {
        return new AcademicResultBuilder();
    }

    public static class AcademicResultBuilder {
        private Long resultId;
        private Student student;
        private Subject subject;
        private String examName;
        private Double marksObtained;
        private Double maxMarks;
        private String grade;
        private Teacher uploadedByTeacher;
        private LocalDateTime uploadedAt = LocalDateTime.now();

        public AcademicResultBuilder resultId(Long resultId) { this.resultId = resultId; return this; }
        public AcademicResultBuilder student(Student student) { this.student = student; return this; }
        public AcademicResultBuilder subject(Subject subject) { this.subject = subject; return this; }
        public AcademicResultBuilder examName(String examName) { this.examName = examName; return this; }
        public AcademicResultBuilder marksObtained(Double marksObtained) { this.marksObtained = marksObtained; return this; }
        public AcademicResultBuilder maxMarks(Double maxMarks) { this.maxMarks = maxMarks; return this; }
        public AcademicResultBuilder grade(String grade) { this.grade = grade; return this; }
        public AcademicResultBuilder uploadedByTeacher(Teacher uploadedByTeacher) { this.uploadedByTeacher = uploadedByTeacher; return this; }
        public AcademicResultBuilder uploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; return this; }

        public AcademicResult build() {
            return new AcademicResult(resultId, student, subject, examName, marksObtained, maxMarks, grade, uploadedByTeacher, uploadedAt);
        }
    }

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }

    public double getPercentage() {
        if (maxMarks != null && maxMarks > 0 && marksObtained != null) {
            return (marksObtained / maxMarks) * 100.0;
        }
        return 0.0;
    }

    public Long getResultId() { return resultId; }
    public void setResultId(Long resultId) { this.resultId = resultId; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }
    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }
    public Double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Double marksObtained) { this.marksObtained = marksObtained; }
    public Double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Double maxMarks) { this.maxMarks = maxMarks; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public Teacher getUploadedByTeacher() { return uploadedByTeacher; }
    public void setUploadedByTeacher(Teacher uploadedByTeacher) { this.uploadedByTeacher = uploadedByTeacher; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
