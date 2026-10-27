package com.campus360.controller;

import com.campus360.entity.*;
import com.campus360.enums.FacilityType;
import com.campus360.enums.IssueStatus;
import com.campus360.enums.IssueTargetRole;
import com.campus360.repository.SubjectRepository;
import com.campus360.repository.TeacherRepository;
import com.campus360.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.*;

@Controller
@RequestMapping("/student")
public class StudentController {

    private final StudentService studentService;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;

    public StudentController(StudentService studentService,
                             SubjectRepository subjectRepository,
                             TeacherRepository teacherRepository) {
        this.studentService = studentService;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        Student student = studentService.getStudentByUsername(principal.getName()).orElse(null);
        if (student == null) return "redirect:/login";

        double overallAttendance = studentService.getOverallAttendancePercentage(student.getStudentId());
        List<StudentService.SubjectAttendanceStat> subjectStats = studentService.getSubjectAttendanceStats(student.getStudentId());
        List<Announcement> activeAnnouncements = studentService.getActiveAnnouncements(student.getClassGroup().getClassId());
        List<AcademicResult> recentResults = studentService.getAcademicResults(student.getStudentId());

        model.addAttribute("student", student);
        model.addAttribute("overallAttendance", overallAttendance);
        model.addAttribute("subjectStats", subjectStats);
        model.addAttribute("activeAnnouncements", activeAnnouncements);
        model.addAttribute("recentResults", recentResults.stream().limit(5).toList());

        return "student/dashboard";
    }

    @GetMapping("/attendance")
    public String attendancePage(Model model, Principal principal) {
        Student student = studentService.getStudentByUsername(principal.getName()).orElse(null);
        if (student == null) return "redirect:/login";

        double overallAttendance = studentService.getOverallAttendancePercentage(student.getStudentId());
        List<StudentService.SubjectAttendanceStat> subjectStats = studentService.getSubjectAttendanceStats(student.getStudentId());
        List<Attendance> history = studentService.getAttendanceHistory(student.getStudentId());

        model.addAttribute("student", student);
        model.addAttribute("overallAttendance", overallAttendance);
        model.addAttribute("subjectStats", subjectStats);
        model.addAttribute("history", history);

        return "student/attendance";
    }

    @GetMapping("/vault")
    public String vaultPage(Model model, Principal principal) {
        Student student = studentService.getStudentByUsername(principal.getName()).orElse(null);
        if (student == null) return "redirect:/login";

        List<StudentDocument> documents = studentService.getVaultDocuments(student.getStudentId());
        model.addAttribute("student", student);
        model.addAttribute("documents", documents);

        return "student/vault";
    }

    @GetMapping("/results")
    public String resultsPage(Model model, Principal principal) {
        Student student = studentService.getStudentByUsername(principal.getName()).orElse(null);
        if (student == null) return "redirect:/login";

        List<AcademicResult> results = studentService.getAcademicResults(student.getStudentId());
        model.addAttribute("student", student);
        model.addAttribute("results", results);

        return "student/results";
    }

    @GetMapping("/assignments")
    public String assignmentsPage(Model model, Principal principal) {
        Student student = studentService.getStudentByUsername(principal.getName()).orElse(null);
        if (student == null) return "redirect:/login";

        List<Assignment> assignedTasks = studentService.getActiveAssignmentsForStudent(student.getClassGroup().getClassId());
        List<AssignmentSubmission> submissions = studentService.getMyAssignments(student.getStudentId());
        List<Subject> classSubjects = subjectRepository.findByClassGroup(student.getClassGroup());

        Map<Long, AssignmentSubmission> submissionByAssignmentId = new HashMap<>();
        for (AssignmentSubmission sub : submissions) {
            if (sub.getAssignment() != null) {
                submissionByAssignmentId.put(sub.getAssignment().getAssignmentId(), sub);
            }
        }

        model.addAttribute("student", student);
        model.addAttribute("assignedTasks", assignedTasks);
        model.addAttribute("submissions", submissions);
        model.addAttribute("submissionMap", submissionByAssignmentId);
        model.addAttribute("subjects", classSubjects);

        return "student/assignments";
    }

