package com.campus360.service;

import com.campus360.entity.*;
import com.campus360.enums.AssignmentStatus;
import com.campus360.enums.FacilityType;
import com.campus360.enums.IssueStatus;
import com.campus360.enums.IssueTargetRole;
import com.campus360.enums.NotificationType;
import com.campus360.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final AcademicResultRepository academicResultRepository;
    private final StudentDocumentRepository studentDocumentRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final IssueTicketRepository issueTicketRepository;
    private final AnnouncementRepository announcementRepository;
    private final CampusLocationRepository locationRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public StudentService(StudentRepository studentRepository,
                          AttendanceRepository attendanceRepository,
                          AcademicResultRepository academicResultRepository,
                          StudentDocumentRepository studentDocumentRepository,
                          AssignmentRepository assignmentRepository,
                          AssignmentSubmissionRepository assignmentSubmissionRepository,
                          IssueTicketRepository issueTicketRepository,
                          AnnouncementRepository announcementRepository,
                          CampusLocationRepository locationRepository,
                          SubjectRepository subjectRepository,
                          TeacherRepository teacherRepository,
                          FileStorageService fileStorageService,
                          NotificationService notificationService) {
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.academicResultRepository = academicResultRepository;
        this.studentDocumentRepository = studentDocumentRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentSubmissionRepository = assignmentSubmissionRepository;
        this.issueTicketRepository = issueTicketRepository;
        this.announcementRepository = announcementRepository;
        this.locationRepository = locationRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    public static class SubjectAttendanceStat {
        private Subject subject;
        private long totalLectures;
        private long attendedLectures;
        private double percentage;

        public Subject getSubject() { return subject; }
        public void setSubject(Subject subject) { this.subject = subject; }
        public long getTotalLectures() { return totalLectures; }
        public void setTotalLectures(long totalLectures) { this.totalLectures = totalLectures; }
        public long getAttendedLectures() { return attendedLectures; }
        public void setAttendedLectures(long attendedLectures) { this.attendedLectures = attendedLectures; }
        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }

    @Transactional(readOnly = true)
    public Optional<Student> getStudentByUsername(String username) {
        return studentRepository.findByUser_Username(username);
    }

    @Transactional(readOnly = true)
    public List<SubjectAttendanceStat> getSubjectAttendanceStats(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        List<Subject> subjects = subjectRepository.findByClassGroup(student.getClassGroup());
        List<SubjectAttendanceStat> statsList = new ArrayList<>();

        for (Subject sub : subjects) {
            long total = attendanceRepository.countByStudentAndSubject(student, sub);
            long attended = attendanceRepository.countAttendedByStudentIdAndSubjectId(studentId, sub.getSubjectId());
            double percentage = total > 0 ? ((double) attended / total) * 100.0 : 0.0;

            SubjectAttendanceStat stat = new SubjectAttendanceStat();
            stat.setSubject(sub);
            stat.setTotalLectures(total);
            stat.setAttendedLectures(attended);
            stat.setPercentage(Math.round(percentage * 10.0) / 10.0);
            statsList.add(stat);
        }
        return statsList;
    }

    @Transactional(readOnly = true)
    public double getOverallAttendancePercentage(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        long total = attendanceRepository.countByStudent(student);
        if (total == 0) return 0.0;

        long attended = attendanceRepository.countAttendedByStudentId(studentId);
        double pct = ((double) attended / total) * 100.0;
        return Math.round(pct * 10.0) / 10.0;
    }

    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceHistory(Long studentId) {
        return attendanceRepository.findByStudent_StudentIdOrderByAttendanceDateDesc(studentId);
    }

    @Transactional(readOnly = true)
    public List<StudentDocument> getVaultDocuments(Long studentId) {
        return studentDocumentRepository.findByStudent_StudentIdOrderByUploadedAtDesc(studentId);
    }

    @Transactional(readOnly = true)
    public List<AcademicResult> getAcademicResults(Long studentId) {
        return academicResultRepository.findByStudent_StudentIdOrderByUploadedAtDesc(studentId);
    }

    @Transactional(readOnly = true)
    public List<AssignmentSubmission> getMyAssignments(Long studentId) {
        return assignmentSubmissionRepository.findByStudent_StudentIdOrderBySubmittedAtDesc(studentId);
    }

    @Transactional(readOnly = true)
    public List<Assignment> getActiveAssignmentsForStudent(Long classId) {
        return assignmentRepository.findBySubject_ClassGroup_ClassIdOrderByDueDateAsc(classId);
    }

    @Transactional(readOnly = true)
    public List<IssueTicket> getMyTickets(Long studentId) {
        return issueTicketRepository.findByStudent_StudentIdOrderByCreatedAtDesc(studentId);
    }

    @Transactional(readOnly = true)
    public List<Announcement> getActiveAnnouncements(Long classId) {
        return announcementRepository.findActiveForClass(classId, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Announcement> getPastAnnouncements(Long classId) {
        return announcementRepository.findPastForClass(classId, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<CampusLocation> searchLocations(String keyword, FacilityType type) {
        if ((keyword == null || keyword.trim().isEmpty()) && type == null) {
            return locationRepository.findAll();
        }
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return locationRepository.searchLocations(cleanKeyword, type);
    }

    @Transactional
    public AssignmentSubmission submitAssignment(Long studentId, Long assignmentId, Long subjectId, String title, MultipartFile file) throws IOException {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        Assignment assignment = null;
        if (assignmentId != null && assignmentId > 0) {
            assignment = assignmentRepository.findById(assignmentId).orElse(null);
        }

        Subject subject = null;
        if (assignment != null) {
            subject = assignment.getSubject();
            if (title == null || title.trim().isEmpty()) {
                title = assignment.getTitle();
            }
        } else if (subjectId != null) {
            subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + subjectId));
        }

        if (subject == null) {
            throw new IllegalArgumentException("Subject is required.");
        }

        String savedFileName = fileStorageService.storeAssignmentFile(file);

        // Check if student has already submitted for this assignment
        Optional<AssignmentSubmission> existingOpt = Optional.empty();
        if (assignment != null) {
            existingOpt = assignmentSubmissionRepository.findByAssignment_AssignmentIdAndStudent_StudentId(assignment.getAssignmentId(), studentId);
        }

        AssignmentSubmission submission;
        if (existingOpt.isPresent()) {
            submission = existingOpt.get();
            submission.setFilePath(savedFileName);
            submission.setStatus(AssignmentStatus.SUBMITTED);
            submission.setSubmittedAt(LocalDateTime.now());
            if (title != null && !title.trim().isEmpty()) {
                submission.setAssignmentTitle(title);
            }
        } else {
            submission = AssignmentSubmission.builder()
                    .assignment(assignment)
                    .student(student)
                    .subject(subject)
                    .assignmentTitle(title != null && !title.trim().isEmpty() ? title : "Coursework Assignment")
                    .filePath(savedFileName)
                    .status(AssignmentStatus.SUBMITTED)
                    .submittedAt(LocalDateTime.now())
                    .build();
        }

        AssignmentSubmission saved = assignmentSubmissionRepository.save(submission);

        if (subject.getAssignedTeacher() != null && subject.getAssignedTeacher().getUser() != null) {
            notificationService.sendNotification(
                    subject.getAssignedTeacher().getUser(),
                    "New Coursework Submission",
                    student.getFullName() + " submitted '" + submission.getAssignmentTitle() + "' for " + subject.getDisplayName(),
                    NotificationType.ASSIGNMENT
            );
        }

        return saved;
    }

    @Transactional
    public AssignmentSubmission submitAssignment(Long studentId, Long subjectId, String title, MultipartFile file) throws IOException {
        return submitAssignment(studentId, null, subjectId, title, file);
    }

    @Transactional
    public IssueTicket raiseIssueTicket(Long studentId, IssueTargetRole targetRole, Long teacherId,
                                        String issueCategory, String description, MultipartFile proofFile) throws IOException {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));

        Teacher teacher = null;
        if (targetRole == IssueTargetRole.TEACHER && teacherId != null) {
            teacher = teacherRepository.findById(teacherId).orElse(null);
        }

        String proofFileName = null;
        if (proofFile != null && !proofFile.isEmpty()) {
            proofFileName = fileStorageService.storeIssueProofFile(proofFile);
        }

        IssueTicket ticket = IssueTicket.builder()
                .student(student)
                .targetRole(targetRole)
                .assignedTeacher(teacher)
                .issueCategory(issueCategory)
                .description(description)
                .proofFilePath(proofFileName)
                .status(IssueStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        IssueTicket saved = issueTicketRepository.save(ticket);

        if (teacher != null && teacher.getUser() != null) {
            notificationService.sendNotification(
                    teacher.getUser(),
                    "New Student Grievance Ticket",
                    student.getFullName() + " raised an issue (" + issueCategory + "): " + description,
                    NotificationType.ISSUE_UPDATE
            );
        }

        return saved;
    }
}
