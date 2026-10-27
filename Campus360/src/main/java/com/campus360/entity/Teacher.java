package com.campus360.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "teachers")
public class Teacher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teacherId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 50)
    private String employeeId;

    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String designation;

    public Teacher() {}

    public Teacher(Long teacherId, User user, String employeeId, String fullName, String department, String designation) {
        this.teacherId = teacherId;
        this.user = user;
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.department = department;
        this.designation = designation;
    }

    public static TeacherBuilder builder() {
        return new TeacherBuilder();
    }

    public static class TeacherBuilder {
        private Long teacherId;
        private User user;
        private String employeeId;
        private String fullName;
        private String department;
        private String designation;

        public TeacherBuilder teacherId(Long teacherId) { this.teacherId = teacherId; return this; }
        public TeacherBuilder user(User user) { this.user = user; return this; }
        public TeacherBuilder employeeId(String employeeId) { this.employeeId = employeeId; return this; }
        public TeacherBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public TeacherBuilder department(String department) { this.department = department; return this; }
        public TeacherBuilder designation(String designation) { this.designation = designation; return this; }

        public Teacher build() {
            return new Teacher(teacherId, user, employeeId, fullName, department, designation);
        }
    }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
}