    @PostMapping("/assignments/submit")
    public String submitAssignment(@RequestParam(required = false) Long assignmentId,
                                   @RequestParam(required = false) Long subjectId,
                                   @RequestParam(required = false) String title,
                                   @RequestParam("file") MultipartFile file,
                                   Principal principal,
                                   RedirectAttributes redirectAttributes) {
        try {
            Student student = studentService.getStudentByUsername(principal.getName()).orElseThrow();
            AssignmentSubmission submission = studentService.submitAssignment(student.getStudentId(), assignmentId, subjectId, title, file);
            redirectAttributes.addFlashAttribute("successMessage", "Assignment '" + submission.getAssignmentTitle() + "' submitted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Assignment submission failed: " + e.getMessage());
        }
        return "redirect:/student/assignments";
    }

    @GetMapping("/issues")
    public String issuesPage(Model model, Principal principal) {
        Student student = studentService.getStudentByUsername(principal.getName()).orElse(null);
        if (student == null) return "redirect:/login";

        List<IssueTicket> tickets = studentService.getMyTickets(student.getStudentId());
        List<Teacher> teachers = teacherRepository.findAll();
        List<Subject> mySubjects = subjectRepository.findByClassGroup(student.getClassGroup());

        model.addAttribute("student", student);
        model.addAttribute("tickets", tickets);
        model.addAttribute("teachers", teachers);
        model.addAttribute("subjects", mySubjects);
        model.addAttribute("targetRoles", IssueTargetRole.values());

        return "student/issues";
    }

    @PostMapping("/issues/raise")
    public String raiseIssue(@RequestParam(defaultValue = "TEACHER") IssueTargetRole targetRole,
                             @RequestParam(required = false) Long teacherId,
                             @RequestParam String issueCategory,
                             @RequestParam String description,
                             @RequestParam(value = "proofFile", required = false) MultipartFile proofFile,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {
        try {
            Student student = studentService.getStudentByUsername(principal.getName()).orElseThrow();
            if (targetRole == IssueTargetRole.TEACHER && (teacherId == null || teacherId <= 0)) {
                List<Subject> subjects = subjectRepository.findByClassGroup(student.getClassGroup());
                for (Subject s : subjects) {
                    if (s.getAssignedTeacher() != null) {
                        teacherId = s.getAssignedTeacher().getTeacherId();
                        break;
                    }
                }
                if (teacherId == null && !teachersEmpty()) {
                    List<Teacher> all = teacherRepository.findAll();
                    if (!all.isEmpty()) teacherId = all.get(0).getTeacherId();
                }
            }
            studentService.raiseIssueTicket(student.getStudentId(), targetRole, teacherId, issueCategory, description, proofFile);
            redirectAttributes.addFlashAttribute("successMessage", "Grievance ticket successfully submitted to faculty member!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to submit ticket: " + e.getMessage());
        }
        return "redirect:/student/issues";
    }

    private boolean teachersEmpty() {
        return teacherRepository.count() == 0;
    }

    @GetMapping("/navigation")
    public String navigationDirectory() {
        return "redirect:/student/dashboard";
    }

    @GetMapping("/announcements")
    public String announcementsPage(Model model, Principal principal) {
        Student student = studentService.getStudentByUsername(principal.getName()).orElse(null);
        if (student == null) return "redirect:/login";

        List<Announcement> active = studentService.getActiveAnnouncements(student.getClassGroup().getClassId());
        List<Announcement> past = studentService.getPastAnnouncements(student.getClassGroup().getClassId());

        model.addAttribute("student", student);
        model.addAttribute("activeAnnouncements", active);
        model.addAttribute("pastAnnouncements", past);

        return "student/announcements";
    }

    @GetMapping("/question-papers")
    public String questionPapersPage(Model model, Principal principal) {
        Student student = studentService.getStudentByUsername(principal.getName()).orElse(null);
        if (student == null) return "redirect:/login";

        List<Subject> subjects = subjectRepository.findByClassGroup(student.getClassGroup());
        model.addAttribute("student", student);
        model.addAttribute("subjects", subjects);
        model.addAttribute("muPortalUrl", "https://muquestionpapers.com");

        return "student/question-papers";
    }
}
