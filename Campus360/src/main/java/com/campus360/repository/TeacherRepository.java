package com.campus360.repository;

import com.campus360.entity.Teacher;
import com.campus360.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByUser(User user);
    Optional<Teacher> findByEmployeeId(String employeeId);
    Optional<Teacher> findByUser_Username(String username);
    boolean existsByEmployeeId(String employeeId);
}
