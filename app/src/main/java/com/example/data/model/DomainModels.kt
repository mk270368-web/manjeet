package com.example.data.model

data class StudentWithDetails(
    val student: StudentProfileEntity,
    val user: UserEntity,
    val classEntity: ClassEntity?
)

data class TeacherWithDetails(
    val teacher: TeacherProfileEntity,
    val user: UserEntity,
    val assignedSubjectNames: List<String> = emptyList(),
    val assignedClassNames: List<String> = emptyList()
)

data class AttendanceRecordWithStudent(
    val record: AttendanceRecordEntity,
    val student: StudentProfileEntity,
    val user: UserEntity
)

data class AttendanceStudentItem(
    val studentId: String,
    val rollNumber: String,
    val name: String,
    var status: AttendanceStatus = AttendanceStatus.PRESENT,
    var remarks: String = ""
)

data class SubjectAttendanceStat(
    val subjectId: String,
    val subjectName: String,
    val subjectCode: String,
    val totalConducted: Int,
    val attendedCount: Int,
    val missedCount: Int,
    val lateCount: Int,
    val attendancePercentage: Float
)

data class AssignmentWithDetails(
    val assignment: AssignmentEntity,
    val subjectName: String,
    val subjectCode: String,
    val className: String,
    val teacherName: String,
    val submissionCount: Int = 0,
    val evaluatedCount: Int = 0,
    val totalStudentsInClass: Int = 0,
    val currentStudentSubmission: SubmissionEntity? = null
)

data class SubmissionWithStudent(
    val submission: SubmissionEntity,
    val student: StudentProfileEntity,
    val user: UserEntity
)

data class TimetableSlotWithDetails(
    val slot: TimetableSlotEntity,
    val subjectName: String,
    val subjectCode: String,
    val teacherName: String,
    val className: String
)

data class AdminDashboardSummary(
    val totalStudents: Int,
    val totalTeachers: Int,
    val totalClasses: Int,
    val totalSubjects: Int,
    val todayAttendanceSessionsCount: Int,
    val todayPresentPercentage: Float,
    val totalAssignments: Int,
    val pendingEvaluations: Int
)
