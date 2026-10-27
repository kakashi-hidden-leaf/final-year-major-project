package com.campus360.repository;

import com.campus360.entity.Assignment;
import com.campus360.entity.AssignmentSubmission;
import com.campus360.entity.Student;
import com.campus360.entity.Subject;
import com.campus360.enums.AssignmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    List<AssignmentSubmission> findByStudentOrderBySubmittedAtDesc(Student student);
    List<AssignmentSubmission> findByStudent_StudentIdOrderBySubmittedAtDesc(Long studentId);
    List<AssignmentSubmission> findBySubjectOrderBySubmittedAtDesc(Subject subject);
    List<AssignmentSubmission> findBySubject_SubjectIdOrderBySubmittedAtDesc(Long subjectId);
    List<AssignmentSubmission> findBySubject_AssignedTeacher_TeacherIdOrderBySubmittedAtDesc(Long teacherId);
    List<AssignmentSubmission> findBySubject_AssignedTeacher_User_UsernameOrderBySubmittedAtDesc(String username);
    List<AssignmentSubmission> findByAssignment_AssignmentIdOrderBySubmittedAtDesc(Long assignmentId);
    Optional<AssignmentSubmission> findByAssignment_AssignmentIdAndStudent_StudentId(Long assignmentId, Long studentId);
    long countBySubject_AssignedTeacher_TeacherIdAndStatus(Long teacherId, AssignmentStatus status);
    void deleteByStudent(Student student);
    void deleteByAssignment(Assignment assignment);
    void deleteByAssignment_AssignmentId(Long assignmentId);
}
