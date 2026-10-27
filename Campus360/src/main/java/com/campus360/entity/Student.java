package com.campus360.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long studentId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 50)
    private String rollNumber;

    @Column(nullable = false, length = 150)
    private String fullName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "class_id", nullable = false)
    private ClassGroup classGroup;

    @Column(length = 20)
    private String phone;

    @Column(length = 10)
    private String admissionYear;

    public Student() {}

    public Student(Long studentId, User user, String rollNumber, String fullName, ClassGroup classGroup, String phone, String admissionYear) {
        this.studentId = studentId;
        this.user = user;
        this.rollNumber = rollNumber;
        this.fullName = fullName;
        this.classGroup = classGroup;
        this.phone = phone;
        this.admissionYear = admissionYear;
    }

    public static StudentBuilder builder() {
        return new StudentBuilder();
    }

    public static class StudentBuilder {
        private Long studentId;
        private User user;
        private String rollNumber;
        private String fullName;
        private ClassGroup classGroup;
        private String phone;
        private String admissionYear;

        public StudentBuilder studentId(Long studentId) { this.studentId = studentId; return this; }
        public StudentBuilder user(User user) { this.user = user; return this; }
        public StudentBuilder rollNumber(String rollNumber) { this.rollNumber = rollNumber; return this; }
        public StudentBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public StudentBuilder classGroup(ClassGroup classGroup) { this.classGroup = classGroup; return this; }
        public StudentBuilder phone(String phone) { this.phone = phone; return this; }
        public StudentBuilder admissionYear(String admissionYear) { this.admissionYear = admissionYear; return this; }

        public Student build() {
            return new Student(studentId, user, rollNumber, fullName, classGroup, phone, admissionYear);
        }
    }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public ClassGroup getClassGroup() { return classGroup; }
    public void setClassGroup(ClassGroup classGroup) { this.classGroup = classGroup; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAdmissionYear() { return admissionYear; }
    public void setAdmissionYear(String admissionYear) { this.admissionYear = admissionYear; }
}
