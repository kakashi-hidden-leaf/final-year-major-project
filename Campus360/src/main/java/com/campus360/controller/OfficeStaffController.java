package com.campus360.controller;

import com.campus360.entity.*;
import com.campus360.enums.FacilityType;
import com.campus360.enums.IssueStatus;
import com.campus360.enums.IssueTargetRole;
import com.campus360.repository.*;
import com.campus360.service.OfficeStaffService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/staff")
public class OfficeStaffController {

    private final OfficeStaffService officeStaffService;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ClassGroupRepository classGroupRepository;
    private final SubjectRepository subjectRepository;
    private final CampusLocationRepository campusLocationRepository;
    private final StudentDocumentRepository studentDocumentRepository;
    private final IssueTicketRepository issueTicketRepository;

    public OfficeStaffController(OfficeStaffService officeStaffService,
                                 StudentRepository studentRepository,
                                 TeacherRepository teacherRepository,
                                 ClassGroupRepository classGroupRepository,
                                 SubjectRepository subjectRepository,
                                 CampusLocationRepository campusLocationRepository,
                                 StudentDocumentRepository studentDocumentRepository,
                                 IssueTicketRepository issueTicketRepository) {
        this.officeStaffService = officeStaffService;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.classGroupRepository = classGroupRepository;
        this.subjectRepository = subjectRepository;
        this.campusLocationRepository = campusLocationRepository;
        this.studentDocumentRepository = studentDocumentRepository;
        this.issueTicketRepository = issueTicketRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        Map<String, Object> stats = officeStaffService.getDashboardStats();
        model.addAllAttributes(stats);
        model.addAttribute("recentStudents", studentRepository.findAll());
        model.addAttribute("recentStaffIssues", issueTicketRepository.findByTargetRoleOrderByCreatedAtDesc(IssueTargetRole.OFFICE_STAFF));
        return "staff/dashboard";
    }

    @GetMapping("/enrollment")
    public String enrollmentPage(Model model) {
        model.addAttribute("classGroups", classGroupRepository.findAllByOrderByAcademicYearAscBranchAscDivisionAsc());
        model.addAttribute("students", studentRepository.findAll());
        model.addAttribute("teachers", teacherRepository.findAll());
        return "staff/enrollment";
    }

    @PostMapping("/enrollment/student")
    public String enrollStudent(@RequestParam String rollNumber,
                                @RequestParam String fullName,
                                @RequestParam String email,
                                @RequestParam String password,
                                @RequestParam Long classId,
                                @RequestParam(required = false) String admissionYear,
                                @RequestParam(required = false) String phone,
                                RedirectAttributes redirectAttributes) {
        try {
            officeStaffService.enrollStudent(rollNumber, fullName, email, password, classId, admissionYear, phone);
            redirectAttributes.addFlashAttribute("successMessage", "Student " + fullName + " (" + rollNumber + ") enrolled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Enrollment failed: " + e.getMessage());
        }
        return "redirect:/staff/enrollment";
    }

    @PostMapping("/enrollment/teacher")
    public String enrollTeacher(@RequestParam String employeeId,
                                @RequestParam String fullName,
                                @RequestParam String email,
                                @RequestParam String password,
                                @RequestParam String department,
                                @RequestParam String designation,
                                RedirectAttributes redirectAttributes) {
        try {
            officeStaffService.enrollTeacher(employeeId, fullName, email, password, department, designation);
            redirectAttributes.addFlashAttribute("successMessage", "Teacher " + fullName + " (" + employeeId + ") registered successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Teacher registration failed: " + e.getMessage());
        }
        return "redirect:/staff/enrollment";
    }

    @PostMapping("/students/{id}/edit")
    public String editStudent(@PathVariable Long id,
                              @RequestParam String fullName,
                              @RequestParam String email,
                              @RequestParam Long classId,
                              @RequestParam(required = false) String admissionYear,
                              @RequestParam(required = false) String phone,
                              @RequestParam(required = false) String newPassword,
                              @RequestParam(defaultValue = "true") Boolean isActive,
                              RedirectAttributes redirectAttributes) {
        try {
            officeStaffService.updateStudent(id, fullName, email, classId, admissionYear, phone, newPassword, isActive);
            redirectAttributes.addFlashAttribute("successMessage", "Student details updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update student: " + e.getMessage());
        }
        return "redirect:/staff/enrollment";
    }

