package com.campus360.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "class_groups")
public class ClassGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long classId;

    @Column(nullable = false, length = 100)
    private String courseName; // e.g. "B.E."

    @Column(nullable = false, length = 100)
    private String branch; // e.g. "IT"

    @Column(nullable = false, length = 50)
    private String academicYear; // e.g. "2nd Year"

    @Column(nullable = false, length = 50)
    private String division; // e.g. "Div A"

    public ClassGroup() {}

    public ClassGroup(Long classId, String courseName, String branch, String academicYear, String division) {
        this.classId = classId;
        this.courseName = courseName;
        this.branch = branch;
        this.academicYear = academicYear;
        this.division = division;
    }

    public static ClassGroupBuilder builder() {
        return new ClassGroupBuilder();
    }

    public static class ClassGroupBuilder {
        private Long classId;
        private String courseName;
        private String branch;
        private String academicYear;
        private String division;

        public ClassGroupBuilder classId(Long classId) { this.classId = classId; return this; }
        public ClassGroupBuilder courseName(String courseName) { this.courseName = courseName; return this; }
        public ClassGroupBuilder branch(String branch) { this.branch = branch; return this; }
        public ClassGroupBuilder academicYear(String academicYear) { this.academicYear = academicYear; return this; }
        public ClassGroupBuilder division(String division) { this.division = division; return this; }

        public ClassGroup build() {
            return new ClassGroup(classId, courseName, branch, academicYear, division);
        }
    }

    public String getDisplayName() {
        return academicYear + " " + courseName + " " + branch + " - " + division;
    }

    public Long getClassId() { return classId; }
    public void setClassId(Long classId) { this.classId = classId; }
    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public String getDivision() { return division; }
    public void setDivision(String division) { this.division = division; }
}
