# 🗄️ Campus360 Database Schema & Seeding Dump

This directory contains the complete database dump for **Campus360** with full schema, tables, foreign keys, and pre-seeded mock records (24 class groups, 6 engineering branches, 360 unique students, 6 faculty members, marks, assignments, grievance tickets, announcements, and timetables).

---

## 🚀 How to Import into MySQL / MariaDB / XAMPP

### Method 1: Using XAMPP phpMyAdmin (Web UI)
1. Start **Apache** and **MySQL** in your XAMPP Control Panel.
2. Open your browser and go to `http://localhost/phpmyadmin`.
3. Click **New** on the left sidebar to create a database:
   - Database name: `campus360_db`
   - Collation: `utf8mb4_unicode_ci` (or default)
   - Click **Create**.
4. Click on `campus360_db`, go to the **Import** tab at the top.
5. Click **Choose File**, select `database/campus360_database.sql`, and click **Import** (or **Go**) at the bottom.

---

### Method 2: Using MySQL Command Line (Terminal)
Open terminal/PowerShell and run:

```bash
# 1. Create database (if not exists)
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS campus360_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. Import SQL dump
mysql -u root -p campus360_db < database/campus360_database.sql
```
*(If your root user has no password, simply omit `-p`)*

---

## 📊 Seeded Entities Summary:
- **Branches:** IT, Computer Science (CS), AI & Data Science (AI-DS), EXTC, Mechanical, Civil
- **Academic Years:** 1st Year (FE), 2nd Year (SE), 3rd Year (TE), 4th Year (BE)
- **Students:** 360 unique students across 6 classes (60 students per class with individual Indian full names)
- **Faculty:** 6 professors (`prof01` to `prof06`) mapped across departments
- **Demo Passwords:** All pre-seeded accounts have the default password `password123`.
