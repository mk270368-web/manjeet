package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["loginId"], unique = true)]
)
data class UserEntity(
    @PrimaryKey val userId: String,
    val loginId: String,          // Unique ID assigned by Admin (e.g., ADM001, TCH101, STU202401)
    val passwordHash: String,     // Hashed or salted password
    val role: UserRole,           // ADMIN, TEACHER, STUDENT
    val name: String,
    val email: String,
    val phone: String,
    val status: AccountStatus = AccountStatus.ACTIVE,
    val tempPassword: String? = null, // Temporary initial password assigned by admin
    val mustChangePassword: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "students",
    indices = [
        Index(value = ["userId"], unique = true),
        Index(value = ["rollNumber"], unique = true),
        Index(value = ["classId"])
    ]
)
data class StudentProfileEntity(
    @PrimaryKey val studentId: String,
    val userId: String,
    val rollNumber: String,
    val classId: String,
    val branch: String,       // e.g. "Electrical & Electronics Engineering (EEE)"
    val semester: Int,        // e.g. 5
    val section: String,      // e.g. "A"
    val academicSession: String = "2024-2025",
    val guardianName: String = "",
    val guardianContact: String = ""
)

@Entity(
    tableName = "teachers",
    indices = [Index(value = ["userId"], unique = true)]
)
data class TeacherProfileEntity(
    @PrimaryKey val teacherId: String,
    val userId: String,
    val employeeCode: String,
    val department: String = "Electrical Engineering",
    val designation: String = "Assistant Professor",
    val qualification: String = "M.Tech, Ph.D"
)

@Entity(
    tableName = "classes",
    indices = [Index(value = ["name", "section", "academicSession"], unique = true)]
)
data class ClassEntity(
    @PrimaryKey val classId: String,
    val name: String,             // e.g., "EEE 3rd Year"
    val branch: String,           // e.g., "Electrical Engineering"
    val semester: Int,            // e.g., 5
    val section: String,          // e.g., "A"
    val academicSession: String   // e.g., "2024-2025"
)

@Entity(
    tableName = "subjects",
    indices = [Index(value = ["code"], unique = true)]
)
data class SubjectEntity(
    @PrimaryKey val subjectId: String,
    val name: String,             // e.g., "Power Systems Analysis"
    val code: String,             // e.g., "EE-501"
    val credits: Int = 4,
    val semester: Int = 5,
    val branch: String = "Electrical Engineering"
)

@Entity(
    tableName = "teacher_assignments",
    indices = [Index(value = ["teacherId", "classId", "subjectId"], unique = true)]
)
data class TeacherAssignmentEntity(
    @PrimaryKey val assignmentId: String,
    val teacherId: String,        // Refers to teachers.teacherId
    val classId: String,          // Refers to classes.classId
    val subjectId: String,        // Refers to subjects.subjectId
    val academicSession: String = "2024-2025"
)

@Entity(
    tableName = "attendance_sessions",
    indices = [Index(value = ["classId", "subjectId", "date", "sessionSlot"], unique = true)]
)
data class AttendanceSessionEntity(
    @PrimaryKey val sessionId: String,
    val classId: String,
    val subjectId: String,
    val teacherId: String,
    val date: String,             // Format: YYYY-MM-DD
    val sessionSlot: String,      // e.g., "09:00 - 10:00 AM"
    val academicSession: String = "2024-2025",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "attendance_records",
    indices = [
        Index(value = ["sessionId", "studentId"], unique = true),
        Index(value = ["studentId"]),
        Index(value = ["sessionId"])
    ]
)
data class AttendanceRecordEntity(
    @PrimaryKey val recordId: String,
    val sessionId: String,
    val studentId: String,
    val status: AttendanceStatus, // PRESENT, ABSENT, LATE, ON_LEAVE
    val remarks: String = "",
    val markedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "assignments",
    indices = [Index(value = ["classId"]), Index(value = ["subjectId"])]
)
data class AssignmentEntity(
    @PrimaryKey val assignmentId: String,
    val title: String,
    val description: String,
    val subjectId: String,
    val classId: String,
    val teacherId: String,
    val issueDate: String,        // YYYY-MM-DD
    val deadline: String,         // YYYY-MM-DD
    val maxMarks: Int = 50,
    val instructions: String = "",
    val attachmentName: String? = null,
    val attachmentUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "submissions",
    indices = [Index(value = ["assignmentId", "studentId"], unique = true)]
)
data class SubmissionEntity(
    @PrimaryKey val submissionId: String,
    val assignmentId: String,
    val studentId: String,
    val fileName: String,
    val fileUri: String = "",
    val comments: String = "",
    val submittedAt: Long = System.currentTimeMillis(),
    val status: SubmissionStatus = SubmissionStatus.SUBMITTED,
    val marksObtained: Int? = null,
    val feedback: String? = null,
    val evaluatedAt: Long? = null
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey val announcementId: String,
    val title: String,
    val message: String,
    val authorId: String,
    val authorName: String,
    val authorRole: UserRole,
    val targetAudience: AnnouncementTarget = AnnouncementTarget.ALL,
    val targetClassId: String? = null,
    val isPinned: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notifications",
    indices = [Index(value = ["recipientUserId"]), Index(value = ["isRead"])]
)
data class NotificationEntity(
    @PrimaryKey val notificationId: String,
    val recipientUserId: String,  // userId or "ALL"
    val title: String,
    val message: String,
    val type: NotificationType,
    val isRead: Boolean = false,
    val targetRoute: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val logId: String,
    val actorUserId: String,
    val actorName: String,
    val actorRole: UserRole,
    val action: String,           // e.g., "CREATE_USER", "MARK_ATTENDANCE", "CHANGE_PASSWORD"
    val affectedRecord: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "timetable_slots",
    indices = [Index(value = ["classId", "dayOfWeek", "startTime"])]
)
data class TimetableSlotEntity(
    @PrimaryKey val slotId: String,
    val classId: String,
    val subjectId: String,
    val teacherId: String,
    val dayOfWeek: String,        // Monday, Tuesday, Wednesday, Thursday, Friday, Saturday
    val startTime: String,        // e.g. "09:00 AM"
    val endTime: String,          // e.g. "10:00 AM"
    val roomNo: String            // e.g. "EE-Lab 2", "LH-104"
)

@Entity(tableName = "study_materials")
data class StudyMaterialEntity(
    @PrimaryKey val materialId: String,
    val title: String,
    val description: String,
    val subjectId: String,
    val classId: String,
    val teacherId: String,
    val fileName: String,
    val fileType: String = "PDF", // PDF, DOCX, PPT
    val fileUrl: String = "",
    val uploadedAt: Long = System.currentTimeMillis()
)
