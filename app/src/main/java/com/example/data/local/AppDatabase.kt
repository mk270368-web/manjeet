package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AcademicDao
import com.example.data.local.dao.ActivityDao
import com.example.data.local.dao.AssignmentDao
import com.example.data.local.dao.AttendanceDao
import com.example.data.local.dao.StudentDao
import com.example.data.local.dao.TeacherDao
import com.example.data.local.dao.UserDao
import com.example.data.model.AccountStatus
import com.example.data.model.AnnouncementEntity
import com.example.data.model.AnnouncementTarget
import com.example.data.model.AssignmentEntity
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceSessionEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.AuditLogEntity
import com.example.data.model.ClassEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.NotificationType
import com.example.data.model.StudentProfileEntity
import com.example.data.model.StudyMaterialEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.SubmissionEntity
import com.example.data.model.SubmissionStatus
import com.example.data.model.TeacherAssignmentEntity
import com.example.data.model.TeacherProfileEntity
import com.example.data.model.TimetableSlotEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

@Database(
    entities = [
        UserEntity::class,
        StudentProfileEntity::class,
        TeacherProfileEntity::class,
        ClassEntity::class,
        SubjectEntity::class,
        TeacherAssignmentEntity::class,
        AttendanceSessionEntity::class,
        AttendanceRecordEntity::class,
        AssignmentEntity::class,
        SubmissionEntity::class,
        AnnouncementEntity::class,
        NotificationEntity::class,
        AuditLogEntity::class,
        TimetableSlotEntity::class,
        StudyMaterialEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun studentDao(): StudentDao
    abstract fun teacherDao(): TeacherDao
    abstract fun academicDao(): AcademicDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun activityDao(): ActivityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vidyut_gyan_kendra.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun hashPassword(password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateInitialData(database)
                }
            }
        }
    }
}

