package com.campus360.repository;

import com.campus360.entity.IssueTicket;
import com.campus360.entity.Student;
import com.campus360.enums.IssueStatus;
import com.campus360.enums.IssueTargetRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IssueTicketRepository extends JpaRepository<IssueTicket, Long> {
    List<IssueTicket> findByStudentOrderByCreatedAtDesc(Student student);
    List<IssueTicket> findByStudent_StudentIdOrderByCreatedAtDesc(Long studentId);
    List<IssueTicket> findByTargetRoleOrderByCreatedAtDesc(IssueTargetRole targetRole);
    List<IssueTicket> findByTargetRoleAndStatusOrderByCreatedAtDesc(IssueTargetRole targetRole, IssueStatus status);
    List<IssueTicket> findByAssignedTeacher_TeacherIdOrderByCreatedAtDesc(Long teacherId);
    List<IssueTicket> findByAssignedTeacher_User_UsernameOrderByCreatedAtDesc(String username);
    List<IssueTicket> findByTargetRoleAndAssignedTeacher_TeacherIdOrderByCreatedAtDesc(IssueTargetRole targetRole, Long teacherId);
    long countByTargetRoleAndStatus(IssueTargetRole targetRole, IssueStatus status);
    long countByAssignedTeacher_TeacherIdAndStatus(Long teacherId, IssueStatus status);
    void deleteByStudent(Student student);
    void deleteByAssignedTeacher(com.campus360.entity.Teacher teacher);
}
