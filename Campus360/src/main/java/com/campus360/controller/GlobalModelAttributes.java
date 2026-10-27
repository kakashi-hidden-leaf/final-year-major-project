package com.campus360.controller;

import com.campus360.entity.OfficeStaff;
import com.campus360.entity.Student;
import com.campus360.entity.Teacher;
import com.campus360.entity.User;
import com.campus360.repository.OfficeStaffRepository;
import com.campus360.repository.StudentRepository;
import com.campus360.repository.TeacherRepository;
import com.campus360.repository.UserRepository;
import com.campus360.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final OfficeStaffRepository officeStaffRepository;
    private final NotificationService notificationService;

    public GlobalModelAttributes(UserRepository userRepository,
                                 StudentRepository studentRepository,
                                 TeacherRepository teacherRepository,
                                 OfficeStaffRepository officeStaffRepository,
                                 NotificationService notificationService) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.officeStaffRepository = officeStaffRepository;
        this.notificationService = notificationService;
    }

    @ModelAttribute("currentUsername")
    public String currentUsername(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            return authentication.getName();
        }
        return null;
    }

    @ModelAttribute("currentFullName")
    public String currentFullName(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }

        String username = authentication.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            switch (user.getRole()) {
                case STUDENT:
                    return studentRepository.findByUser(user).map(Student::getFullName).orElse(username);
                case TEACHER:
                    return teacherRepository.findByUser(user).map(Teacher::getFullName).orElse(username);
                case OFFICE_STAFF:
                    return officeStaffRepository.findByUser(user).map(OfficeStaff::getFullName).orElse(username);
            }
        }
        return username;
    }

    @ModelAttribute("currentUserRole")
    public String currentUserRole(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName())
                .map(u -> u.getRole().name())
                .orElse(null);
    }

    @ModelAttribute("unreadNotificationCount")
    public long unreadNotificationCount(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return 0;
        }
        return userRepository.findByUsername(authentication.getName())
                .map(u -> notificationService.getUnreadCount(u.getUserId()))
                .orElse(0L);
    }
}
