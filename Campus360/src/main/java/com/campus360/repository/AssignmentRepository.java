package com.campus360.repository;

import com.campus360.entity.Assignment;
import com.campus360.entity.Subject;
import com.campus360.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    List<Assignment> findByTeacher_TeacherIdOrderByCreatedAtDesc(Long teacherId);

    List<Assignment> findByTeacher_TeacherIdAndSubject_SubjectIdOrderByCreatedAtDesc(Long teacherId, Long subjectId);

    List<Assignment> findBySubject_ClassGroup_ClassIdOrderByDueDateAsc(Long classId);

    List<Assignment> findBySubject_SubjectIdOrderByDueDateAsc(Long subjectId);

    void deleteByTeacher(Teacher teacher);

    void deleteBySubject(Subject subject);
}
