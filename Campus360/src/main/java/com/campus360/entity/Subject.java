package com.campus360.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "subjects")
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long subjectId;

    @Column(nullable = false, length = 50)
    private String subjectCode;

    @Column(nullable = false, length = 150)
    private String subjectName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassGroup classGroup;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "teacher_id")
    private Teacher assignedTeacher;

    public Subject() {}

    public Subject(Long subjectId, String subjectCode, String subjectName, ClassGroup classGroup, Teacher assignedTeacher) {
        this.subjectId = subjectId;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.classGroup = classGroup;
        this.assignedTeacher = assignedTeacher;
    }

    public static SubjectBuilder builder() {
        return new SubjectBuilder();
    }

    public static class SubjectBuilder {
        private Long subjectId;
        private String subjectCode;
        private String subjectName;
        private ClassGroup classGroup;
        private Teacher assignedTeacher;

        public SubjectBuilder subjectId(Long subjectId) { this.subjectId = subjectId; return this; }
        public SubjectBuilder subjectCode(String subjectCode) { this.subjectCode = subjectCode; return this; }
        public SubjectBuilder subjectName(String subjectName) { this.subjectName = subjectName; return this; }
        public SubjectBuilder classGroup(ClassGroup classGroup) { this.classGroup = classGroup; return this; }
        public SubjectBuilder assignedTeacher(Teacher assignedTeacher) { this.assignedTeacher = assignedTeacher; return this; }

        public Subject build() {
            return new Subject(subjectId, subjectCode, subjectName, classGroup, assignedTeacher);
        }
    }

    public String getDisplayName() {
        return subjectName + " (" + subjectCode + ")";
    }

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public ClassGroup getClassGroup() { return classGroup; }
    public void setClassGroup(ClassGroup classGroup) { this.classGroup = classGroup; }
    public Teacher getAssignedTeacher() { return assignedTeacher; }
    public void setAssignedTeacher(Teacher assignedTeacher) { this.assignedTeacher = assignedTeacher; }
}
