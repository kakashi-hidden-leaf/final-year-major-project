package com.campus360.repository;

import com.campus360.entity.AcademicResult;
import com.campus360.entity.Student;
import com.campus360.entity.Subject;
import com.campus360.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AcademicResultRepository extends JpaRepository<AcademicResult, Long> {
    List<AcademicResult> findByStudentOrderByUploadedAtDesc(Student student);
    List<AcademicResult> findByStudent_StudentIdOrderByUploadedAtDesc(Long studentId);
    List<AcademicResult> findBySubjectOrderByUploadedAtDesc(Subject subject);
    List<AcademicResult> findBySubject_SubjectIdOrderByUploadedAtDesc(Long subjectId);
    List<AcademicResult> findByUploadedByTeacher_TeacherIdOrderByUploadedAtDesc(Long teacherId);
    Optional<AcademicResult> findByStudentAndSubjectAndExamName(Student student, Subject subject, String examName);
    Optional<AcademicResult> findByStudent_StudentIdAndSubject_SubjectIdAndExamName(Long studentId, Long subjectId, String examName);
    void deleteByStudent(Student student);
    void deleteByUploadedByTeacher(Teacher teacher);
}
