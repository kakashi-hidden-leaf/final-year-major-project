package com.campus360.repository;

import com.campus360.entity.OfficeStaff;
import com.campus360.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OfficeStaffRepository extends JpaRepository<OfficeStaff, Long> {
    Optional<OfficeStaff> findByUser(User user);
    Optional<OfficeStaff> findByEmployeeId(String employeeId);
    Optional<OfficeStaff> findByUser_Username(String username);
    boolean existsByEmployeeId(String employeeId);
}
