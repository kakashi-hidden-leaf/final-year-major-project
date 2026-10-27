package com.campus360.controller;

import com.campus360.entity.*;
import com.campus360.enums.AssignmentStatus;
import com.campus360.enums.AttendanceStatus;
import com.campus360.enums.IssueStatus;
import com.campus360.repository.*;
import com.campus360.service.CampusSchedulerService;
import com.campus360.service.TeacherService;
import com.campus360.service.TimetableExportService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    private final TeacherService teacherService;
    private final TeacherRepository teacherRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final AttendanceRepository attendanceRepository;
    private final AcademicResultRepository academicResultRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentSubmissionRepository assignmentSubmissionRepository;
    private final IssueTicketRepository issueTicketRepository;
    private final AnnouncementRepository announcementRepository;
    private final ClassGroupRepository classGroupRepository;
    private final CampusLocationRepository campusLocationRepository;
    private final TimetableExportService timetableExportService;
    private final CampusSchedulerService campusSchedulerService;

    public TeacherController(TeacherService teacherService,
                             TeacherRepository teacherRepository,
                             SubjectRepository subjectRepository,
                             StudentRepository studentRepository,
                             AttendanceRepository attendanceRepository,
                             AcademicResultRepository academicResultRepository,
                             AssignmentRepository assignmentRepository,
                             AssignmentSubmissionRepository assignmentSubmissionRepository,
                             IssueTicketRepository issueTicketRepository,
                             AnnouncementRepository announcementRepository,
                             ClassGroupRepository classGroupRepository,
                             CampusLocationRepository campusLocationRepository,
                             TimetableExportService timetableExportService,
                             CampusSchedulerService campusSchedulerService) {
        this.teacherService = teacherService;
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
        this.studentRepository = studentRepository;
        this.attendanceRepository = attendanceRepository;
        this.academicResultRepository = academicResultRepository;
        this.assignmentRepository = assignmentRepository;
        this.assignmentSubmissionRepository = assignmentSubmissionRepository;
        this.issueTicketRepository = issueTicketRepository;
        this.announcementRepository = announcementRepository;
        this.classGroupRepository = classGroupRepository;
        this.campusLocationRepository = campusLocationRepository;
        this.timetableExportService = timetableExportService;
        this.campusSchedulerService = campusSchedulerService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        Teacher teacher = teacherRepository.findByUser_Username(principal.getName()).orElse(null);
        if (teacher == null) return "redirect:/login";

        List<Subject> subjects = teacherService.getAssignedSubjects(teacher.getTeacherId());
        List<TeacherSchedule> todayLectures = teacherService.getTodayLectures(teacher.getTeacherId());
        List<AssignmentSubmission> pendingAssignments = assignmentSubmissionRepository.findBySubject_AssignedTeacher_TeacherIdOrderBySubmittedAtDesc(teacher.getTeacherId());
        List<IssueTicket> issues = issueTicketRepository.findByAssignedTeacher_TeacherIdOrderByCreatedAtDesc(teacher.getTeacherId());

        long pendingAssignmentCount = pendingAssignments.stream().filter(a -> a.getStatus() == AssignmentStatus.SUBMITTED).count();
        long openIssuesCount = issues.stream().filter(i -> i.getStatus() == IssueStatus.OPEN).count();
        List<IssueTicket> openIssues = issues.stream().filter(i -> i.getStatus() == IssueStatus.OPEN).toList();

        model.addAttribute("teacher", teacher);
        model.addAttribute("assignedSubjects", subjects);
        model.addAttribute("todayLectures", todayLectures);
        model.addAttribute("pendingAssignmentCount", pendingAssignmentCount);
        model.addAttribute("openIssuesCount", openIssuesCount);
        model.addAttribute("openIssuesList", openIssues);
        model.addAttribute("recentAssignments", pendingAssignments.stream().limit(5).toList());
        model.addAttribute("recentIssues", issues.stream().limit(5).toList());
        model.addAttribute("statuses", IssueStatus.values());
        model.addAttribute("allClasses", classGroupRepository.findAllByOrderByAcademicYearAscBranchAscDivisionAsc());

        return "teacher/dashboard";
    }

    @GetMapping("/subjects")
    public String subjectsPage(Model model, Principal principal) {
        Teacher teacher = teacherRepository.findByUser_Username(principal.getName()).orElse(null);
        if (teacher == null) return "redirect:/login";

        List<Subject> assignedSubjects = teacherService.getAssignedSubjects(teacher.getTeacherId());
        List<ClassGroup> allClasses = classGroupRepository.findAllByOrderByAcademicYearAscBranchAscDivisionAsc();

        Map<Long, Long> subjectStudentCounts = new HashMap<>();
        for (Subject sub : assignedSubjects) {
            long count = studentRepository.countByClassGroup_ClassId(sub.getClassGroup().getClassId());
            subjectStudentCounts.put(sub.getSubjectId(), count);
        }

        model.addAttribute("teacher", teacher);
        model.addAttribute("assignedSubjects", assignedSubjects);
        model.addAttribute("allClasses", allClasses);
        model.addAttribute("subjectStudentCounts", subjectStudentCounts);
        return "teacher/subjects";
    }

    @PostMapping("/subjects/claim")
    public String claimSubject(@RequestParam Long classId,
                               @RequestParam(required = false) Long existingSubjectId,
                               @RequestParam(required = false) String newSubjectName,
                               @RequestParam(required = false) String newSubjectCode,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        Teacher teacher = teacherRepository.findByUser_Username(principal.getName()).orElse(null);
        if (teacher == null) return "redirect:/login";

        ClassGroup classGroup = classGroupRepository.findById(classId).orElse(null);
        if (classGroup == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Selected class not found.");
            return "redirect:/teacher/subjects";
        }

        if (existingSubjectId != null && existingSubjectId > 0) {
            Subject existingSub = subjectRepository.findById(existingSubjectId).orElse(null);
            if (existingSub != null) {
                existingSub.setAssignedTeacher(teacher);
                subjectRepository.save(existingSub);
                redirectAttributes.addFlashAttribute("successMessage",
                        "Successfully assigned " + existingSub.getDisplayName() + " to your teaching portfolio!");
                return "redirect:/teacher/subjects";
            }
        }

        if (newSubjectName != null && !newSubjectName.trim().isEmpty()) {
            String code = (newSubjectCode != null && !newSubjectCode.trim().isEmpty())
                    ? newSubjectCode.trim().toUpperCase()
                    : classGroup.getBranch().toUpperCase() + (200 + new Random().nextInt(300));

            Subject newSubject = Subject.builder()
                    .subjectName(newSubjectName.trim())
                    .subjectCode(code)
                    .classGroup(classGroup)
                    .assignedTeacher(teacher)
                    .build();
            subjectRepository.save(newSubject);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Successfully registered and assigned new subject " + newSubject.getDisplayName() + " for " + classGroup.getDisplayName() + "!");
            return "redirect:/teacher/subjects";
        }

        redirectAttributes.addFlashAttribute("errorMessage", "Please select an existing subject or enter new subject details.");
        return "redirect:/teacher/subjects";
    }

    @PostMapping("/subjects/{subjectId}/drop")
    public String dropSubject(@PathVariable Long subjectId, Principal principal, RedirectAttributes redirectAttributes) {
        Teacher teacher = teacherRepository.findByUser_Username(principal.getName()).orElse(null);
        if (teacher == null) return "redirect:/login";

        Subject subject = subjectRepository.findById(subjectId).orElse(null);
        if (subject != null && subject.getAssignedTeacher() != null &&
                subject.getAssignedTeacher().getTeacherId().equals(teacher.getTeacherId())) {
            subject.setAssignedTeacher(null);
            subjectRepository.save(subject);
            redirectAttributes.addFlashAttribute("successMessage", "Subject " + subject.getDisplayName() + " unassigned from your teaching portfolio.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to unassign subject.");
        }
        return "redirect:/teacher/subjects";
    }

    @GetMapping("/api/classes/{classId}/subjects")
    @ResponseBody
    public List<Map<String, Object>> getClassSubjects(@PathVariable Long classId, Principal principal) {
        Teacher currentTeacher = teacherRepository.findByUser_Username(principal.getName()).orElse(null);
        List<Subject> subjects = subjectRepository.findByClassGroup_ClassId(classId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Subject s : subjects) {
            Map<String, Object> map = new HashMap<>();
            map.put("subjectId", s.getSubjectId());
            map.put("subjectCode", s.getSubjectCode());
            map.put("subjectName", s.getSubjectName());
            map.put("displayName", s.getDisplayName());
            if (s.getAssignedTeacher() != null) {
                map.put("assignedTeacherName", s.getAssignedTeacher().getFullName());
                map.put("isAssignedToMe", currentTeacher != null && s.getAssignedTeacher().getTeacherId().equals(currentTeacher.getTeacherId()));
            } else {
                map.put("assignedTeacherName", "Unassigned");
                map.put("isAssignedToMe", false);
            }
            result.add(map);
        }
        return result;
    }

    @GetMapping("/attendance")
    public String attendancePage(@RequestParam(required = false) Long subjectId,
                                 @RequestParam(required = false) String date,
                                 Model model, Principal principal) {
        Teacher teacher = teacherRepository.findByUser_Username(principal.getName()).orElse(null);
        if (teacher == null) return "redirect:/login";

        List<Subject> subjects = teacherService.getAssignedSubjects(teacher.getTeacherId());
        model.addAttribute("subjects", subjects);

        LocalDate selectedDate = (date != null && !date.isEmpty()) ? LocalDate.parse(date) : LocalDate.now();
        model.addAttribute("selectedDate", selectedDate);

        Subject selectedSubject = null;
        if (subjectId != null) {
            selectedSubject = subjectRepository.findById(subjectId).orElse(null);
        } else if (!subjects.isEmpty()) {
            selectedSubject = subjects.get(0);
        }

        model.addAttribute("selectedSubject", selectedSubject);

        if (selectedSubject != null) {
            List<Student> students = studentRepository.findByClassGroup(selectedSubject.getClassGroup());
            List<Attendance> existingLogs = attendanceRepository.findBySubjectAndAttendanceDate(selectedSubject, selectedDate);
            Map<Long, AttendanceStatus> currentStatusMap = new HashMap<>();
            for (Attendance a : existingLogs) {
                currentStatusMap.put(a.getStudent().getStudentId(), a.getStatus());
            }

            model.addAttribute("students", students);
            model.addAttribute("currentStatusMap", currentStatusMap);
        }

        model.addAttribute("attendanceStatuses", AttendanceStatus.values());
        return "teacher/attendance";
    }

    @PostMapping("/attendance/save")
    public String saveAttendance(@RequestParam Long subjectId,
                                 @RequestParam String date,
                                 HttpServletRequest request,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        try {
            LocalDate attendanceDate = LocalDate.parse(date);
            Subject subject = subjectRepository.findById(subjectId).orElseThrow();
            List<Student> students = studentRepository.findByClassGroup(subject.getClassGroup());

            Map<Long, AttendanceStatus> attendanceMap = new HashMap<>();
            for (Student s : students) {
                String paramVal = request.getParameter("status_" + s.getStudentId());
                if (paramVal != null) {
                    attendanceMap.put(s.getStudentId(), AttendanceStatus.valueOf(paramVal));
                } else {
                    attendanceMap.put(s.getStudentId(), AttendanceStatus.PRESENT);
                }
            }

            teacherService.recordOrUpdateAttendance(subjectId, attendanceDate, attendanceMap, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Attendance successfully recorded/updated for " + attendanceDate + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error recording attendance: " + e.getMessage());
        }

        return "redirect:/teacher/attendance?subjectId=" + subjectId + "&date=" + date;
    }

    @GetMapping("/results")
    public String resultsPage(@RequestParam(required = false) Long subjectId, Model model, Principal principal) {
        Teacher teacher = teacherRepository.findByUser_Username(principal.getName()).orElse(null);
        if (teacher == null) return "redirect:/login";

        List<Subject> subjects = teacherService.getAssignedSubjects(teacher.getTeacherId());
        model.addAttribute("subjects", subjects);
        model.addAttribute("students", studentRepository.findAll());

        List<AcademicResult> results;
        if (subjectId != null) {
            results = academicResultRepository.findBySubject_SubjectIdOrderByUploadedAtDesc(subjectId);
            model.addAttribute("selectedSubjectId", subjectId);
            model.addAttribute("selectedSubject", subjectRepository.findById(subjectId).orElse(null));
        } else {
            results = academicResultRepository.findByUploadedByTeacher_TeacherIdOrderByUploadedAtDesc(teacher.getTeacherId());
            model.addAttribute("selectedSubjectId", null);
            model.addAttribute("selectedSubject", null);
        }
        model.addAttribute("results", results);
        return "teacher/results";
    }

    @GetMapping("/results/template")
    public ResponseEntity<byte[]> downloadResultTemplate(@RequestParam(required = false) Long subjectId, Principal principal) {
        Teacher teacher = teacherRepository.findByUser_Username(principal.getName()).orElseThrow();
        List<Subject> subjects = teacherService.getAssignedSubjects(teacher.getTeacherId());
        if (subjects.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Long targetSubjectId = subjectId != null ? subjectId : subjects.get(0).getSubjectId();
        Subject subject = subjectRepository.findById(targetSubjectId).orElse(subjects.get(0));

        byte[] csvBytes = teacherService.generateRosterCsv(subject.getSubjectId());
        String filename = "marks_template_" + subject.getSubjectCode().replaceAll("[^a-zA-Z0-9]", "_") + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvBytes);
    }

    @PostMapping("/results/bulk-upload")
    public String bulkUploadResults(@RequestParam Long subjectId,
                                    @RequestParam String examName,
                                    @RequestParam Double maxMarks,
                                    @RequestParam("file") MultipartFile file,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        try {
            int count = teacherService.uploadBulkResults(subjectId, examName, maxMarks, file, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Successfully published exam marks for " + count + " students in '" + examName + "'!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload bulk results: " + e.getMessage());
        }
        return "redirect:/teacher/results";
    }

    @PostMapping("/results/upload")
    public String uploadResult(@RequestParam Long subjectId,
                               @RequestParam Long studentId,
                               @RequestParam String examName,
                               @RequestParam Double marksObtained,
                               @RequestParam Double maxMarks,
                               @RequestParam String grade,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            teacherService.uploadResult(subjectId, studentId, examName, marksObtained, maxMarks, grade, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Exam result published successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload result: " + e.getMessage());
        }
        return "redirect:/teacher/results";
    }

    @GetMapping("/assignments")
    public String assignmentsPage(@RequestParam(required = false) Long subjectId, Model model, Principal principal) {
        Teacher teacher = teacherRepository.findByUser_Username(principal.getName()).orElse(null);
        if (teacher == null) return "redirect:/login";

        List<Subject> subjects = teacherService.getAssignedSubjects(teacher.getTeacherId());
        List<Assignment> assignments;
        List<AssignmentSubmission> submissions;

        if (subjectId != null) {
            assignments = assignmentRepository.findByTeacher_TeacherIdAndSubject_SubjectIdOrderByCreatedAtDesc(teacher.getTeacherId(), subjectId);
            submissions = assignmentSubmissionRepository.findBySubject_SubjectIdOrderBySubmittedAtDesc(subjectId);
            model.addAttribute("selectedSubjectId", subjectId);
            model.addAttribute("selectedSubject", subjectRepository.findById(subjectId).orElse(null));
        } else {
            assignments = assignmentRepository.findByTeacher_TeacherIdOrderByCreatedAtDesc(teacher.getTeacherId());
            submissions = assignmentSubmissionRepository.findBySubject_AssignedTeacher_TeacherIdOrderBySubmittedAtDesc(teacher.getTeacherId());
            model.addAttribute("selectedSubjectId", null);
            model.addAttribute("selectedSubject", null);
        }

        model.addAttribute("teacher", teacher);
        model.addAttribute("subjects", subjects);
        model.addAttribute("assignments", assignments);
        model.addAttribute("submissions", submissions);
        model.addAttribute("assignmentStatuses", AssignmentStatus.values());
        return "teacher/assignments";
    }

    @PostMapping("/assignments/create")
    public String createAssignment(@RequestParam Long subjectId,
                                   @RequestParam String title,
                                   @RequestParam String description,
                                   @RequestParam String dueDate,
                                   @RequestParam(value = "questionFile", required = false) MultipartFile questionFile,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            LocalDate due = LocalDate.parse(dueDate);
            teacherService.createAssignment(subjectId, title, description, due, questionFile, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Assignment task '" + title + "' published to students successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create assignment: " + e.getMessage());
        }
        return "redirect:/teacher/assignments";
    }

    @PostMapping("/assignments/{id}/delete")
    public String deleteAssignment(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            teacherService.deleteAssignment(id);
            redirectAttributes.addFlashAttribute("successMessage", "Assignment task and all submissions removed successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete assignment: " + e.getMessage());
        }
        return "redirect:/teacher/assignments";
    }

    @PostMapping("/assignments/{id}/review")
    public String reviewAssignment(@PathVariable Long id,
                                   @RequestParam AssignmentStatus status,
                                   @RequestParam String remarks,
                                   RedirectAttributes redirectAttributes) {
        try {
            teacherService.reviewAssignment(id, status, remarks);
            redirectAttributes.addFlashAttribute("successMessage", "Assignment marked as " + status + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to review assignment: " + e.getMessage());
        }
        return "redirect:/teacher/assignments";
    }

    @GetMapping("/issues")
    public String issuesPage(Model model, Principal principal) {
        Teacher teacher = teacherRepository.findByUser_Username(principal.getName()).orElse(null);
        if (teacher == null) return "redirect:/login";

        List<IssueTicket> tickets = issueTicketRepository.findByAssignedTeacher_TeacherIdOrderByCreatedAtDesc(teacher.getTeacherId());
        model.addAttribute("tickets", tickets);
        model.addAttribute("statuses", IssueStatus.values());
        return "teacher/issues";
    }

    @PostMapping("/issues/{id}/resolve")
    public String resolveIssue(@PathVariable Long id,
                               @RequestParam IssueStatus status,
                               @RequestParam String resolutionRemarks,
                               @RequestParam(required = false, defaultValue = "issues") String redirectSource,
                               RedirectAttributes redirectAttributes) {
        try {
            teacherService.resolveAcademicTicket(id, status, resolutionRemarks);
            redirectAttributes.addFlashAttribute("successMessage", "Grievance ticket #" + id + " updated to " + status + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update ticket: " + e.getMessage());
        }
        if ("dashboard".equalsIgnoreCase(redirectSource)) {
            return "redirect:/teacher/dashboard";
        }
        return "redirect:/teacher/issues";
    }

    @GetMapping("/announcements")
    public String announcementsPage(Model model, Principal principal) {
        Teacher teacher = teacherRepository.findByUser_Username(principal.getName()).orElse(null);
        if (teacher == null) return "redirect:/login";

        model.addAttribute("classGroups", classGroupRepository.findAll());
        model.addAttribute("announcements", announcementRepository.findByCreatedByTeacher_TeacherIdOrderByCreatedAtDesc(teacher.getTeacherId()));
        return "teacher/announcements";
    }

    @PostMapping("/announcements/create")
    public String createAnnouncement(@RequestParam String title,
                                     @RequestParam String message,
                                     @RequestParam(required = false) Long classGroupId,
                                     @RequestParam String publishDate,
                                     @RequestParam String expiryDate,
                                     Principal principal,
                                     RedirectAttributes redirectAttributes) {
        try {
            LocalDate pub = LocalDate.parse(publishDate);
            LocalDate exp = LocalDate.parse(expiryDate);
            teacherService.createAnnouncement(title, message, classGroupId, pub, exp, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Announcement scheduled successfully! In-app notifications will be sent automatically.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to schedule announcement: " + e.getMessage());
        }
        return "redirect:/teacher/announcements";
    }

    @PostMapping("/announcements/{id}/delete")
    public String deleteAnnouncement(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            teacherService.deleteAnnouncement(id);
            redirectAttributes.addFlashAttribute("successMessage", "Announcement removed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove announcement: " + e.getMessage());
        }
        return "redirect:/teacher/announcements";
    }

    @GetMapping("/timetable")
    public String timetablePage(Model model, Principal principal) {
        Teacher teacher = teacherRepository.findByUser_Username(principal.getName()).orElse(null);
        if (teacher == null) return "redirect:/login";

        List<TeacherSchedule> schedules = teacherService.getAllWeeklySchedules(teacher.getTeacherId());
        Map<TeacherSchedule, String> calendarLinks = new LinkedHashMap<>();
        for (TeacherSchedule s : schedules) {
            calendarLinks.put(s, timetableExportService.generateGoogleCalendarLink(s));
        }

        model.addAttribute("teacher", teacher);
        model.addAttribute("schedules", schedules);
        model.addAttribute("calendarLinks", calendarLinks);
        model.addAttribute("daysOfWeek", DayOfWeek.values());
        model.addAttribute("classGroups", classGroupRepository.findAll());
        model.addAttribute("subjects", teacherService.getAssignedSubjects(teacher.getTeacherId()));
        model.addAttribute("locations", campusLocationRepository.findAll());

        return "teacher/timetable";
    }

    @PostMapping("/timetable/add")
    public String addTimetableSlot(@RequestParam Long classId,
                                   @RequestParam Long subjectId,
                                   @RequestParam Long locationId,
                                   @RequestParam DayOfWeek dayOfWeek,
                                   @RequestParam String startTime,
                                   @RequestParam String endTime,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            Teacher teacher = teacherRepository.findByUser_Username(principal.getName()).orElseThrow();
            LocalTime start = LocalTime.parse(startTime);
            LocalTime end = LocalTime.parse(endTime);

            teacherService.addTimetableSlot(teacher.getTeacherId(), classId, subjectId, locationId, dayOfWeek, start, end);
            redirectAttributes.addFlashAttribute("successMessage", "Lecture slot added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add timetable slot: " + e.getMessage());
        }
        return "redirect:/teacher/timetable";
    }

    @GetMapping("/timetable/export-ics")
    public ResponseEntity<byte[]> exportIcs(Principal principal) {
        Teacher teacher = teacherRepository.findByUser_Username(principal.getName()).orElseThrow();
        List<TeacherSchedule> schedules = teacherService.getAllWeeklySchedules(teacher.getTeacherId());
        String icsContent = timetableExportService.generateIcsContent(teacher.getFullName(), schedules);

        byte[] bytes = icsContent.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"campus360_timetable_" + teacher.getEmployeeId() + ".ics\"")
                .contentType(MediaType.parseMediaType("text/calendar; charset=UTF-8"))
                .body(bytes);
    }

    @PostMapping("/timetable/simulate-alert")
    public String simulateAlert(Principal principal, RedirectAttributes redirectAttributes) {
        try {
            Teacher teacher = teacherRepository.findByUser_Username(principal.getName()).orElseThrow();
            boolean triggered = campusSchedulerService.simulateLectureAlert(teacher.getTeacherId());
            if (triggered) {
                redirectAttributes.addFlashAttribute("successMessage", "15-minute lecture alert successfully triggered! Check your notification bell icon in the top navbar.");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "No schedule slots found to simulate alert for.");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to simulate alert: " + e.getMessage());
        }
        return "redirect:/teacher/timetable";
    }

    @PostMapping("/timetable/slot/{id}/delete")
    public String deleteTimetableSlot(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            teacherService.deleteTimetableSlot(id);
            redirectAttributes.addFlashAttribute("successMessage", "Lecture slot removed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove slot: " + e.getMessage());
        }
        return "redirect:/teacher/timetable";
    }
}
