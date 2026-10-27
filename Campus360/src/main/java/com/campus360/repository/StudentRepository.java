package com.campus360.repository;

import com.campus360.entity.ClassGroup;
import com.campus360.entity.Student;
import com.campus360.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByUser(User user);
    Optional<Student> findByRollNumber(String rollNumber);
    Optional<Student> findByUser_Username(String username);
    List<Student> findByClassGroup(ClassGroup classGroup);
    List<Student> findByClassGroup_ClassIdOrderByRollNumberAsc(Long classId);
    long countByClassGroup_ClassId(Long classId);
    long countByClassGroup(ClassGroup classGroup);
    boolean existsByRollNumber(String rollNumber);
}
