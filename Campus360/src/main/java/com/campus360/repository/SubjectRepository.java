package com.campus360.repository;

import com.campus360.entity.ClassGroup;
import com.campus360.entity.Subject;
import com.campus360.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByClassGroup(ClassGroup classGroup);
    List<Subject> findByClassGroup_ClassId(Long classId);
    List<Subject> findByAssignedTeacher(Teacher teacher);
    List<Subject> findByAssignedTeacher_TeacherId(Long teacherId);
    List<Subject> findByAssignedTeacher_User_Username(String username);
    List<Subject> findByAssignedTeacherIsNull();
    List<Subject> findByClassGroup_ClassIdAndAssignedTeacherIsNull(Long classId);
    boolean existsBySubjectCodeAndClassGroup_ClassId(String subjectCode, Long classId);
}
