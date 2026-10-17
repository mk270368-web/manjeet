# Vidyut Gyan Kendra – Database Schema & Architecture

This document describes the data architecture, entity relationships, and role-based access control (RBAC) rules for **Vidyut Gyan Kendra (Electrical Engineering Educational Management System)**.

---

## 1. Architecture Overview

The app is architected with a repository pattern backed by:
- **Local Persistence Layer:** SQLite / Room Database (`vidyut_gyan_kendra.db`) for immediate on-device reliability, offline access, fast reporting, and instant testability.
- **Cloud Backend Layer:** Compatible with Firebase Cloud Firestore (see `firestore.rules`), Firebase Auth, and Storage.

---

## 2. Entities & Schema Specification

### 2.1 `users`
Stores user credentials, account status, and role assignment.
*Only Admin can insert records. Public self-registration is disabled.*

| Column | Type | Constraints | Description |
|---|---|---|---|
| `userId` | TEXT | PRIMARY KEY | Unique user ID (`user_stu_xxx`, `user_tch_xxx`, `user_admin_xxx`) |
| `loginId` | TEXT | UNIQUE, NOT NULL | Institute-assigned login ID (`ADM001`, `TCH101`, `STU202401`) |
| `passwordHash` | TEXT | NOT NULL | SHA-256 / PBKDF2 hashed password |
| `role` | TEXT | NOT NULL | `ADMIN`, `TEACHER`, or `STUDENT` |
| `name` | TEXT | NOT NULL | Full name of student or faculty |
| `email` | TEXT | NOT NULL | Institutional email address |
| `phone` | TEXT | NOT NULL | Contact telephone number |
| `status` | TEXT | NOT NULL | `ACTIVE` or `INACTIVE` (Deactivated by Admin) |
| `tempPassword` | TEXT | NULLABLE | Initial temporary password issued by admin |
| `mustChangePassword` | INTEGER | NOT NULL | Flag requiring password reset upon initial login |
| `createdAt` | INTEGER | NOT NULL | Unix epoch millisecond timestamp |

### 2.2 `students`
Student academic cohort profile.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `studentId` | TEXT | PRIMARY KEY | Unique student identifier |
| `userId` | TEXT | UNIQUE, FK -> `users.userId` | Link to user credentials |
| `rollNumber` | TEXT | UNIQUE, NOT NULL | College Roll No (e.g. `EE-2024-001`) |
| `classId` | TEXT | FK -> `classes.classId` | Assigned class and section |
| `branch` | TEXT | NOT NULL | Branch (e.g. Electrical & Electronics Engineering) |
| `semester` | INTEGER | NOT NULL | Current semester (e.g. 5) |
| `section` | TEXT | NOT NULL | Section (e.g. A, B) |
| `academicSession`| TEXT | NOT NULL | e.g. "2024-2025" |

### 2.3 `teachers`
Faculty profiles and departments.

| Column | Type | Constraints | Description |
|---|---|---|---|
| `teacherId` | TEXT | PRIMARY KEY | Unique faculty identifier |
| `userId` | TEXT | UNIQUE, FK -> `users.userId` | Link to user credentials |
| `employeeCode` | TEXT | UNIQUE, NOT NULL | Staff code (e.g. `EMP-EE-101`) |
| `department` | TEXT | NOT NULL | e.g. "Electrical Engineering" |
| `designation` | TEXT | NOT NULL | e.g. "Associate Professor & HOD" |
| `qualification` | TEXT | NOT NULL | Academic degrees (e.g. "M.Tech, Ph.D") |

### 2.4 `classes` & `subjects`
- `classes`: `classId`, `name`, `branch`, `semester`, `section`, `academicSession`.
- `subjects`: `subjectId`, `name`, `code` (e.g. `EE-501`), `credits`, `semester`, `branch`.
- `teacher_assignments`: Maps `teacherId` to `classId` and `subjectId`.

### 2.5 `attendance_sessions` & `attendance_records`
- `attendance_sessions`: `sessionId`, `classId`, `subjectId`, `teacherId`, `date`, `sessionSlot`, `academicSession`, `createdAt`.
  *Index prevents duplicate sessions for identical (classId, subjectId, date, sessionSlot).*
- `attendance_records`: `recordId`, `sessionId`, `studentId`, `status` (`PRESENT`, `ABSENT`, `LATE`, `ON_LEAVE`), `remarks`, `markedAt`.

### 2.6 `assignments` & `submissions`
- `assignments`: `assignmentId`, `title`, `description`, `subjectId`, `classId`, `teacherId`, `issueDate`, `deadline`, `maxMarks`, `instructions`, `attachmentName`.
- `submissions`: `submissionId`, `assignmentId`, `studentId`, `fileName`, `fileUri`, `comments`, `submittedAt`, `status` (`SUBMITTED`, `LATE`, `EVALUATED`), `marksObtained`, `feedback`, `evaluatedAt`.

### 2.7 `announcements`, `notifications`, & `audit_logs`
- `announcements`: Broadcast messages from Admin and Faculty with pinned priority.
- `notifications`: Individual user notifications (e.g., assignment graded, upcoming deadline, low attendance warning).
- `audit_logs`: Immutable trail of actions (account creation, status toggles, attendance saves, password resets).

---

## 3. Pre-Seeded Testing Accounts

| Role | Login ID | Password | Name | Assigned Cohort / Subjects |
|---|---|---|---|---|
| **Admin** | `ADM001` | `admin@123` | Dr. Arvind Swaminathan | Institute Master Administrator |
| **Faculty** | `TCH101` | `pass123` | Prof. Rajesh Sharma | EEE 3rd Year (Sec A) • Power Systems (EE-501), Machines II (EE-502) |
| **Faculty** | `TCH102` | `pass123` | Dr. Sunita Verma | EEE 3rd Year (Sec A) • Control Systems (EE-503), Microprocessors (EE-504) |
| **Student** | `STU202401` | `pass123` | Aarav Patel | Roll: `EE-2024-001`, EEE 3rd Year (Sec A), Sem 5 |
| **Student** | `STU202402` | `pass123` | Priya Sharma | Roll: `EE-2024-002`, EEE 3rd Year (Sec A), Sem 5 |
| **Student** | `STU202403` | `pass123` | Rohan Gupta | Roll: `EE-2024-003`, EEE 3rd Year (Sec A), Sem 5 |
| **Student** | `STU202404` | `pass123` | Ananya Sen | Roll: `EE-2024-004`, EEE 3rd Year (Sec A), Sem 5 |
