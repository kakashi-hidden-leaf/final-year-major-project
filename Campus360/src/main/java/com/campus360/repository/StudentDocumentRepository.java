package com.campus360.repository;

import com.campus360.entity.Student;
import com.campus360.entity.StudentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentDocumentRepository extends JpaRepository<StudentDocument, Long> {
    List<StudentDocument> findByStudentOrderByUploadedAtDesc(Student student);
    List<StudentDocument> findByStudent_StudentIdOrderByUploadedAtDesc(Long studentId);
    void deleteByStudent(Student student);
}