    @PostMapping("/students/{id}/delete")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            officeStaffService.deleteStudent(id);
            redirectAttributes.addFlashAttribute("successMessage", "Student record removed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove student: " + e.getMessage());
        }
        return "redirect:/staff/enrollment";
    }

    @PostMapping("/teachers/{id}/edit")
    public String editTeacher(@PathVariable Long id,
                              @RequestParam String fullName,
                              @RequestParam String email,
                              @RequestParam String department,
                              @RequestParam String designation,
                              @RequestParam(required = false) String newPassword,
                              @RequestParam(defaultValue = "true") Boolean isActive,
                              RedirectAttributes redirectAttributes) {
        try {
            officeStaffService.updateTeacher(id, fullName, email, department, designation, newPassword, isActive);
            redirectAttributes.addFlashAttribute("successMessage", "Teacher details updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update teacher: " + e.getMessage());
        }
        return "redirect:/staff/enrollment";
    }

    @PostMapping("/teachers/{id}/delete")
    public String deleteTeacher(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            officeStaffService.deleteTeacher(id);
            redirectAttributes.addFlashAttribute("successMessage", "Teacher record removed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove teacher: " + e.getMessage());
        }
        return "redirect:/staff/enrollment";
    }

    @GetMapping("/academic-setup")
    public String academicSetupPage(Model model) {
        model.addAttribute("classGroups", classGroupRepository.findAllByOrderByAcademicYearAscBranchAscDivisionAsc());
        model.addAttribute("subjects", subjectRepository.findAll());
        model.addAttribute("teachers", teacherRepository.findAll());
        model.addAttribute("locations", campusLocationRepository.findAll());
        model.addAttribute("facilityTypes", FacilityType.values());
        return "staff/academic-setup";
    }

    @PostMapping("/academic-setup/class-group")
    public String createClassGroup(@RequestParam String courseName,
                                   @RequestParam String branch,
                                   @RequestParam String academicYear,
                                   @RequestParam String division,
                                   RedirectAttributes redirectAttributes) {
        try {
            officeStaffService.createClassGroup(courseName, branch, academicYear, division);
            redirectAttributes.addFlashAttribute("successMessage", "Class Group created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create Class Group: " + e.getMessage());
        }
        return "redirect:/staff/academic-setup";
    }

    @PostMapping("/academic-setup/subject")
    public String createSubject(@RequestParam String subjectCode,
                                @RequestParam String subjectName,
                                @RequestParam Long classId,
                                @RequestParam(required = false) Long teacherId,
                                RedirectAttributes redirectAttributes) {
        try {
            officeStaffService.createSubject(subjectCode, subjectName, classId, teacherId);
            redirectAttributes.addFlashAttribute("successMessage", "Subject " + subjectName + " (" + subjectCode + ") created successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create Subject: " + e.getMessage());
        }
        return "redirect:/staff/academic-setup";
    }

    @PostMapping("/academic-setup/location")
    public String createCampusLocation(@RequestParam String buildingName,
                                       @RequestParam String floorNumber,
                                       @RequestParam String roomNumber,
                                       @RequestParam FacilityType facilityType,
                                       @RequestParam(required = false) String description,
                                       RedirectAttributes redirectAttributes) {
        try {
            officeStaffService.createCampusLocation(buildingName, floorNumber, roomNumber, facilityType, description);
            redirectAttributes.addFlashAttribute("successMessage", "Campus Location added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add Location: " + e.getMessage());
        }
        return "redirect:/staff/academic-setup";
    }

    @PostMapping("/academic-setup/class-group/{id}/delete")
    public String deleteClassGroup(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            officeStaffService.deleteClassGroup(id);
            redirectAttributes.addFlashAttribute("successMessage", "Class Group deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete class group: " + e.getMessage());
        }
        return "redirect:/staff/academic-setup";
    }

    @PostMapping("/academic-setup/subject/{id}/delete")
    public String deleteSubject(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            officeStaffService.deleteSubject(id);
            redirectAttributes.addFlashAttribute("successMessage", "Subject deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete subject: " + e.getMessage());
        }
        return "redirect:/staff/academic-setup";
    }

    @PostMapping("/academic-setup/location/{id}/delete")
    public String deleteCampusLocation(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            officeStaffService.deleteCampusLocation(id);
            redirectAttributes.addFlashAttribute("successMessage", "Campus Location deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete location: " + e.getMessage());
        }
        return "redirect:/staff/academic-setup";
    }

    @GetMapping("/vault")
    public String vaultPage(Model model) {
        model.addAttribute("students", studentRepository.findAll());
        model.addAttribute("documents", studentDocumentRepository.findAll());
        return "staff/vault";
    }

    @PostMapping("/vault/upload")
    public String uploadVaultDocument(@RequestParam Long studentId,
                                      @RequestParam String documentTitle,
                                      @RequestParam String documentType,
                                      @RequestParam("file") MultipartFile file,
                                      Principal principal,
                                      RedirectAttributes redirectAttributes) {
        try {
            officeStaffService.uploadStudentDocument(studentId, documentTitle, documentType, file, principal.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Document '" + documentTitle + "' uploaded to Vault successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vault upload failed: " + e.getMessage());
        }
        return "redirect:/staff/vault";
    }

    @PostMapping("/vault/document/{id}/delete")
    public String deleteStudentDocument(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            officeStaffService.deleteStudentDocument(id);
            redirectAttributes.addFlashAttribute("successMessage", "Document removed from Vault successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to remove document: " + e.getMessage());
        }
        return "redirect:/staff/vault";
    }

    @GetMapping("/issues")
    public String issuesPage(Model model) {
        List<IssueTicket> tickets = issueTicketRepository.findByTargetRoleOrderByCreatedAtDesc(IssueTargetRole.OFFICE_STAFF);
        model.addAttribute("tickets", tickets);
        model.addAttribute("statuses", IssueStatus.values());
        return "staff/issues";
    }

    @PostMapping("/issues/{id}/resolve")
    public String resolveTicket(@PathVariable Long id,
                                @RequestParam IssueStatus status,
                                @RequestParam String resolutionRemarks,
                                RedirectAttributes redirectAttributes) {
        try {
            officeStaffService.resolveAdministrativeTicket(id, status, resolutionRemarks);
            redirectAttributes.addFlashAttribute("successMessage", "Ticket #" + id + " updated to " + status + "!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to update ticket: " + e.getMessage());
        }
        return "redirect:/staff/issues";
    }
}
