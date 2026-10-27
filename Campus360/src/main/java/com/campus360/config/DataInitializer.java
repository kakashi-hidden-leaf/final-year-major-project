package com.campus360.config;

import com.campus360.entity.*;
import com.campus360.enums.*;
import com.campus360.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final OfficeStaffRepository officeStaffRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final ClassGroupRepository classGroupRepository;
    private final CampusLocationRepository campusLocationRepository;
    private final SubjectRepository subjectRepository;
    private final AttendanceRepository attendanceRepository;
    private final AcademicResultRepository academicResultRepository;
    private final StudentDocumentRepository studentDocumentRepository;
    private final AssignmentRepository assignmentRepo;
    private final AssignmentSubmissionRepository assignmentRepository;
    private final IssueTicketRepository issueTicketRepository;
    private final AnnouncementRepository announcementRepository;
    private final TeacherScheduleRepository teacherScheduleRepository;
    private final NotificationRepository notificationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           OfficeStaffRepository officeStaffRepository,
                           TeacherRepository teacherRepository,
                           StudentRepository studentRepository,
                           ClassGroupRepository classGroupRepository,
                           CampusLocationRepository campusLocationRepository,
                           SubjectRepository subjectRepository,
                           AttendanceRepository attendanceRepository,
                           AcademicResultRepository academicResultRepository,
                           StudentDocumentRepository studentDocumentRepository,
                           AssignmentRepository assignmentRepo,
                           AssignmentSubmissionRepository assignmentRepository,
                           IssueTicketRepository issueTicketRepository,
                           AnnouncementRepository announcementRepository,
                           TeacherScheduleRepository teacherScheduleRepository,
                           NotificationRepository notificationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.officeStaffRepository = officeStaffRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.classGroupRepository = classGroupRepository;
        this.campusLocationRepository = campusLocationRepository;
        this.subjectRepository = subjectRepository;
        this.attendanceRepository = attendanceRepository;
        this.academicResultRepository = academicResultRepository;
        this.studentDocumentRepository = studentDocumentRepository;
        this.assignmentRepo = assignmentRepo;
        this.assignmentRepository = assignmentRepository;
        this.issueTicketRepository = issueTicketRepository;
        this.announcementRepository = announcementRepository;
        this.teacherScheduleRepository = teacherScheduleRepository;
        this.notificationRepository = notificationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        new File("./uploads/vault").mkdirs();
        new File("./uploads/assignments").mkdirs();
        new File("./uploads/issues").mkdirs();

        // Check if database is already seeded with full college dataset
        if (classGroupRepository.count() >= 20 && studentRepository.count() >= 300) {
            log.info("Full college database with {} classes and {} students is already seeded. Skipping reseed.",
                    classGroupRepository.count(), studentRepository.count());
            return;
        }

        // Reseed full engineering college dataset with 360 unique student names across all 6 key branches and classes
        log.info("Seeding full engineering college dataset with 6 branches, 4 academic years, 60 unique students per class, and multi-year teacher assignments...");
        seedCompleteCollegeData();
        log.info("College data seeding completed successfully! {} classes and {} students registered.",
                classGroupRepository.count(), studentRepository.count());
    }

    private void seedCompleteCollegeData() throws Exception {
        // Clean out existing records in child-first foreign key order
        notificationRepository.deleteAllInBatch();
        issueTicketRepository.deleteAllInBatch();
        announcementRepository.deleteAllInBatch();
        assignmentRepository.deleteAllInBatch();
        assignmentRepo.deleteAllInBatch();
        studentDocumentRepository.deleteAllInBatch();
        academicResultRepository.deleteAllInBatch();
        attendanceRepository.deleteAllInBatch();
        teacherScheduleRepository.deleteAllInBatch();
        campusLocationRepository.deleteAllInBatch();
        subjectRepository.deleteAllInBatch();
        studentRepository.deleteAllInBatch();
        teacherRepository.deleteAllInBatch();
        officeStaffRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        classGroupRepository.deleteAllInBatch();
        campusLocationRepository.deleteAllInBatch();

        // Pre-compute password hash once to save BCrypt compute time
        final String defaultPasswordHash = passwordEncoder.encode("password123");

        // 1. Seed Campus Locations (Classrooms)
        CampusLocation locRoom101 = campusLocationRepository.save(CampusLocation.builder()
                .buildingName("Main Academic Block A").floorNumber("1st Floor").roomNumber("Room 101")
                .facilityType(FacilityType.CLASSROOM).description("First Year Lecture Hall with 80 seating.").build());

        CampusLocation locRoom201 = campusLocationRepository.save(CampusLocation.builder()
                .buildingName("Main Academic Block A").floorNumber("2nd Floor").roomNumber("Room 201")
                .facilityType(FacilityType.CLASSROOM).description("Second & Third Year Classroom.").build());

        CampusLocation locRoom302 = campusLocationRepository.save(CampusLocation.builder()
                .buildingName("Main Academic Block A").floorNumber("3rd Floor").roomNumber("Room 302")
                .facilityType(FacilityType.CLASSROOM).description("Smart classroom with projector.").build());

        CampusLocation locLab204 = campusLocationRepository.save(CampusLocation.builder()
                .buildingName("IT & Computer Science Wing").floorNumber("2nd Floor").roomNumber("Lab 204")
                .facilityType(FacilityType.LAB).description("High-performance Computing & DBMS Lab.").build());

        // 2. Seed Office Staff (admin01)
        User staffUser = userRepository.save(User.builder()
                .username("admin01").email("admin@campus360.edu")
                .passwordHash(defaultPasswordHash).role(Role.OFFICE_STAFF)
                .isActive(true).createdAt(LocalDateTime.now()).build());

        officeStaffRepository.save(OfficeStaff.builder()
                .user(staffUser).employeeId("EMP-ADM01").fullName("Rajesh Sharma")
                .officeSection("Academic Registrar Office").build());

        // 3. Seed Faculty Members (6 Teachers across Engineering Departments)
        // prof01: Primary Demo Teacher (teaches across 1st Yr, 2nd Yr, 3rd Yr, 4th Yr)
        User teacherUser1 = userRepository.save(User.builder()
                .username("prof01").email("prof01@campus360.edu")
                .passwordHash(defaultPasswordHash).role(Role.TEACHER)
                .isActive(true).createdAt(LocalDateTime.now()).build());

        Teacher profVikram = teacherRepository.save(Teacher.builder()
                .user(teacherUser1).employeeId("EMP-IT01").fullName("Prof. Vikram Deshmukh")
                .department("Information Technology").designation("Senior Professor & HOD").build());

        // prof02: AI & Data Science
        User teacherUser2 = userRepository.save(User.builder()
                .username("prof02").email("prof02@campus360.edu")
                .passwordHash(defaultPasswordHash).role(Role.TEACHER)
                .isActive(true).createdAt(LocalDateTime.now()).build());

        Teacher profSunita = teacherRepository.save(Teacher.builder()
                .user(teacherUser2).employeeId("EMP-AIDS01").fullName("Prof. Sunita Rao")
                .department("Artificial Intelligence & Data Science").designation("Associate Professor").build());

        // prof03: Computer Engineering
        User teacherUser3 = userRepository.save(User.builder()
                .username("prof03").email("prof03@campus360.edu")
                .passwordHash(defaultPasswordHash).role(Role.TEACHER)
                .isActive(true).createdAt(LocalDateTime.now()).build());

        Teacher profRajesh = teacherRepository.save(Teacher.builder()
                .user(teacherUser3).employeeId("EMP-CS01").fullName("Prof. Rajesh Kulkarni")
                .department("Computer Engineering").designation("Associate Professor").build());

        // prof04: EXTC
        User teacherUser4 = userRepository.save(User.builder()
                .username("prof04").email("prof04@campus360.edu")
                .passwordHash(defaultPasswordHash).role(Role.TEACHER)
                .isActive(true).createdAt(LocalDateTime.now()).build());

        Teacher profAnita = teacherRepository.save(Teacher.builder()
                .user(teacherUser4).employeeId("EMP-EXTC01").fullName("Prof. Anita Joshi")
                .department("Electronics & Telecommunication").designation("Assistant Professor").build());

        // prof05: Mechanical
        User teacherUser5 = userRepository.save(User.builder()
                .username("prof05").email("prof05@campus360.edu")
                .passwordHash(defaultPasswordHash).role(Role.TEACHER)
                .isActive(true).createdAt(LocalDateTime.now()).build());

        Teacher profAmit = teacherRepository.save(Teacher.builder()
                .user(teacherUser5).employeeId("EMP-MECH01").fullName("Prof. Amit Saxena")
                .department("Mechanical Engineering").designation("Associate Professor").build());

        // prof06: Civil
        User teacherUser6 = userRepository.save(User.builder()
                .username("prof06").email("prof06@campus360.edu")
                .passwordHash(defaultPasswordHash).role(Role.TEACHER)
                .isActive(true).createdAt(LocalDateTime.now()).build());

        Teacher profMeera = teacherRepository.save(Teacher.builder()
                .user(teacherUser6).employeeId("EMP-CIVIL01").fullName("Prof. Meera Iyer")
                .department("Civil Engineering").designation("Assistant Professor").build());

        // 4. Seed 24 ClassGroups (6 Engineering Branches x 4 Academic Years)
        String[] branches = {"IT", "Computer Engineering", "AI & Data Science", "Electronics & Telecom", "Mechanical Engineering", "Civil Engineering"};
        String[] branchCodes = {"IT", "CS", "AIDS", "EXTC", "MECH", "CIVIL"};
        String[] years = {"1st Year", "2nd Year", "3rd Year", "4th Year"};

        Map<String, ClassGroup> classMap = new HashMap<>();

        for (int b = 0; b < branches.length; b++) {
            for (String year : years) {
                String key = year + "_" + branchCodes[b];
                ClassGroup cg = classGroupRepository.save(ClassGroup.builder()
                        .academicYear(year)
                        .courseName("B.E.")
                        .branch(branches[b])
                        .division("Div A")
                        .build());
                classMap.put(key, cg);
            }
        }

        // Specific handy class group references
        ClassGroup it1stYear = classMap.get("1st Year_IT");
        ClassGroup it2ndYear = classMap.get("2nd Year_IT");
        ClassGroup it3rdYear = classMap.get("3rd Year_IT");
        ClassGroup it4thYear = classMap.get("4th Year_IT");
        ClassGroup cs1stYear = classMap.get("1st Year_CS");
        ClassGroup cs2ndYear = classMap.get("2nd Year_CS");
        ClassGroup cs3rdYear = classMap.get("3rd Year_CS");
        ClassGroup aids2ndYear = classMap.get("2nd Year_AIDS");
        ClassGroup aids3rdYear = classMap.get("3rd Year_AIDS");
        ClassGroup extc2ndYear = classMap.get("2nd Year_EXTC");
        ClassGroup mech1stYear = classMap.get("1st Year_MECH");
        ClassGroup civil2ndYear = classMap.get("2nd Year_CIVIL");

        // 5. Seed Curriculum Subjects
        // Subjects for Prof. Vikram Deshmukh (prof01) across 4 different classes:
        Subject subCpp1stYear = subjectRepository.save(Subject.builder()
                .subjectCode("IT101").subjectName("Computer Programming & Problem Solving")
                .classGroup(it1stYear).assignedTeacher(profVikram).build());

        Subject subDbms2ndYear = subjectRepository.save(Subject.builder()
                .subjectCode("IT201").subjectName("Database Management Systems")
                .classGroup(it2ndYear).assignedTeacher(profVikram).build());

        Subject subSe3rdYear = subjectRepository.save(Subject.builder()
                .subjectCode("CS301").subjectName("Software Engineering & Agile")
                .classGroup(cs3rdYear).assignedTeacher(profVikram).build());

        Subject subCloud4thYear = subjectRepository.save(Subject.builder()
                .subjectCode("IT401").subjectName("Cloud Computing & DevOps Architecture")
                .classGroup(it4thYear).assignedTeacher(profVikram).build());

        // Other faculty subjects:
        Subject subDsa2ndYear = subjectRepository.save(Subject.builder()
                .subjectCode("IT202").subjectName("Data Structures & Algorithms")
                .classGroup(it2ndYear).assignedTeacher(profSunita).build());

        Subject subMlAids = subjectRepository.save(Subject.builder()
                .subjectCode("AIDS301").subjectName("Machine Learning & Deep Neural Networks")
                .classGroup(aids3rdYear).assignedTeacher(profSunita).build());

        Subject subJavaCs1st = subjectRepository.save(Subject.builder()
                .subjectCode("CS101").subjectName("Object Oriented Programming with Java")
                .classGroup(cs1stYear).assignedTeacher(profRajesh).build());

        Subject subOsCs2nd = subjectRepository.save(Subject.builder()
                .subjectCode("CS201").subjectName("Operating Systems & System Design")
                .classGroup(cs2ndYear).assignedTeacher(profRajesh).build());

        Subject subCnIt3rd = subjectRepository.save(Subject.builder()
                .subjectCode("IT301").subjectName("Computer Networks & Network Security")
                .classGroup(it3rdYear).assignedTeacher(profRajesh).build());

        Subject subDldExtc = subjectRepository.save(Subject.builder()
                .subjectCode("EXTC201").subjectName("Digital Logic & Microprocessors")
                .classGroup(extc2ndYear).assignedTeacher(profAnita).build());

        Subject subMech1st = subjectRepository.save(Subject.builder()
                .subjectCode("MECH101").subjectName("Engineering Mechanics")
                .classGroup(mech1stYear).assignedTeacher(profAmit).build());

        Subject subCivil2nd = subjectRepository.save(Subject.builder()
                .subjectCode("CIVIL201").subjectName("Building Materials & Construction")
                .classGroup(civil2ndYear).assignedTeacher(profMeera).build());

        // 6. Seed Students (60 completely unique students per class across 6 classes = 360 unique students total)
        String[] feitNames = {
                "Kabir Verma", "Ananya Deshmukh", "Reyansh Nair", "Ishita Iyer", "Vihaan Kulkarni",
                "Diya Sengupta", "Advik Chatterjee", "Saanvi Pillai", "Atharva Joshi", "Myra Chawla",
                "Vivaan Hegde", "Kiara Menon", "Shaurya Bhatnagar", "Anvi Kapoor", "Ayaan Mukherjee",
                "Pihu Agarwal", "Krishna Nambiar", "Fatima Siddiqui", "Darshil Pandya", "Ira Singhania",
                "Kian Malhotra", "Shanaya Rao", "Ritvik Somani", "Navya Tiwari", "Samarth Bansal",
                "Vamika Mittal", "Shivam Mathur", "Aditi Goyal", "Aryan Trivedi", "Meera Saxena",
                "Hridaan Shukla", "Riddhi Pandey", "Tejas Bhatt", "Siya Khurana", "Yug Upadhyay",
                "Avani Mahajan", "Lakshya Goswami", "Prisha Solanki", "Parv Kashyap", "Trisha Tripathi",
                "Shlok Rastogi", "Jiya Dubashi", "Daksh Rawat", "Alisha Merchant", "Om Dave",
                "Tara Srinivasan", "Neil D'Souza", "Khushi Bhardwaj", "Rudra Chauhan", "Charvi Lal",
                "Ranveer Dogra", "Mahika Kaul", "Tanay Dhar", "Nayantara Raina", "Veer Talwar",
                "Ishani Bedi", "Eshan Bajaj", "Samaira Sethi", "Agastya Grover", "Tanvi Oberoi"
        };

        String[] seitNames = {
                "Aarav Sharma", "Rohan Shinde", "Tanvi Kulkarni", "Aditya Patil", "Sneha Deshmukh",
                "Siddharth Verma", "Pooja Nair", "Nikhil Joshi", "Anushka Mehta", "Kunal Singh",
                "Isha Bhat", "Gaurav Pawar", "Bhavya Shah", "Ayush Iyer", "Aniket Rao",
                "Neha Mishra", "Soham Saxena", "Divya More", "Harsh Kale", "Mehul Mahajan",
                "Mihir Gadgil", "Nisha Tendulkar", "Omkar Vartak", "Parth Phadke", "Pranav Godbole",
                "Prasad Apte", "Pratik Bapat", "Priya Chitnis", "Rahul Kelkar", "Rhea Sahasrabuddhe",
                "Rishabh Sathe", "Ritesh Ranade", "Riya Pendse", "Rohit Paranjpe", "Sahil Karve",
                "Sakshi Modak", "Sameer Agarkar", "Sanket Abhyankar", "Saurabh Kanitkar", "Shivani Limaye",
                "Shreya Marathe", "Tejas Natu", "Utkarsh Oak", "Vaishnavi Patwardhan", "Varun Potnis",
                "Vedant Risbud", "Vikas Sane", "Yash Talwalkar", "Chirag Tilak", "Deepak Velankar",
                "Devansh Wagh", "Karan Date", "Kavya Gokhale", "Kiran Damle", "Manish Jog",
                "Rakesh Khare", "Sumit Lele", "Swapnil Moghe", "Tushar Pethe", "Yogesh Sovani"
        };

        String[] tecsNames = {
                "Arjun Kadam", "Sanya Jagtap", "Dev Bhosale", "Sanika Salunkhe", "Pratik Ghorpade",
                "Mansi Mane", "Sanket Thorat", "Akanksha Gaikwad", "Saurabh Sawant", "Rutuja Nalawade",
                "Tejas Tambe", "Pranali Jadhav", "Utkarsh Surve", "Shruti Shirke", "Vedant Mohite",
                "Pallavi Chavan", "Vikas Dalvi", "Kalyani Shirodkar", "Yash Ghag", "Payal Divekar",
                "Manish Kenkre", "Juhi Mashelkar", "Mihir Pednekar", "Sayali Raikar", "Omkar Borkar",
                "Siddhi Prabhu", "Parth Kamat", "Tejaswini Shenoy", "Prasad Pai", "Mrunmayee Kini",
                "Rahul Mhatre", "Sayee Patil", "Ritesh Thakur", "Ashwini Gharat", "Rohit Tandel",
                "Komal Bhoir", "Sahil Tare", "Harshada Bhagat", "Sameer Gawand", "Prachi Raut",
                "Akash Vaze", "Mayuri Vani", "Bhavesh Chaudhari", "Dipali Sonawane", "Chetan Badgujar",
                "Rohini Borse", "Dinesh Wani", "Punam Mahale", "Girish Deore", "Madhuri Ahire",
                "Hitesh Bhamare", "Sarita Bhalerao", "Jagdish Khairnar", "Rupali Shirsath", "Kailas Pagare",
                "Sonali Gangurde", "Lalit Nikam", "Ujjwala Kapadnis", "Mangesh Hire", "Vandana Pawar"
        };

        String[] beitNames = {
                "Akshay Tendulkar", "Chinmay Gavaskar", "Harshit Dravid", "Ketan Ganguly", "Mandar Kumble",
                "Nitin Srinath", "Pravin Agarkar", "Rajendra Zaheer", "Sandip Nehra", "Tushar Sehwag",
                "Umesh Gambhir", "Yogesh Raina", "Ankita Dhoni", "Charulata Yuvraj", "Deepali Rohit",
                "Gauri Kohli", "Hetal Rahane", "Juhi Ashwin", "Komal Jadeja", "Lata Bumrah",
                "Madhavi Shami", "Nandita Kuldeep", "Poonam Chahal", "Radhika Hardik", "Saroj Pant",
                "Tulsi Axar", "Urmila Siraj", "Vidya Ishant", "Yamini Umesh", "Zoya Gill",
                "Abhay Pujara", "Brijesh Dhawan", "Chandan Rayudu", "Dilip Samson", "Eknath Kishan",
                "Falguni Dube", "Govind Bishnoi", "Hemant Arshdeep", "Inderjeet Washington", "Jayant Prasidh",
                "Kamal Harshit", "Lokendra Nitish", "Mohan Rinku", "Naresh Jitesh", "Omprakash Tilak",
                "Pankaj Mukesh", "Qasim Khaleel", "Ratan Mayank", "Shashi Avesh", "Tarun Varun",
                "Upendra Sai", "Viren Shivam", "Wasim Yashasvi", "Xavier Dhruv", "Yatin Sarfaraz",
                "Zuber Devdutt", "Alok Abhimanyu", "Bipin Rajat", "Chaitanya Shahbaz", "Deven Suyash"
        };

        String[] fecsNames = {
                "Amitav Sen", "Anirban Ghosh", "Debashis Mukherjee", "Indranil Banerjee", "Kallol Bose",
                "Maitreyi Ganguly", "Pallab Dasgupta", "Ritwik Chakraborty", "Sayan Sanyal", "Tanmoy Dutta",
                "Tridib Bhattacharya", "Upal Majumdar", "Bidisha Roy", "Chaitali Roychaudhury", "Debjani Sarkar",
                "Ishani Sikdar", "Madhurima Bhowmik", "Nabanita Guha", "Poulomi Mitra", "Sayani Pal",
                "Subhashis Debnath", "Tirtha Das", "Utsav Biswas", "Anupam Karmakar", "Bikram Saha",
                "Chandan Paul", "Dipankar Pramanik", "Gautam Mondal", "Hiranmay Barman", "Jagat Adhikari",
                "Kalyan Bagchi", "Lokenath Chowdhury", "Manas Rudra", "Nirmal Kundu", "Prabir Nandi",
                "Rajat Rakshit", "Saikat Samanta", "Tapan Halder", "Utpal Shil", "Vivek Basak",
                "Arpita Poddar", "Barnali Malakar", "Chandrima Dey", "Diti Lahiri", "Gargi Goswami",
                "Jayeeta Bhaduri", "Kakali Chanda", "Lopamudra Maitra", "Monalisa Dhar", "Piyali Dan",
                "Ruma Talukdar", "Saswati Khasnobis", "Tapashi Ghatak", "Ujjayini Ghatak", "Moushumi Ghatak",
                "Ruma Sen", "Sharmila Roy", "Bratati Ray", "Swagata Ghoshal", "Paulami Sen"
        };

        String[] seaidsNames = {
                "Arunachalam Raman", "Balasubramanian Swaminathan", "Chandrasekhar Natarajan", "Dhanush Subramanian", "Eshwar Sundaresan",
                "Gopalakrishnan Sridhar", "Hariharan Rangarajan", "Karthikeyan Venkataraman", "Lakshminarayanan Raghavan", "Madhavan Soundararajan",
                "Narayanan Venkatesh", "Padmanabhan Vaidyanathan", "Radhakrishnan Thyagarajan", "Santhanam Ramachandran", "Thirumalai Soundar",
                "Vasanth Vignesh", "Abirami Kalyani", "Bhavani Meenakshi", "Chitra Sivakami", "Devaki Saranya",
                "Elango Annamalai", "Gopinath Chettiar", "Jayaraman Mudaliar", "Krishnamoorthy Pillai", "Mani Thevar",
                "Nagarajan Nadar", "Pandian Konar", "Rajagopal Gounder", "Saravanan Naicker", "Thangavel Muthaliar",
                "Uma Janaki", "Valli Revathi", "Yashodha Malathi", "Anandhi Gomathi", "Brinda Rajalakshmi",
                "Champa Padmavathi", "Damayanti Balamani", "Gowri Kamakshi", "Hemamalini Andal", "Indrani Kasthuri",
                "Jayashree Kanagam", "Karpagam Mangalam", "Lalitha Maragatham", "Mythili Neelaveni", "Nithya Pankajam",
                "Pushpa Pushpavalli", "Rajeshwari Rukmini", "Shakuntala Saraswathi", "Thangam Soundaravalli", "Usha Thangamani",
                "Vasantha Vedavalli", "Vijayalakshmi Visalakshi", "Anusuya Alamelu", "Bagyam Bhageerathi", "Chandra Chellammal",
                "Durgadevi Gandhimathi", "Ezhilarasi Gnanam", "Geetha Kannagi", "Hamsaveni Manimegalai", "Ilavarasi Sengamalam"
        };

        // Class A: 1st Year B.E. IT - Div A (60 students: 2026FEIT01 to 2026FEIT60)
        List<Student> feitStudents = seedClassStudents(it1stYear, "2026FEIT", 60, 2026, feitNames, defaultPasswordHash);

        // Class B: 2nd Year B.E. IT - Div A (60 students: 2026IT01 to 2026IT60, primary demo student: 2026IT01 Aarav Sharma)
        List<Student> seitStudents = seedClassStudents(it2ndYear, "2026IT", 60, 2025, seitNames, defaultPasswordHash);

        // Class C: 3rd Year B.E. CS - Div A (60 students: 2026TECS01 to 2026TECS60)
        List<Student> tecsStudents = seedClassStudents(cs3rdYear, "2026TECS", 60, 2024, tecsNames, defaultPasswordHash);

        // Class D: 4th Year B.E. IT - Div A (60 students: 2026BEIT01 to 2026BEIT60)
        List<Student> beitStudents = seedClassStudents(it4thYear, "2026BEIT", 60, 2023, beitNames, defaultPasswordHash);

        // Class E: 1st Year B.E. CS - Div A (60 students: 2026FECS01 to 2026FECS60)
        List<Student> fecsStudents = seedClassStudents(cs1stYear, "2026FECS", 60, 2026, fecsNames, defaultPasswordHash);

        // Class F: 2nd Year B.E. AI-DS - Div A (60 students: 2026AIDS01 to 2026AIDS60)
        List<Student> seaidsStudents = seedClassStudents(aids2ndYear, "2026AIDS", 60, 2025, seaidsNames, defaultPasswordHash);

        // 7. Seed Sample Attendance, Results, Assignments, Timetable for prof01 across different classes
        LocalDate today = LocalDate.now();
        Student primaryDemoStudent = seitStudents.get(0); // 2026IT01: Aarav Sharma

        // --- 1st Year IT (CPPS IT101) ---
        // Attendance
        for (int i = 0; i < 30; i++) {
            attendanceRepository.save(Attendance.builder()
                    .student(feitStudents.get(i)).subject(subCpp1stYear).attendanceDate(today.minusDays(1))
                    .status(i % 10 == 0 ? AttendanceStatus.ABSENT : AttendanceStatus.PRESENT)
                    .recordedByTeacher(profVikram).recordedAt(LocalDateTime.now().minusDays(1)).build());
        }
        // Result for 1st Year
        academicResultRepository.save(AcademicResult.builder()
                .student(feitStudents.get(0)).subject(subCpp1stYear).examName("Unit Test 1")
                .marksObtained(27.5).maxMarks(30.0).grade("A+").uploadedByTeacher(profVikram).uploadedAt(LocalDateTime.now().minusDays(2)).build());
        academicResultRepository.save(AcademicResult.builder()
                .student(feitStudents.get(1)).subject(subCpp1stYear).examName("Unit Test 1")
                .marksObtained(24.0).maxMarks(30.0).grade("A").uploadedByTeacher(profVikram).uploadedAt(LocalDateTime.now().minusDays(2)).build());

        // Assignment for 1st Year IT
        File f1 = new File("./uploads/assignments/q_c_pointers.pdf");
        if (!f1.exists()) {
            try (FileWriter fw = new FileWriter(f1)) { fw.write("%PDF-1.4 Mock C Programming Assignment"); }
        }
        Assignment a1stYear = assignmentRepo.save(Assignment.builder()
                .title("Assignment 1: C Pointers, Structs & Dynamic Memory Allocation")
                .description("Q1. Implement dynamic integer array using malloc and free.\nQ2. Create a Student struct and demonstrate pointer-to-struct operations.\nQ3. Write memory-leak free matrix multiplication.")
                .subject(subCpp1stYear).teacher(profVikram).dueDate(today.plusDays(7)).questionFilePath("q_c_pointers.pdf").createdAt(LocalDateTime.now().minusDays(2)).build());

        assignmentRepository.save(AssignmentSubmission.builder()
                .assignment(a1stYear).student(feitStudents.get(0)).subject(subCpp1stYear)
                .assignmentTitle(a1stYear.getTitle()).filePath("sub_c_pointers.pdf")
                .status(AssignmentStatus.SUBMITTED).submittedAt(LocalDateTime.now().minusDays(1)).remarks(null).build());

        // --- 2nd Year IT (DBMS IT201) ---
        // Attendance for 2nd Year (giving primary student 85% attendance)
        attendanceRepository.save(Attendance.builder().student(primaryDemoStudent).subject(subDbms2ndYear).attendanceDate(today.minusDays(1)).status(AttendanceStatus.PRESENT).recordedByTeacher(profVikram).recordedAt(LocalDateTime.now().minusDays(1)).build());
        attendanceRepository.save(Attendance.builder().student(primaryDemoStudent).subject(subDbms2ndYear).attendanceDate(today.minusDays(2)).status(AttendanceStatus.PRESENT).recordedByTeacher(profVikram).recordedAt(LocalDateTime.now().minusDays(2)).build());
        attendanceRepository.save(Attendance.builder().student(primaryDemoStudent).subject(subDsa2ndYear).attendanceDate(today.minusDays(1)).status(AttendanceStatus.PRESENT).recordedByTeacher(profSunita).recordedAt(LocalDateTime.now().minusDays(1)).build());

        // Published marks for 2nd Year IT
        academicResultRepository.save(AcademicResult.builder()
                .student(primaryDemoStudent).subject(subDbms2ndYear).examName("Unit Test 1")
                .marksObtained(28.5).maxMarks(30.0).grade("A+").uploadedByTeacher(profVikram).uploadedAt(LocalDateTime.now().minusDays(3)).build());
        academicResultRepository.save(AcademicResult.builder()
                .student(seitStudents.get(1)).subject(subDbms2ndYear).examName("Unit Test 1")
                .marksObtained(29.0).maxMarks(30.0).grade("O").uploadedByTeacher(profVikram).uploadedAt(LocalDateTime.now().minusDays(3)).build());

        // Assignment for 2nd Year IT
        File f2 = new File("./uploads/assignments/q_dbms_3nf.pdf");
        if (!f2.exists()) {
            try (FileWriter fw = new FileWriter(f2)) { fw.write("%PDF-1.4 Mock DBMS 3NF Assignment"); }
        }
        Assignment a2ndYear = assignmentRepo.save(Assignment.builder()
                .title("Assignment 1: Relational Schema & 3NF Normalization")
                .description("Q1. Design an ER diagram for a Hospital Management System.\nQ2. Convert relations into 3rd Normal Form (3NF).\nQ3. State functional dependencies and candidate keys.")
                .subject(subDbms2ndYear).teacher(profVikram).dueDate(today.plusDays(5)).questionFilePath("q_dbms_3nf.pdf").createdAt(LocalDateTime.now().minusDays(3)).build());

        assignmentRepository.save(AssignmentSubmission.builder()
                .assignment(a2ndYear).student(primaryDemoStudent).subject(subDbms2ndYear)
                .assignmentTitle(a2ndYear.getTitle()).filePath("sub_dbms_3nf.pdf")
                .status(AssignmentStatus.REVIEWED).submittedAt(LocalDateTime.now().minusDays(2))
                .remarks("Excellent schema decomposition and normalization proofs. 30/30 awarded.").build());

        // --- 3rd Year CS (SE CS301) ---
        Assignment a3rdYear = assignmentRepo.save(Assignment.builder()
                .title("Assignment 1: Software Requirements Specification (SRS) IEEE 830")
                .description("Prepare an IEEE 830 compliant SRS document for an Online Smart Campus portal.\nSpecify functional use cases, actor definitions, and DFD Level 0 & 1.")
                .subject(subSe3rdYear).teacher(profVikram).dueDate(today.plusDays(10)).createdAt(LocalDateTime.now().minusDays(1)).build());

        assignmentRepository.save(AssignmentSubmission.builder()
                .assignment(a3rdYear).student(tecsStudents.get(0)).subject(subSe3rdYear)
                .assignmentTitle(a3rdYear.getTitle()).filePath("sub_se_srs.pdf")
                .status(AssignmentStatus.SUBMITTED).submittedAt(LocalDateTime.now().minusHours(5)).build());

        academicResultRepository.save(AcademicResult.builder()
                .student(tecsStudents.get(0)).subject(subSe3rdYear).examName("Unit Test 1")
                .marksObtained(29.0).maxMarks(30.0).grade("O").uploadedByTeacher(profVikram).uploadedAt(LocalDateTime.now().minusDays(1)).build());

        // --- 4th Year IT (Cloud Computing IT401) ---
        Assignment a4thYear = assignmentRepo.save(Assignment.builder()
                .title("Assignment 1: Kubernetes Cluster Deployment & Dockerization")
                .description("Containerize a 3-tier Spring Boot + MySQL application and write Kubernetes deployment.yaml and service.yaml specs.")
                .subject(subCloud4thYear).teacher(profVikram).dueDate(today.plusDays(12)).createdAt(LocalDateTime.now().minusDays(1)).build());

        // 8. Timetable slots for prof01 across different classes:
        teacherScheduleRepository.save(TeacherSchedule.builder()
                .teacher(profVikram).classGroup(it1stYear).subject(subCpp1stYear)
                .campusLocation(locRoom101).dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(11, 0)).build());

        teacherScheduleRepository.save(TeacherSchedule.builder()
                .teacher(profVikram).classGroup(it2ndYear).subject(subDbms2ndYear)
                .campusLocation(locRoom302).dayOfWeek(today.getDayOfWeek())
                .startTime(LocalTime.of(11, 15)).endTime(LocalTime.of(12, 15)).build());

        teacherScheduleRepository.save(TeacherSchedule.builder()
                .teacher(profVikram).classGroup(cs3rdYear).subject(subSe3rdYear)
                .campusLocation(locRoom201).dayOfWeek(DayOfWeek.TUESDAY)
                .startTime(LocalTime.of(13, 0)).endTime(LocalTime.of(14, 0)).build());

        teacherScheduleRepository.save(TeacherSchedule.builder()
                .teacher(profVikram).classGroup(it4thYear).subject(subCloud4thYear)
                .campusLocation(locLab204).dayOfWeek(DayOfWeek.WEDNESDAY)
                .startTime(LocalTime.of(14, 0)).endTime(LocalTime.of(15, 0)).build());

        // 9. Sample Grievance Tickets
        issueTicketRepository.save(IssueTicket.builder()
                .student(primaryDemoStudent).assignedTeacher(profVikram).targetRole(IssueTargetRole.TEACHER)
                .issueCategory("Attendance Discrepancy")
                .description("Respected Sir, I was present for the DBMS lecture yesterday from 11:15 to 12:15, but was marked absent due to late entry into the biometric register. Kindly verify and update.")
                .proofFilePath(null).status(IssueStatus.OPEN).createdAt(LocalDateTime.now().minusHours(6)).build());

        issueTicketRepository.save(IssueTicket.builder()
                .student(feitStudents.get(0)).assignedTeacher(profVikram).targetRole(IssueTargetRole.TEACHER)
                .issueCategory("Medical Leave")
                .description("Sir, I was suffering from viral fever and was advised rest by medical practitioner. Attaching fitness certificate.")
                .proofFilePath(null).status(IssueStatus.OPEN).createdAt(LocalDateTime.now().minusHours(3)).build());

        issueTicketRepository.save(IssueTicket.builder()
                .student(primaryDemoStudent).targetRole(IssueTargetRole.OFFICE_STAFF)
                .issueCategory("Document Correction")
                .description("Request to update mother's name spelling on official admission register and Bonafide certificate.")
                .status(IssueStatus.OPEN).createdAt(LocalDateTime.now().minusDays(1)).build());

        // 10. Sample Document Vault files for primary student (2026IT01)
        File fRec = new File("./uploads/vault/receipt_2026IT01.pdf");
        if (!fRec.exists()) { try (FileWriter fw = new FileWriter(fRec)) { fw.write("%PDF-1.4 Mock Admission Receipt"); } }
        studentDocumentRepository.save(StudentDocument.builder()
                .student(primaryDemoStudent).documentTitle("Official Admission Fee Receipt - Sem 3")
                .documentType("Fee Receipt").filePath("receipt_2026IT01.pdf")
                .uploadedAt(LocalDateTime.now().minusDays(15)).build());

        File fBon = new File("./uploads/vault/bonafide_2026IT01.pdf");
        if (!fBon.exists()) { try (FileWriter fw = new FileWriter(fBon)) { fw.write("%PDF-1.4 Mock Bonafide Certificate"); } }
        studentDocumentRepository.save(StudentDocument.builder()
                .student(primaryDemoStudent).documentTitle("Official Bonafide Certificate (Academic Year 2026-27)")
                .documentType("Bonafide Certificate").filePath("bonafide_2026IT01.pdf")
                .uploadedAt(LocalDateTime.now().minusDays(10)).build());

        // 11. Sample College Announcement
        announcementRepository.save(Announcement.builder()
                .title("Commencement of Unit Test 1 for Engineering Students")
                .message("All Engineering students (FE to BE) are hereby informed that Unit Test 1 will commence from next Monday. Timetable is published on the portal.")
                .targetClassGroup(it2ndYear).createdByTeacher(profVikram)
                .publishDate(today.minusDays(2)).expiryDate(today.plusDays(14)).isNotificationSent(false).build());
    }

    private List<Student> seedClassStudents(ClassGroup classGroup, String rollPrefix, int count,
                                            int admissionYear, String[] fullNames,
                                            String defaultPasswordHash) {
        List<Student> students = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            String rollNumber = String.format("%s%02d", rollPrefix, i);
            String fullName = (i - 1 < fullNames.length) ? fullNames[i - 1] : ("Student " + rollNumber);

            User user = User.builder()
                    .username(rollNumber)
                    .email(rollNumber.toLowerCase() + "@campus360.edu")
                    .passwordHash(defaultPasswordHash)
                    .role(Role.STUDENT)
                    .isActive(true)
                    .createdAt(LocalDateTime.now())
                    .build();
            user = userRepository.save(user);

            Student student = Student.builder()
                    .user(user)
                    .rollNumber(rollNumber)
                    .fullName(fullName)
                    .classGroup(classGroup)
                    .admissionYear(String.valueOf(admissionYear))
                    .phone(String.format("+91 98%08d", (i * 137) % 90000000 + 10000000))
                    .build();
            student = studentRepository.save(student);
            students.add(student);
        }
        return students;
    }
}
