package com.campus360.service;

import com.campus360.entity.*;
import com.campus360.enums.FacilityType;
import com.campus360.enums.IssueStatus;
import com.campus360.enums.IssueTargetRole;
import com.campus360.enums.NotificationType;
import com.campus360.enums.Role;
import com.campus360.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OfficeStaffService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final OfficeStaffRepository staffRepository;
    private final ClassGroupRepository classGroupRepository;
    private final SubjectRepository subjectRepository;
    private final CampusLocationRepository campusLocationRepository;
    private final StudentDocumentRepository studentDocumentRepository;
    private final IssueTicketRepository issueTicketRepository;
    private final AttendanceRepository attendanceRepository;
    private final AcademicResultRepository academicResultRepository;
    private final AssignmentSubmissionRepository assignmentRepository;
    private final AssignmentRepository assignmentRepo;
    private final TeacherScheduleRepository teacherScheduleRepository;
    private final AnnouncementRepository announcementRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public OfficeStaffService(UserRepository userRepository,
                              StudentRepository studentRepository,
                              TeacherRepository teacherRepository,
                              OfficeStaffRepository staffRepository,
                              ClassGroupRepository classGroupRepository,
                              SubjectRepository subjectRepository,
                              CampusLocationRepository campusLocationRepository,
                              StudentDocumentRepository studentDocumentRepository,
                              IssueTicketRepository issueTicketRepository,
                              AttendanceRepository attendanceRepository,
                              AcademicResultRepository academicResultRepository,
                              AssignmentSubmissionRepository assignmentRepository,
                              AssignmentRepository assignmentRepo,
                              TeacherScheduleRepository teacherScheduleRepository,
                              AnnouncementRepository announcementRepository,
                              NotificationRepository notificationRepository,
                              PasswordEncoder passwordEncoder,
                              FileStorageService fileStorageService,
                              NotificationService notificationService) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.staffRepository = staffRepository;
        this.classGroupRepository = classGroupRepository;
        this.subjectRepository = subjectRepository;
        this.campusLocationRepository = campusLocationRepository;
        this.studentDocumentRepository = studentDocumentRepository;
        this.issueTicketRepository = issueTicketRepository;
        this.attendanceRepository = attendanceRepository;
        this.academicResultRepository = academicResultRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentRepo = assignmentRepo;
        this.teacherScheduleRepository = teacherScheduleRepository;
        this.announcementRepository = announcementRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalStudents", studentRepository.count());
        stats.put("totalTeachers", teacherRepository.count());
        stats.put("totalClasses", classGroupRepository.count());
        stats.put("totalLocations", campusLocationRepository.count());
        stats.put("openIssues", issueTicketRepository.countByTargetRoleAndStatus(IssueTargetRole.OFFICE_STAFF, IssueStatus.OPEN));
        return stats;
    }

    @Transactional
    public Student enrollStudent(String rollNumber, String fullName, String email, String password,
                                 Long classId, String admissionYear, String phone) {
        if (userRepository.existsByUsername(rollNumber)) {
            throw new IllegalArgumentException("Username/Roll Number already exists: " + rollNumber);
        }
        if (studentRepository.existsByRollNumber(rollNumber)) {
            throw new IllegalArgumentException("Student with Roll Number already exists: " + rollNumber);
        }

        ClassGroup classGroup = classGroupRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Class Group not found with ID: " + classId));

        User user = User.builder()
                .username(rollNumber)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.STUDENT)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(user);

        Student student = Student.builder()
                .user(savedUser)
                .rollNumber(rollNumber)
                .fullName(fullName)
                .classGroup(classGroup)
                .admissionYear(admissionYear)
                .phone(phone)
                .build();

        return studentRepository.save(student);
    }

    @Transactional
    public Teacher enrollTeacher(String employeeId, String fullName, String email, String password,
                                 String department, String designation) {
        if (userRepository.existsByUsername(employeeId)) {
            throw new IllegalArgumentException("Username/Employee ID already exists: " + employeeId);
        }
        if (teacherRepository.existsByEmployeeId(employeeId)) {
            throw new IllegalArgumentException("Teacher with Employee ID already exists: " + employeeId);
        }

        User user = User.builder()
                .username(employeeId)
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(Role.TEACHER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        User savedUser = userRepository.save(user);

        Teacher teacher = Teacher.builder()
                .user(savedUser)
                .employeeId(employeeId)
                .fullName(fullName)
                .department(department)
                .designation(designation)
                .build();

        return teacherRepository.save(teacher);
    }

    @Transactional
    public ClassGroup createClassGroup(String courseName, String branch, String academicYear, String division) {
        ClassGroup cg = ClassGroup.builder()
                .courseName(courseName)
                .branch(branch)
                .academicYear(academicYear)
                .division(division)
                .build();
        return classGroupRepository.save(cg);
    }

    @Transactional
    public Subject createSubject(String subjectCode, String subjectName, Long classId, Long teacherId) {
        ClassGroup classGroup = classGroupRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found: " + classId));

        Teacher teacher = null;
        if (teacherId != null) {
            teacher = teacherRepository.findById(teacherId).orElse(null);
        }

        Subject subject = Subject.builder()
                .subjectCode(subjectCode)
                .subjectName(subjectName)
                .classGroup(classGroup)
                .assignedTeacher(teacher)
                .build();
        return subjectRepository.save(subject);
    }

    @Transactional
    public CampusLocation createCampusLocation(String buildingName, String floorNumber, String roomNumber,
                                               FacilityType facilityType, String description) {
        CampusLocation location = CampusLocation.builder()
                .buildingName(buildingName)
                .floorNumber(floorNumber)
                .roomNumber(roomNumber)
                .facilityType(facilityType)
                .description(description)
                .build();
        return campusLocationRepository.save(location);
    }

    @Transactional
    public StudentDocument uploadStudentDocument(Long studentId, String documentTitle, String documentType,
                                                 MultipartFile file, String staffUsername) throws IOException {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        OfficeStaff staff = staffRepository.findByUser_Username(staffUsername).orElse(null);

        String savedFileName = fileStorageService.storeVaultFile(file);

        StudentDocument doc = StudentDocument.builder()
                .student(student)
                .documentTitle(documentTitle)
                .documentType(documentType)
                .filePath(savedFileName)
                .uploadedByStaff(staff)
                .uploadedAt(LocalDateTime.now())
                .build();

        StudentDocument savedDoc = studentDocumentRepository.save(doc);

        notificationService.sendNotification(
                student.getUser(),
                "New Document Added to Vault",
                "Official document '" + documentTitle + "' has been uploaded to your Document Vault.",
                NotificationType.GENERAL
        );

        return savedDoc;
    }

    @Transactional
    public void resolveAdministrativeTicket(Long ticketId, IssueStatus status, String resolutionRemarks) {
        IssueTicket ticket = issueTicketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        ticket.setStatus(status);
        ticket.setResolutionRemarks(resolutionRemarks);
        if (status == IssueStatus.RESOLVED || status == IssueStatus.REJECTED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }
        issueTicketRepository.save(ticket);

        notificationService.sendNotification(
                ticket.getStudent().getUser(),
                "Grievance Ticket Update: " + ticket.getIssueCategory(),
                "Your issue ticket #" + ticket.getTicketId() + " status is now " + status + ". Remarks: " + resolutionRemarks,
                NotificationType.ISSUE_UPDATE
        );
    }

    @Transactional
    public Student updateStudent(Long studentId, String fullName, String email, Long classId,
                                 String admissionYear, String phone, String newPassword, Boolean isActive) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));

        ClassGroup classGroup = classGroupRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Class Group not found with ID: " + classId));

        student.setFullName(fullName);
        student.setClassGroup(classGroup);
        student.setAdmissionYear(admissionYear);
        student.setPhone(phone);

        User user = student.getUser();
        if (user != null) {
            user.setEmail(email);
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                user.setPasswordHash(passwordEncoder.encode(newPassword.trim()));
            }
            if (isActive != null) {
                user.setActive(isActive);
            }
            userRepository.save(user);
        }

        return studentRepository.save(student);
    }

    @Transactional
    public void deleteStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        User user = student.getUser();

        // 1. Delete all student-dependent records across tables
        attendanceRepository.deleteByStudent(student);
        academicResultRepository.deleteByStudent(student);
        assignmentRepository.deleteByStudent(student);
        studentDocumentRepository.deleteByStudent(student);
        issueTicketRepository.deleteByStudent(student);

        // 2. Delete user notifications
        if (user != null) {
            notificationRepository.deleteByRecipientUser(user);
        }

        // 3. Delete student entity
        studentRepository.delete(student);

        // 4. Delete user account
        if (user != null) {
            userRepository.delete(user);
        }
    }

    @Transactional
    public Teacher updateTeacher(Long teacherId, String fullName, String email, String department,
                                 String designation, String newPassword, Boolean isActive) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found with ID: " + teacherId));

        teacher.setFullName(fullName);
        teacher.setDepartment(department);
        teacher.setDesignation(designation);

        User user = teacher.getUser();
        if (user != null) {
            user.setEmail(email);
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                user.setPasswordHash(passwordEncoder.encode(newPassword.trim()));
            }
            if (isActive != null) {
                user.setActive(isActive);
            }
            userRepository.save(user);
        }

        return teacherRepository.save(teacher);
    }

    @Transactional
    public void deleteTeacher(Long teacherId) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));
        User user = teacher.getUser();

        // 1. Unassign subjects
        List<Subject> subjects = subjectRepository.findByAssignedTeacher(teacher);
        for (Subject s : subjects) {
            s.setAssignedTeacher(null);
            subjectRepository.save(s);
        }

        // 2. Delete timetable slots
        teacherScheduleRepository.deleteByTeacher(teacher);

        // 3. Clear or delete teacher dependencies
        issueTicketRepository.deleteByAssignedTeacher(teacher);
        academicResultRepository.deleteByUploadedByTeacher(teacher);
        attendanceRepository.deleteByRecordedByTeacher(teacher);
        announcementRepository.deleteByCreatedByTeacher(teacher);

        List<Assignment> teacherAssignments = assignmentRepo.findByTeacher_TeacherIdOrderByCreatedAtDesc(teacherId);
        for (Assignment a : teacherAssignments) {
            assignmentRepository.deleteByAssignment(a);
            assignmentRepo.delete(a);
        }

        if (user != null) {
            notificationRepository.deleteByRecipientUser(user);
        }

        teacherRepository.delete(teacher);
        if (user != null) {
            userRepository.delete(user);
        }
    }

    @Transactional
    public void deleteClassGroup(Long classId) {
        classGroupRepository.deleteById(classId);
    }

    @Transactional
    public void deleteSubject(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        if (subject != null) {
            List<Assignment> subjectAssignments = assignmentRepo.findBySubject_SubjectIdOrderByDueDateAsc(subjectId);
            for (Assignment a : subjectAssignments) {
                assignmentRepository.deleteByAssignment(a);
                assignmentRepo.delete(a);
            }
            subjectRepository.delete(subject);
        }
    }

    @Transactional
    public void deleteCampusLocation(Long locationId) {
        campusLocationRepository.deleteById(locationId);
    }

    @Transactional
    public void deleteStudentDocument(Long documentId) {
        studentDocumentRepository.deleteById(documentId);
    }
}
