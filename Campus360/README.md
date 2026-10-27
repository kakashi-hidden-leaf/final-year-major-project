# 🎓 Campus 360 - Smart College Enterprise ERP System

[![Java](https://img.shields.io/badge/Java-17%20%7C%2021%20%7C%2025-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.x%20%2F%20MariaDB-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)](https://www.thymeleaf.org/)
[![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)](https://getbootstrap.com/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg?style=for-the-badge)](LICENSE)

**Campus 360** is a centralized, production-grade web-based college management ERP platform built with **Spring Boot 3.3.4**, **MySQL / MariaDB**, **Thymeleaf**, and **Bootstrap 5**. It automates university operations, multi-year teacher course allocations, class roster CSV bulk marks uploads, student attendance tracking, verified document vaults, assignment submissions, student grievance handling with proof uploads, lecture timetables with Google Calendar synchronization, automated prior lecture alerts, and university question paper portal integration.

---

## 🌟 Key Highlights & Major Features

### 1. 👨‍🏫 Faculty / Teacher Portal (`/teacher/**`)
* **Teaching Portfolio & Course Management (`/teacher/subjects`):**
  * View all active subjects, assigned classes (FE to BE across branches), and enrolled batch counts (60 students per class).
  * **"+ Start Teaching New Subject" Modal:** Teachers can pick any of the 24 college classes and either claim an existing curriculum subject or register a new subject / elective with custom code and name.
* **Class-Specific 60-Student Roster CSV Bulk Marks Upload:**
  * Dynamic CSV roster download: Selecting a class/subject automatically downloads a pre-formatted CSV template containing all 60 enrolled students with their roll numbers and names.
  * Uploading the CSV parses scores and publishes grades instantly to that class only.
* **Class Attendance Tracker:**
  * Mark daily lecture attendance (`PRESENT`, `ABSENT`, `LATE`) with live student rosters and duplicate prevention.
* **Coursework & Assignment Desk:**
  * Create assignments with question file attachments and due dates.
  * Review submitted student coursework files and award status with faculty feedback.
* **Academic Grievance Resolution:**
  * Review student attendance discrepancy and medical leave tickets, inspect student proof files, and resolve/reject with faculty remarks.
* **Timetable & Google Calendar Integration:**
  * Weekly lecture scheduling with room mapping.
  * Direct **"Add to Google Calendar"** button per lecture slot and **"Download Full Timetable (.ics)"** with 15-minute prior alarm triggers.
* **Scheduled Announcements:**
  * Broadcast targeted class notices with automatic student push notification dispatch.

---

### 2. 👨‍🎓 Student Portal (`/student/**`)
* **Live Academic Dashboard:**
  * Real-time overall attendance percentage, subject-wise progress bars with **75% threshold warning alerts**, active class announcements, recent exam scores, and unread notifications.
* **My Examination Results:**
  * View subject-wise scores, percentage progress, and letter grades (`O`, `A+`, `A`, etc.). Isolated strictly to the student's enrolled year and branch.
* **My Attendance Tracker:**
  * Subject-wise percentage summaries and date-wise attendance history logs.
* **Assignment Submissions (Google Classroom Workflow):**
  * View questions, download teacher reference files, and submit coursework attachments (PDF, DOCX, ZIP).
* **Online Document Vault:**
  * View and download official admission receipts and verified certificates uploaded by the Office Staff.
* **Student Grievance Reporting with Proof:**
  * Raise tickets to Faculty or Office Staff (Attendance Discrepancy, Medical Leave, Document Correction) with supporting proof file uploads.
* **MU Question Papers Integration:**
  * Direct shortcut portal leading to University question papers repository (`muquestionpapers.com`).

---

### 3. 🏢 Office Staff Portal (`/staff/**`)
* **Administrative Dashboard:**
  * High-level KPI metrics for enrolled students, active faculty, and open administrative issue tickets.
* **Enrollment & Record Management:**
  * Register and edit Students (Roll Number, Full Name, Email, Password, Class Group, Admission Year, Phone) and Faculty (Employee ID, Full Name, Department, Designation).
* **Admission Document Vault:**
  * Upload verified official files (Admission Fee Receipts, Marksheets, Bonafide Certificates) directly to individual student profiles.
* **Administrative Grievance Desk:**
  * Inspect tickets assigned to office staff, review uploaded student proofs, and resolve/reject with detailed administrative remarks.

---

## 🏛️ Comprehensive Engineering College Dataset Included
The database comes pre-seeded with a complete engineering college structure:
* **6 Engineering Branches:** Information Technology (IT), Computer Engineering (CS), Artificial Intelligence & Data Science (AI-DS), Electronics & Telecommunication (EXTC), Mechanical Engineering (MECH), Civil Engineering (CIVIL).
* **24 Class Groups:** 1st Year (FE), 2nd Year (SE), 3rd Year (TE), 4th Year (BE) for every branch.
* **360 Enrolled Students:** 6 full classes with 60 students each, having **100% unique, diverse Indian full names** and unique roll numbers.
* **6 Faculty Members:** Professors mapped across engineering departments with multi-year teaching loads.

---

## 🔑 Quick Demo Login Credentials

| Role | Username | Password | Key Features to Explore |
| :--- | :--- | :--- | :--- |
| **Teacher** | `prof01` | `password123` | **"My Subjects & Classes"**, 60-Student CSV Marks Upload, Assignments, Grievances |
| **Student (1st Yr IT)** | `2026FEIT01` | `password123` | *Kabir Verma* &bull; 1st Year CPPS subject, attendance & results |
| **Student (2nd Yr IT)** | `2026IT01` | `password123` | *Aarav Sharma* &bull; 2nd Year DBMS subject, Unit Test marks & assignments |
| **Office Staff** | `admin01` | `password123` | Student & Faculty record management, document vault uploads |

---

## 🚀 Getting Started & Installation

### Prerequisites
1. **Java JDK 17 or higher** (JDK 17, 21, or 25)
2. **MySQL / MariaDB** (Running locally on port 3306, e.g. via XAMPP)
3. **Apache Maven 3.8+** (Optional if using bundled launcher or jar)

---

### Step 1: Database Setup
1. Start MySQL in XAMPP or MySQL Service.
2. Create database `campus360_db`:
   ```sql
   CREATE DATABASE campus360_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. Import the pre-seeded dump file:
   ```bash
   mysql -u root campus360_db < database/campus360_database.sql
   ```
   *(Or import `database/campus360_database.sql` via phpMyAdmin web UI).*

---

### Step 2: Running the Application

#### Option A: 1-Click Startup (Windows)
Simply double-click:
```bat
START_CAMPUS360.bat
```
*It automatically checks MySQL, boots Spring Boot, launches a public Cloudflare tunnel, and opens your browser directly to the application!*

To stop: double-click `STOP_CAMPUS360.bat`.

---

#### Option B: Running with Maven
```bash
mvn spring-boot:run
```

---

#### Option C: Running Standalone Executable JAR
```bash
java -jar target/campus360-1.0.0.jar
```

Once started, open:
👉 **http://localhost:8080**

---

## 📁 Project Directory Structure
```
campus360_majorproject/
├── database/
│   ├── campus360_database.sql       # Complete MySQL database schema & seeded data
│   └── README.md                    # Database import guide
├── src/
│   ├── main/
│   │   ├── java/com/campus360/
│   │   │   ├── config/              # Security & DataInitializer
│   │   │   ├── controller/          # Teacher, Student, Staff, Auth Controllers
│   │   │   ├── entity/              # JPA Domain Entities
│   │   │   ├── enums/               # Status & Role Enums
│   │   │   ├── repository/          # Spring Data JPA Repositories
│   │   │   └── service/             # Business Logic & Schedulers
│   │   └── resources/
│   │       ├── static/              # CSS (Executive Light Theme), JS, Images
│   │       ├── templates/           # Thymeleaf Templates (Teacher, Student, Staff)
│   │       └── application.properties # App & Database Configuration
├── uploads/                         # Mock coursework & document files
├── START_CAMPUS360.bat              # 1-Click launcher
├── STOP_CAMPUS360.bat               # 1-Click stopper
├── start_campus360.ps1              # Automation engine
├── pom.xml                          # Maven dependencies & build plugins
└── README.md                        # Documentation
```

---

## 🛠️ Technology Stack
* **Backend:** Spring Boot 3.3.4, Spring Security 6 (BCrypt), Spring Data JPA
* **Database:** MySQL 8.x / MariaDB 10.4+ (Hibernate ORM)
* **Frontend:** Thymeleaf template engine, Bootstrap 5.3, Bootstrap Icons
* **Scheduling:** Spring `@EnableScheduling` (15-min prior lecture alerts)
* **Build Tool:** Apache Maven 3.9+
* **Calendar:** iCalendar (RFC 5545) Google Calendar Sync

---

## 📄 License
This project is developed for educational and academic purposes as a Major Project. Free to use and customize under the MIT License.
