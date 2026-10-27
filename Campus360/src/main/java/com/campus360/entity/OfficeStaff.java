package com.campus360.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "office_staff")
public class OfficeStaff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long staffId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 50)
    private String employeeId;

    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(length = 100)
    private String officeSection;

    public OfficeStaff() {}

    public OfficeStaff(Long staffId, User user, String employeeId, String fullName, String officeSection) {
        this.staffId = staffId;
        this.user = user;
        this.employeeId = employeeId;
        this.fullName = fullName;
        this.officeSection = officeSection;
    }

    public static OfficeStaffBuilder builder() {
        return new OfficeStaffBuilder();
    }

    public static class OfficeStaffBuilder {
        private Long staffId;
        private User user;
        private String employeeId;
        private String fullName;
        private String officeSection;

        public OfficeStaffBuilder staffId(Long staffId) { this.staffId = staffId; return this; }
        public OfficeStaffBuilder user(User user) { this.user = user; return this; }
        public OfficeStaffBuilder employeeId(String employeeId) { this.employeeId = employeeId; return this; }
        public OfficeStaffBuilder fullName(String fullName) { this.fullName = fullName; return this; }
        public OfficeStaffBuilder officeSection(String officeSection) { this.officeSection = officeSection; return this; }

        public OfficeStaff build() {
            return new OfficeStaff(staffId, user, employeeId, fullName, officeSection);
        }
    }

    public Long getStaffId() { return staffId; }
    public void setStaffId(Long staffId) { this.staffId = staffId; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getOfficeSection() { return officeSection; }
    public void setOfficeSection(String officeSection) { this.officeSection = officeSection; }
}
