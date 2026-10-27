package com.campus360.service;

import com.campus360.entity.*;
import com.campus360.enums.*;
import com.campus360.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final AcademicResultRepository academicResultRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final IssueTicketRepository issueTicketRepository;
    private final AnnouncementRepository announcementRepository;
    private final TeacherScheduleRepository scheduleRepository;
    private final ClassGroupRepository classGroupRepository;
    private final CampusLocationRepository campusLocationRepository;
    private final FileStorageService fileStorageService;
    private final NotificationService notificationService;

    public TeacherService(TeacherRepository teacherRepository,
                          SubjectRepository subjectRepository,
                          AttendanceRepository attendanceRepository,
                          StudentRepository studentRepository,
                          AcademicResultRepository academicResultRepository,
                          AssignmentRepository assignmentRepository,
                          AssignmentSubmissionRepository assignmentSubmissionRepository,
                          IssueTicketRepository issueTicketRepository,
                          AnnouncementRepository announcementRepository,
                          TeacherScheduleRepository scheduleRepository,
                          ClassGroupRepository classGroupRepository,
                          CampusLocationRepository campusLocationRepository,
                          FileStorageService fileStorageService,
                          NotificationService notificationService) {
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
        this.academicResultRepository = academicResultRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentSubmissionRepository = assignmentSubmissionRepository;
        this.issueTicketRepository = issueTicketRepository;
        this.announcementRepository = announcementRepository;
        this.scheduleRepository = scheduleRepository;
        this.classGroupRepository = classGroupRepository;
        this.campusLocationRepository = campusLocationRepository;
        this.fileStorageService = fileStorageService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public Optional<Teacher> getTeacherByUsername(String username) {
        return teacherRepository.findByUser_Username(username);
    }

    @Transactional(readOnly = true)
    public List<Subject> getAssignedSubjects(Long teacherId) {
        return subjectRepository.findByAssignedTeacher_TeacherId(teacherId);
    }

    @Transactional(readOnly = true)
    public List<TeacherSchedule> getTodayLectures(Long teacherId) {
        DayOfWeek today = LocalDate.now().getDayOfWeek();
        return scheduleRepository.findByTeacher_TeacherIdAndDayOfWeekOrderByStartTimeAsc(teacherId, today);
    }

    @Transactional(readOnly = true)
    public List<TeacherSchedule> getAllWeeklySchedules(Long teacherId) {
        return scheduleRepository.findByTeacher_TeacherIdOrderByDayOfWeekAscStartTimeAsc(teacherId);
    }

    @Transactional
    public void recordOrUpdateAttendance(Long subjectId, LocalDate date, Map<Long, AttendanceStatus> attendanceMap, String teacherUsername) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + subjectId));

        Teacher teacher = teacherRepository.findByUser_Username(teacherUsername).orElse(null);

        for (Map.Entry<Long, AttendanceStatus> entry : attendanceMap.entrySet()) {
            Long studentId = entry.getKey();
            AttendanceStatus status = entry.getValue();

            Student student = studentRepository.findById(studentId).orElse(null);
            if (student == null) continue;

            Optional<Attendance> existingOpt = attendanceRepository.findByStudentAndSubjectAndAttendanceDate(student, subject, date);
            if (existingOpt.isPresent()) {
                Attendance existing = existingOpt.get();
                existing.setStatus(status);
                existing.setRecordedByTeacher(teacher);
                existing.setRecordedAt(LocalDateTime.now());
                attendanceRepository.save(existing);
            } else {
                Attendance newAttendance = Attendance.builder()
                        .student(student)
                        .subject(subject)
                        .attendanceDate(date)
                        .status(status)
                        .recordedByTeacher(teacher)
                        .recordedAt(LocalDateTime.now())
                        .build();
                attendanceRepository.save(newAttendance);
            }
        }
    }

    @Transactional
    public AcademicResult uploadResult(Long subjectId, Long studentId, String examName, Double marks, Double maxMarks, String grade, String teacherUsername) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + subjectId));
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found: " + studentId));
        Teacher teacher = teacherRepository.findByUser_Username(teacherUsername).orElse(null);

        Optional<AcademicResult> existingOpt = academicResultRepository.findByStudentAndSubjectAndExamName(student, subject, examName);
        AcademicResult result;
        if (existingOpt.isPresent()) {
            result = existingOpt.get();
            result.setMarksObtained(marks);
            result.setMaxMarks(maxMarks);
            result.setGrade(grade);
            result.setUploadedByTeacher(teacher);
            result.setUploadedAt(LocalDateTime.now());
        } else {
            result = AcademicResult.builder()
                    .subject(subject)
                    .student(student)
                    .examName(examName)
                    .marksObtained(marks)
                    .maxMarks(maxMarks)
                    .grade(grade)
                    .uploadedByTeacher(teacher)
                    .uploadedAt(LocalDateTime.now())
                    .build();
        }

        AcademicResult saved = academicResultRepository.save(result);

        notificationService.sendNotification(
                student.getUser(),
                "New Exam Result Uploaded: " + examName,
                "Marks for " + subject.getDisplayName() + " have been released. Score: " + marks + "/" + maxMarks + " (" + grade + ")",
                NotificationType.GENERAL
        );

        return saved;
    }

    @Transactional(readOnly = true)
    public byte[] generateRosterCsv(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + subjectId));

        List<Student> students = studentRepository.findByClassGroup_ClassIdOrderByRollNumberAsc(subject.getClassGroup().getClassId());
        StringBuilder csv = new StringBuilder();
        csv.append("Roll Number,Student Name,Marks Obtained,Grade\n");
        for (Student s : students) {
            csv.append(escapeCsvField(s.getRollNumber()))
               .append(",")
               .append(escapeCsvField(s.getFullName()))
               .append(",,\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Transactional
    public int uploadBulkResults(Long subjectId, String examName, Double maxMarks, MultipartFile file, String teacherUsername) throws IOException {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + subjectId));
        Teacher teacher = teacherRepository.findByUser_Username(teacherUsername).orElse(null);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded CSV file is empty.");
        }

        int count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (isFirstLine) {
                    isFirstLine = false;
                    String lower = line.toLowerCase();
                    if (lower.contains("roll") || lower.contains("student")) {
                        continue;
                    }
                }

                String[] tokens = parseCsvLine(line);
                if (tokens.length < 1) continue;

                String rollNo = tokens[0].trim().replace("\"", "");
                if (rollNo.isEmpty()) continue;

                Double marks = null;
                if (tokens.length >= 3 && !tokens[2].trim().isEmpty()) {
                    try {
                        marks = Double.parseDouble(tokens[2].trim().replace("\"", ""));
                    } catch (NumberFormatException ignored) {}
                }
                if (marks == null) continue;

                String grade = null;
                if (tokens.length >= 4 && !tokens[3].trim().isEmpty()) {
                    grade = tokens[3].trim().replace("\"", "");
                }
                if (grade == null || grade.isEmpty()) {
                    double pct = (marks / maxMarks) * 100.0;
                    if (pct >= 90) grade = "O";
                    else if (pct >= 80) grade = "A+";
                    else if (pct >= 70) grade = "A";
                    else if (pct >= 60) grade = "B+";
                    else if (pct >= 50) grade = "B";
                    else if (pct >= 40) grade = "C";
                    else grade = "F";
                }

                Optional<Student> studentOpt = studentRepository.findByRollNumber(rollNo);
                if (studentOpt.isEmpty()) continue;
                Student student = studentOpt.get();

                Optional<AcademicResult> existingOpt = academicResultRepository.findByStudentAndSubjectAndExamName(student, subject, examName);
                AcademicResult result;
                if (existingOpt.isPresent()) {
                    result = existingOpt.get();
                    result.setMarksObtained(marks);
                    result.setMaxMarks(maxMarks);
                    result.setGrade(grade);
                    result.setUploadedByTeacher(teacher);
                    result.setUploadedAt(LocalDateTime.now());
                } else {
                    result = AcademicResult.builder()
                            .student(student)
                            .subject(subject)
                            .examName(examName)
                            .marksObtained(marks)
                            .maxMarks(maxMarks)
                            .grade(grade)
                            .uploadedByTeacher(teacher)
                            .uploadedAt(LocalDateTime.now())
                            .build();
                }
                academicResultRepository.save(result);
                count++;

                notificationService.sendNotification(
                        student.getUser(),
                        "Exam Result Published: " + examName,
                        "Your result for " + subject.getDisplayName() + " has been released. Score: " + marks + "/" + maxMarks + " (" + grade + ")",
                        NotificationType.GENERAL
                );
            }
        }
        return count;
    }

    private String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens.toArray(new String[0]);
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    @Transactional
    public Assignment createAssignment(Long subjectId, String title, String description,
                                       LocalDate dueDate, MultipartFile questionFile,
                                       String teacherUsername) throws IOException {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + subjectId));
        Teacher teacher = teacherRepository.findByUser_Username(teacherUsername)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherUsername));

        String questionFilePath = null;
        if (questionFile != null && !questionFile.isEmpty()) {
            questionFilePath = fileStorageService.storeAssignmentFile(questionFile);
        }

        Assignment assignment = Assignment.builder()
                .subject(subject)
                .teacher(teacher)
                .title(title)
                .description(description)
                .dueDate(dueDate)
                .questionFilePath(questionFilePath)
                .createdAt(LocalDateTime.now())
                .build();

        Assignment saved = assignmentRepository.save(assignment);

        List<Student> students = studentRepository.findByClassGroup(subject.getClassGroup());
        for (Student st : students) {
            notificationService.sendNotification(
                    st.getUser(),
                    "New Coursework Assigned: " + title,
                    "Prof. " + teacher.getFullName() + " posted assignment '" + title + "' for " + subject.getDisplayName() + ". Due date: " + dueDate,
                    NotificationType.ASSIGNMENT
            );
        }

        return saved;
    }

    @Transactional
    public void deleteAssignment(Long assignmentId) {
        assignmentSubmissionRepository.deleteByAssignment_AssignmentId(assignmentId);
        assignmentRepository.deleteById(assignmentId);
    }

    @Transactional
    public void reviewAssignment(Long submissionId, AssignmentStatus status, String remarks) {
        AssignmentSubmission submission = assignmentSubmissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found: " + submissionId));

        submission.setStatus(status);
        submission.setRemarks(remarks);
        assignmentSubmissionRepository.save(submission);

        notificationService.sendNotification(
                submission.getStudent().getUser(),
                "Assignment Evaluated: " + submission.getAssignmentTitle(),
                "Status: " + status + ". Remarks: " + remarks,
                NotificationType.ASSIGNMENT
        );
    }

    @Transactional
    public void resolveAcademicTicket(Long ticketId, IssueStatus status, String resolutionRemarks) {
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
                "Your issue ticket #" + ticket.getTicketId() + " has been updated by faculty to " + status + ". Remarks: " + resolutionRemarks,
                NotificationType.ISSUE_UPDATE
        );
    }

    @Transactional
    public Announcement createAnnouncement(String title, String message, Long classGroupId, LocalDate publishDate, LocalDate expiryDate, String teacherUsername) {
        Teacher teacher = teacherRepository.findByUser_Username(teacherUsername).orElse(null);
        ClassGroup classGroup = null;
        if (classGroupId != null && classGroupId > 0) {
            classGroup = classGroupRepository.findById(classGroupId).orElse(null);
        }

        Announcement announcement = Announcement.builder()
                .title(title)
                .message(message)
                .targetClassGroup(classGroup)
                .createdByTeacher(teacher)
                .publishDate(publishDate)
                .expiryDate(expiryDate)
                .isNotificationSent(false)
                .createdAt(LocalDateTime.now())
                .build();

        return announcementRepository.save(announcement);
    }

    @Transactional
    public TeacherSchedule addTimetableSlot(Long teacherId, Long classId, Long subjectId, Long locationId,
                                            DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found: " + teacherId));
        ClassGroup classGroup = classGroupRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found: " + classId));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found: " + subjectId));
        CampusLocation location = campusLocationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Campus Location not found: " + locationId));

        TeacherSchedule slot = TeacherSchedule.builder()
                .teacher(teacher)
                .classGroup(classGroup)
                .subject(subject)
                .campusLocation(location)
                .dayOfWeek(dayOfWeek)
                .startTime(startTime)
                .endTime(endTime)
                .build();

        return scheduleRepository.save(slot);
    }

    @Transactional
    public void deleteTimetableSlot(Long scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }

    @Transactional
    public void deleteAnnouncement(Long announcementId) {
        announcementRepository.deleteById(announcementId);
    }
}
