package com.campus360.repository;

import com.campus360.entity.Attendance;
import com.campus360.entity.Student;
import com.campus360.entity.Subject;
import com.campus360.entity.Teacher;
import com.campus360.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByStudentAndSubjectAndAttendanceDate(Student student, Subject subject, LocalDate attendanceDate);

    List<Attendance> findBySubjectAndAttendanceDate(Subject subject, LocalDate attendanceDate);

    List<Attendance> findBySubject_SubjectIdAndAttendanceDate(Long subjectId, LocalDate attendanceDate);

    List<Attendance> findByStudentOrderByAttendanceDateDesc(Student student);

    List<Attendance> findByStudent_StudentIdOrderByAttendanceDateDesc(Long studentId);

    List<Attendance> findByStudent_StudentIdAndSubject_SubjectIdOrderByAttendanceDateDesc(Long studentId, Long subjectId);

    long countByStudentAndSubject(Student student, Subject subject);

    long countByStudentAndSubjectAndStatus(Student student, Subject subject, AttendanceStatus status);

    long countByStudent(Student student);

    long countByStudentAndStatus(Student student, AttendanceStatus status);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.studentId = :studentId AND a.status IN ('PRESENT', 'LATE')")
    long countAttendedByStudentId(@Param("studentId") Long studentId);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.student.studentId = :studentId AND a.subject.subjectId = :subjectId AND a.status IN ('PRESENT', 'LATE')")
    long countAttendedByStudentIdAndSubjectId(@Param("studentId") Long studentId, @Param("subjectId") Long subjectId);

    void deleteByStudent(Student student);

    void deleteByRecordedByTeacher(Teacher recordedByTeacher);
}