suspend fun populateInitialData(db: AppDatabase) {
    val adminHash = AppDatabase.hashPassword("admin@123")
    val defaultUserHash = AppDatabase.hashPassword("pass123")

    // 1. Initial Admin Account (ADM001)
    val adminUser = UserEntity(
        userId = "user_admin_01",
        loginId = "ADM001",
        passwordHash = adminHash,
        role = UserRole.ADMIN,
        name = "Dr. Arvind Swaminathan",
        email = "admin.vidyut@gk.edu.in",
        phone = "+91 98765 43210",
        status = AccountStatus.ACTIVE,
        tempPassword = null,
        mustChangePassword = false
    )
    db.userDao().insertUser(adminUser)

    // 2. Teachers
    val teacher1User = UserEntity(
        userId = "user_tch_01",
        loginId = "TCH101",
        passwordHash = defaultUserHash,
        role = UserRole.TEACHER,
        name = "Prof. Rajesh Sharma",
        email = "rajesh.sharma@gk.edu.in",
        phone = "+91 98111 22334",
        status = AccountStatus.ACTIVE,
        tempPassword = "pass123",
        mustChangePassword = false
    )
    val teacher2User = UserEntity(
        userId = "user_tch_02",
        loginId = "TCH102",
        passwordHash = defaultUserHash,
        role = UserRole.TEACHER,
        name = "Dr. Sunita Verma",
        email = "sunita.verma@gk.edu.in",
        phone = "+91 98222 33445",
        status = AccountStatus.ACTIVE,
        tempPassword = "pass123",
        mustChangePassword = false
    )
    db.userDao().insertUsers(listOf(teacher1User, teacher2User))

    val teacher1Profile = TeacherProfileEntity(
        teacherId = "tch_01",
        userId = "user_tch_01",
        employeeCode = "EMP-EE-101",
        department = "Electrical Engineering",
        designation = "Associate Professor & HOD",
        qualification = "M.Tech (Power Systems), Ph.D"
    )
    val teacher2Profile = TeacherProfileEntity(
        teacherId = "tch_02",
        userId = "user_tch_02",
        employeeCode = "EMP-EE-102",
        department = "Electrical Engineering",
        designation = "Assistant Professor",
        qualification = "M.Tech (Control & Instrumentation)"
    )
    db.teacherDao().insertTeachers(listOf(teacher1Profile, teacher2Profile))

    // 3. Classes
    val class3A = ClassEntity(
        classId = "cls_ee_3a",
        name = "EEE 3rd Year",
        branch = "Electrical & Electronics Engineering",
        semester = 5,
        section = "A",
        academicSession = "2024-2025"
    )
    val class3B = ClassEntity(
        classId = "cls_ee_3b",
        name = "EEE 3rd Year",
        branch = "Electrical & Electronics Engineering",
        semester = 5,
        section = "B",
        academicSession = "2024-2025"
    )
    val class4A = ClassEntity(
        classId = "cls_ee_4a",
        name = "EEE 4th Year",
        branch = "Electrical Engineering",
        semester = 7,
        section = "A",
        academicSession = "2024-2025"
    )
    db.academicDao().insertClasses(listOf(class3A, class3B, class4A))

    // 4. Subjects
    val sub1 = SubjectEntity("sub_ee501", "Power Systems Analysis", "EE-501", 4, 5, "Electrical & Electronics Engineering")
    val sub2 = SubjectEntity("sub_ee502", "Electrical Machines II", "EE-502", 4, 5, "Electrical & Electronics Engineering")
    val sub3 = SubjectEntity("sub_ee503", "Control Systems Engineering", "EE-503", 3, 5, "Electrical & Electronics Engineering")
    val sub4 = SubjectEntity("sub_ee504", "Microprocessors & Interfacing", "EE-504", 3, 5, "Electrical & Electronics Engineering")
    val sub5 = SubjectEntity("sub_ee505", "Electromagnetic Fields", "EE-505", 3, 5, "Electrical & Electronics Engineering")
    db.academicDao().insertSubjects(listOf(sub1, sub2, sub3, sub4, sub5))

    // 5. Teacher Assignments (Subjects + Classes)
    db.teacherDao().insertTeacherAssignment(
        TeacherAssignmentEntity("ta_01", "tch_01", "cls_ee_3a", "sub_ee501", "2024-2025")
    )
    db.teacherDao().insertTeacherAssignment(
        TeacherAssignmentEntity("ta_02", "tch_01", "cls_ee_3a", "sub_ee502", "2024-2025")
    )
    db.teacherDao().insertTeacherAssignment(
        TeacherAssignmentEntity("ta_03", "tch_02", "cls_ee_3a", "sub_ee503", "2024-2025")
    )

    // 6. Students
    val studentUsers = listOf(
        UserEntity("user_stu_01", "STU202401", defaultUserHash, UserRole.STUDENT, "Aarav Patel", "aarav.p@gk.edu.in", "+91 99000 11111", AccountStatus.ACTIVE, "pass123"),
        UserEntity("user_stu_02", "STU202402", defaultUserHash, UserRole.STUDENT, "Priya Sharma", "priya.s@gk.edu.in", "+91 99000 22222", AccountStatus.ACTIVE, "pass123"),
        UserEntity("user_stu_03", "STU202403", defaultUserHash, UserRole.STUDENT, "Rohan Gupta", "rohan.g@gk.edu.in", "+91 99000 33333", AccountStatus.ACTIVE, "pass123"),
        UserEntity("user_stu_04", "STU202404", defaultUserHash, UserRole.STUDENT, "Ananya Sen", "ananya.s@gk.edu.in", "+91 99000 44444", AccountStatus.ACTIVE, "pass123")
    )
    db.userDao().insertUsers(studentUsers)

    val studentProfiles = listOf(
        StudentProfileEntity("stu_01", "user_stu_01", "EE-2024-001", "cls_ee_3a", "Electrical & Electronics Engineering", 5, "A", "2024-2025", "Mr. Narendra Patel", "+91 99000 11110"),
        StudentProfileEntity("stu_02", "user_stu_02", "EE-2024-002", "cls_ee_3a", "Electrical & Electronics Engineering", 5, "A", "2024-2025", "Mr. Suresh Sharma", "+91 99000 22220"),
        StudentProfileEntity("stu_03", "user_stu_03", "EE-2024-003", "cls_ee_3a", "Electrical & Electronics Engineering", 5, "A", "2024-2025", "Mr. Vijay Gupta", "+91 99000 33330"),
        StudentProfileEntity("stu_04", "user_stu_04", "EE-2024-004", "cls_ee_3a", "Electrical & Electronics Engineering", 5, "A", "2024-2025", "Mrs. Manju Sen", "+91 99000 44440")
    )
    db.studentDao().insertStudents(studentProfiles)

    // 7. Seed Past Attendance Sessions
    val session1 = AttendanceSessionEntity("sess_01", "cls_ee_3a", "sub_ee501", "tch_01", "2026-10-14", "09:00 - 10:00 AM")
    val session2 = AttendanceSessionEntity("sess_02", "cls_ee_3a", "sub_ee502", "tch_01", "2026-10-14", "11:00 - 12:00 PM")
    val session3 = AttendanceSessionEntity("sess_03", "cls_ee_3a", "sub_ee503", "tch_02", "2026-10-15", "10:00 - 11:00 AM")
    val session4 = AttendanceSessionEntity("sess_04", "cls_ee_3a", "sub_ee501", "tch_01", "2026-10-16", "09:00 - 10:00 AM")

    db.attendanceDao().insertSession(session1)
    db.attendanceDao().insertSession(session2)
    db.attendanceDao().insertSession(session3)
    db.attendanceDao().insertSession(session4)

    val recs = listOf(
        // Sess 1 (Power Systems)
        AttendanceRecordEntity("rec_1_1", "sess_01", "stu_01", AttendanceStatus.PRESENT),
        AttendanceRecordEntity("rec_1_2", "sess_01", "stu_02", AttendanceStatus.PRESENT),
        AttendanceRecordEntity("rec_1_3", "sess_01", "stu_03", AttendanceStatus.ABSENT, "Unexcused absence"),
        AttendanceRecordEntity("rec_1_4", "sess_01", "stu_04", AttendanceStatus.PRESENT),

        // Sess 2 (Machines)
        AttendanceRecordEntity("rec_2_1", "sess_02", "stu_01", AttendanceStatus.PRESENT),
        AttendanceRecordEntity("rec_2_2", "sess_02", "stu_02", AttendanceStatus.LATE, "Joined 15m late"),
        AttendanceRecordEntity("rec_2_3", "sess_02", "stu_03", AttendanceStatus.PRESENT),
        AttendanceRecordEntity("rec_2_4", "sess_02", "stu_04", AttendanceStatus.PRESENT),

        // Sess 3 (Control Systems)
        AttendanceRecordEntity("rec_3_1", "sess_03", "stu_01", AttendanceStatus.PRESENT),
        AttendanceRecordEntity("rec_3_2", "sess_03", "stu_02", AttendanceStatus.PRESENT),
        AttendanceRecordEntity("rec_3_3", "sess_03", "stu_03", AttendanceStatus.ON_LEAVE, "Medical cert submitted"),
        AttendanceRecordEntity("rec_3_4", "sess_03", "stu_04", AttendanceStatus.PRESENT),

        // Sess 4 (Today - Power Systems)
        AttendanceRecordEntity("rec_4_1", "sess_04", "stu_01", AttendanceStatus.PRESENT),
        AttendanceRecordEntity("rec_4_2", "sess_04", "stu_02", AttendanceStatus.PRESENT),
        AttendanceRecordEntity("rec_4_3", "sess_04", "stu_03", AttendanceStatus.PRESENT),
        AttendanceRecordEntity("rec_4_4", "sess_04", "stu_04", AttendanceStatus.PRESENT)
    )
    db.attendanceDao().insertRecords(recs)

    // 8. Seed Assignments
    val asg1 = AssignmentEntity(
        assignmentId = "asg_01",
        title = "Gauss-Seidel & Newton-Raphson Power Flow Analysis",
        description = "Formulate Y-bus matrix for the 5-bus test system and solve for bus voltages and active/reactive power line flows using 3 iterations of Newton-Raphson method.",
        subjectId = "sub_ee501",
        classId = "cls_ee_3a",
        teacherId = "tch_01",
        issueDate = "2026-10-10",
        deadline = "2026-10-22",
        maxMarks = 50,
        instructions = "Submit handwritten neat step-by-step derivation in PDF. Include bus classification table and iteration delta vector.",
        attachmentName = "EE501_Assignment_1_ProblemStatement.pdf"
    )
    val asg2 = AssignmentEntity(
        assignmentId = "asg_02",
        title = "Transformer Equivalent Circuit & Efficiency Curve",
        description = "Calculate open-circuit and short-circuit test parameters of a 50 kVA, 2400/240 V transformer and plot regulation vs power factor.",
        subjectId = "sub_ee502",
        classId = "cls_ee_3a",
        teacherId = "tch_01",
        issueDate = "2026-10-12",
        deadline = "2026-10-25",
        maxMarks = 30,
        instructions = "Graph must show lagging, unity, and leading power factor characteristics.",
        attachmentName = "EE502_Lab_Data_Sheet.pdf"
    )
    val asg3 = AssignmentEntity(
        assignmentId = "asg_03",
        title = "Bode Plot & Nyquist Stability Criterion",
        description = "Plot open-loop frequency response for open-loop transfer function G(s)H(s) = 100 / [s(s+2)(s+10)]. Determine gain margin and phase margin.",
        subjectId = "sub_ee503",
        classId = "cls_ee_3a",
        teacherId = "tch_02",
        issueDate = "2026-10-14",
        deadline = "2026-10-28",
        maxMarks = 40,
        instructions = "Plot on semi-log graph paper or attach clean MATLAB / Python plot.",
        attachmentName = "Control_Systems_Assignment3.pdf"
    )
    db.assignmentDao().insertAssignment(asg1)
    db.assignmentDao().insertAssignment(asg2)
    db.assignmentDao().insertAssignment(asg3)

    // Seed student submission
    val subStudent1 = SubmissionEntity(
        submissionId = "subm_01",
        assignmentId = "asg_01",
        studentId = "stu_01",
        fileName = "Aarav_Patel_EE501_Assignment1.pdf",
        comments = "Completed all 3 iterations. Bus 3 voltage converged within 0.002 pu.",
        submittedAt = System.currentTimeMillis() - 86400000L,
        status = SubmissionStatus.EVALUATED,
        marksObtained = 46,
        feedback = "Excellent work on Jacobian matrix inversion! Neat derivation.",
        evaluatedAt = System.currentTimeMillis() - 3600000L
    )
    val subStudent2 = SubmissionEntity(
        submissionId = "subm_02",
        assignmentId = "asg_01",
        studentId = "stu_02",
        fileName = "Priya_Sharma_EE501_Assignment1.pdf",
        comments = "Attached PDF with Y-bus calculations.",
        submittedAt = System.currentTimeMillis() - 43200000L,
        status = SubmissionStatus.SUBMITTED,
        marksObtained = null,
        feedback = null
    )
    db.assignmentDao().insertOrUpdateSubmission(subStudent1)
    db.assignmentDao().insertOrUpdateSubmission(subStudent2)

    // 9. Timetable Slots
    val slots = listOf(
        TimetableSlotEntity("slot_01", "cls_ee_3a", "sub_ee501", "tch_01", "Monday", "09:00 AM", "10:00 AM", "LH-101"),
        TimetableSlotEntity("slot_02", "cls_ee_3a", "sub_ee502", "tch_01", "Monday", "10:00 AM", "11:00 AM", "Machines Lab"),
        TimetableSlotEntity("slot_03", "cls_ee_3a", "sub_ee503", "tch_02", "Tuesday", "09:00 AM", "10:00 AM", "LH-101"),
        TimetableSlotEntity("slot_04", "cls_ee_3a", "sub_ee504", "tch_02", "Wednesday", "11:00 AM", "12:00 PM", "Microprocessor Lab"),
        TimetableSlotEntity("slot_05", "cls_ee_3a", "sub_ee505", "tch_01", "Thursday", "02:00 PM", "03:00 PM", "LH-103")
    )
    db.academicDao().insertTimetableSlots(slots)

    // 10. Study Materials
    val mat1 = StudyMaterialEntity(
        materialId = "mat_01",
        title = "Lecture 12: Symmetric Fault Analysis & Thevenin Impedance",
        description = "Detailed notes on 3-phase symmetrical fault current calculation and circuit breakers rating.",
        subjectId = "sub_ee501",
        classId = "cls_ee_3a",
        teacherId = "tch_01",
        fileName = "EE501_Lec12_Fault_Analysis.pdf",
        fileType = "PDF"
    )
    val mat2 = StudyMaterialEntity(
        materialId = "mat_02",
        title = "State Space Representation & Controllability Matrix",
        description = "Notes covering state variable modeling of RLC circuits and Kalman rank criterion.",
        subjectId = "sub_ee503",
        classId = "cls_ee_3a",
        teacherId = "tch_02",
        fileName = "Control_State_Space_Handout.pdf",
        fileType = "PDF"
    )
    db.academicDao().insertStudyMaterial(mat1)
    db.academicDao().insertStudyMaterial(mat2)

    // 11. Announcements
    val ann1 = AnnouncementEntity(
        announcementId = "ann_01",
        title = "Mid-Semester Electrical Examination Schedule",
        message = "Mid-term examinations for 5th & 7th Semester commence on November 3rd, 2026. Hall tickets will be issued next week.",
        authorId = "user_admin_01",
        authorName = "Dr. Arvind Swaminathan (Admin)",
        authorRole = UserRole.ADMIN,
        targetAudience = AnnouncementTarget.ALL,
        isPinned = true
    )
    val ann2 = AnnouncementEntity(
        announcementId = "ann_02",
        title = "Guest Lecture on Smart Grid & Renewable Integration",
        message = "Distinguished IEEE speaker Dr. K. Ramanathan from Power Grid Corp will address 3rd & 4th year EEE students on Friday at 3:00 PM in Seminar Hall A.",
        authorId = "user_tch_01",
        authorName = "Prof. Rajesh Sharma",
        authorRole = UserRole.TEACHER,
        targetAudience = AnnouncementTarget.STUDENTS,
        targetClassId = "cls_ee_3a",
        isPinned = false
    )
    db.activityDao().insertAnnouncement(ann1)
    db.activityDao().insertAnnouncement(ann2)

    // 12. Notifications
    val notifs = listOf(
        NotificationEntity("notif_01", "user_stu_01", "Assignment Graded: EE-501", "Prof. Rajesh Sharma evaluated your Newton-Raphson assignment (46/50).", NotificationType.ASSIGNMENT_EVALUATED),
        NotificationEntity("notif_02", "user_stu_01", "Upcoming Deadline", "Transformer Equivalent Circuit assignment is due on Oct 25th.", NotificationType.ASSIGNMENT_DEADLINE),
        NotificationEntity("notif_03", "ALL", "Institute Electrical Symposium", "Registration open for Spark 2026 Electrical Engineering Project Expo.", NotificationType.ANNOUNCEMENT_ADMIN),
        NotificationEntity("notif_04", "user_tch_01", "Submissions Pending", "2 students submitted Power Systems assignment ready for review.", NotificationType.ASSIGNMENT_NEW)
    )
    db.activityDao().insertNotifications(notifs)

    // 13. Audit Log
    db.activityDao().insertAuditLog(
        AuditLogEntity(
            logId = "log_01",
            actorUserId = "user_admin_01",
            actorName = "Dr. Arvind Swaminathan",
            actorRole = UserRole.ADMIN,
            action = "INITIAL_SYSTEM_SETUP",
            affectedRecord = "INSTITUTE_CORE",
            details = "Vidyut Gyan Kendra initial database bootstrapped with EEE faculty, student cohorts, and academic curriculum."
        )
    )
}
