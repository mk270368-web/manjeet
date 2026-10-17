package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AccountStatus
import com.example.data.model.AdminDashboardSummary
import com.example.data.model.AnnouncementEntity
import com.example.data.model.AnnouncementTarget
import com.example.data.model.AssignmentEntity
import com.example.data.model.AssignmentWithDetails
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceRecordWithStudent
import com.example.data.model.AttendanceSessionEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.AttendanceStudentItem
import com.example.data.model.AuditLogEntity
import com.example.data.model.ClassEntity
import com.example.data.model.NotificationEntity
import com.example.data.model.NotificationType
import com.example.data.model.StudentProfileEntity
import com.example.data.model.StudentWithDetails
import com.example.data.model.StudyMaterialEntity
import com.example.data.model.SubjectAttendanceStat
import com.example.data.model.SubjectEntity
import com.example.data.model.SubmissionEntity
import com.example.data.model.SubmissionStatus
import com.example.data.model.SubmissionWithStudent
import com.example.data.model.TeacherAssignmentEntity
import com.example.data.model.TeacherProfileEntity
import com.example.data.model.TeacherWithDetails
import com.example.data.model.TimetableSlotEntity
import com.example.data.model.TimetableSlotWithDetails
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AppRepository(private val db: AppDatabase) {

    // --- Authentication ---
    suspend fun authenticate(loginId: String, password: String): Result<UserEntity> = withContext(Dispatchers.IO) {
        val cleanLoginId = loginId.trim()
        val user = db.userDao().getUserByLoginId(cleanLoginId)
            ?: return@withContext Result.failure(Exception("Login ID not found. Accounts are created by the Institute Administrator only."))

        if (user.status == AccountStatus.INACTIVE) {
            return@withContext Result.failure(Exception("This account has been deactivated by the Administrator. Please contact institute office."))
        }

        val inputHash = AppDatabase.hashPassword(password.trim())
        if (user.passwordHash != inputHash) {
            return@withContext Result.failure(Exception("Invalid credentials. Please verify your password."))
        }

        // Record audit
        db.activityDao().insertAuditLog(
            AuditLogEntity(
                logId = "log_${UUID.randomUUID()}",
                actorUserId = user.userId,
                actorName = user.name,
                actorRole = user.role,
                action = "USER_LOGIN",
                affectedRecord = "AUTH_SESSION",
                details = "User ${user.loginId} (${user.role.displayName}) logged in successfully."
            )
        )

        Result.success(user)
    }

    suspend fun changePassword(
        userId: String,
        oldPass: String,
        newPass: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val user = db.userDao().getUserById(userId) ?: return@withContext Result.failure(Exception("User not found"))
        if (db.userDao().getUserById(userId)?.passwordHash != AppDatabase.hashPassword(oldPass)) {
            return@withContext Result.failure(Exception("Current password does not match."))
        }
        if (newPass.length < 6) {
            return@withContext Result.failure(Exception("New password must be at least 6 characters long."))
        }

        val newHash = AppDatabase.hashPassword(newPass)
        db.userDao().updatePassword(userId, newHash, tempPassword = null, mustChange = false)

        db.activityDao().insertAuditLog(
            AuditLogEntity(
                logId = "log_${UUID.randomUUID()}",
                actorUserId = userId,
                actorName = user.name,
                actorRole = user.role,
                action = "PASSWORD_CHANGE",
                affectedRecord = user.userId,
                details = "User ${user.loginId} updated their password."
            )
        )

        Result.success(Unit)
    }

    // --- Admin Operations ---
    suspend fun createStudentAccount(
        adminUser: UserEntity,
        name: String,
        email: String,
        phone: String,
        rollNumber: String,
        classId: String,
        branch: String,
        semester: Int,
        section: String,
        tempPassword: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        if (adminUser.role != UserRole.ADMIN) {
            return@withContext Result.failure(Exception("Unauthorized: Admin privileges required."))
        }

        // Auto-generate or derive Login ID
        val count = db.studentDao().countTotalStudents() + 1
        val loginId = "STU${SimpleDateFormat("yyyy", Locale.US).format(Date())}${String.format(Locale.US, "%03d", count)}"
        val userId = "user_stu_${UUID.randomUUID().toString().take(8)}"
        val studentId = "stu_${UUID.randomUUID().toString().take(8)}"

        val passHash = AppDatabase.hashPassword(tempPassword)
        val user = UserEntity(
            userId = userId,
            loginId = loginId,
            passwordHash = passHash,
            role = UserRole.STUDENT,
            name = name.trim(),
            email = email.trim(),
            phone = phone.trim(),
            status = AccountStatus.ACTIVE,
            tempPassword = tempPassword,
            mustChangePassword = true
        )
        val profile = StudentProfileEntity(
            studentId = studentId,
            userId = userId,
            rollNumber = rollNumber.trim(),
            classId = classId,
            branch = branch,
            semester = semester,
            section = section
        )

        db.userDao().insertUser(user)
        db.studentDao().insertStudent(profile)

        // Audit & Welcome notification
        db.activityDao().insertAuditLog(
            AuditLogEntity(
                logId = "log_${UUID.randomUUID()}",
                actorUserId = adminUser.userId,
                actorName = adminUser.name,
                actorRole = adminUser.role,
                action = "CREATE_STUDENT_ACCOUNT",
                affectedRecord = loginId,
                details = "Admin created student account $loginId for $name (Roll: $rollNumber, Class: $classId)."
            )
        )
        db.activityDao().insertNotification(
            NotificationEntity(
                notificationId = "notif_${UUID.randomUUID()}",
                recipientUserId = userId,
                title = "Welcome to Vidyut Gyan Kendra",
                message = "Your official student account $loginId has been generated. Please update your temporary password.",
                type = NotificationType.SYSTEM
            )
        )

        Result.success(user)
    }

    suspend fun createTeacherAccount(
        adminUser: UserEntity,
        name: String,
        email: String,
        phone: String,
        employeeCode: String,
        department: String,
        designation: String,
        qualification: String,
        tempPassword: String
    ): Result<UserEntity> = withContext(Dispatchers.IO) {
        if (adminUser.role != UserRole.ADMIN) {
            return@withContext Result.failure(Exception("Unauthorized: Admin privileges required."))
        }

        val count = db.teacherDao().countTotalTeachers() + 101
        val loginId = "TCH$count"
        val userId = "user_tch_${UUID.randomUUID().toString().take(8)}"
        val teacherId = "tch_${UUID.randomUUID().toString().take(8)}"

        val passHash = AppDatabase.hashPassword(tempPassword)
        val user = UserEntity(
            userId = userId,
            loginId = loginId,
            passwordHash = passHash,
            role = UserRole.TEACHER,
            name = name.trim(),
            email = email.trim(),
            phone = phone.trim(),
            status = AccountStatus.ACTIVE,
            tempPassword = tempPassword,
            mustChangePassword = true
        )
        val profile = TeacherProfileEntity(
            teacherId = teacherId,
            userId = userId,
            employeeCode = employeeCode.trim(),
            department = department.trim(),
            designation = designation.trim(),
            qualification = qualification.trim()
        )

        db.userDao().insertUser(user)
        db.teacherDao().insertTeacher(profile)

        db.activityDao().insertAuditLog(
            AuditLogEntity(
                logId = "log_${UUID.randomUUID()}",
                actorUserId = adminUser.userId,
                actorName = adminUser.name,
                actorRole = adminUser.role,
                action = "CREATE_TEACHER_ACCOUNT",
                affectedRecord = loginId,
                details = "Admin created faculty account $loginId for $name (Code: $employeeCode)."
            )
        )

        Result.success(user)
    }

    suspend fun toggleUserAccountStatus(adminUser: UserEntity, targetUserId: String): Result<AccountStatus> = withContext(Dispatchers.IO) {
        if (adminUser.role != UserRole.ADMIN) return@withContext Result.failure(Exception("Admin only"))
        val user = db.userDao().getUserById(targetUserId) ?: return@withContext Result.failure(Exception("User not found"))
        val newStatus = if (user.status == AccountStatus.ACTIVE) AccountStatus.INACTIVE else AccountStatus.ACTIVE
        db.userDao().updateStatus(targetUserId, newStatus)

        db.activityDao().insertAuditLog(
            AuditLogEntity(
                logId = "log_${UUID.randomUUID()}",
                actorUserId = adminUser.userId,
                actorName = adminUser.name,
                actorRole = adminUser.role,
                action = "TOGGLE_USER_STATUS",
                affectedRecord = user.loginId,
                details = "Admin changed account status of ${user.loginId} to $newStatus."
            )
        )

        Result.success(newStatus)
    }

    suspend fun adminResetUserPassword(
        adminUser: UserEntity,
        targetUserId: String,
        newTempPass: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        if (adminUser.role != UserRole.ADMIN) return@withContext Result.failure(Exception("Admin only"))
        val target = db.userDao().getUserById(targetUserId) ?: return@withContext Result.failure(Exception("User not found"))
        val hash = AppDatabase.hashPassword(newTempPass)
        db.userDao().updatePassword(targetUserId, hash, tempPassword = newTempPass, mustChange = true)

        db.activityDao().insertAuditLog(
            AuditLogEntity(
                logId = "log_${UUID.randomUUID()}",
                actorUserId = adminUser.userId,
                actorName = adminUser.name,
                actorRole = adminUser.role,
                action = "ADMIN_RESET_PASSWORD",
                affectedRecord = target.loginId,
                details = "Admin reset password for ${target.loginId}. Temporary password issued."
            )
        )
        Result.success(Unit)
    }

    suspend fun importStudentsFromCsv(
        adminUser: UserEntity,
        csvContent: String,
        targetClassId: String
    ): Result<Int> = withContext(Dispatchers.IO) {
        if (adminUser.role != UserRole.ADMIN) return@withContext Result.failure(Exception("Admin only"))
        val lines = csvContent.lines().filter { it.isNotBlank() }
        var importedCount = 0

        val targetClass = db.academicDao().getClassById(targetClassId)
            ?: return@withContext Result.failure(Exception("Target class not found"))

        // Header format expected: Name,RollNumber,Email,Phone
        val startIndex = if (lines.first().contains("Name", ignoreCase = true)) 1 else 0
        for (i in startIndex until lines.size) {
            val parts = lines[i].split(",").map { it.trim() }
            if (parts.size >= 2) {
                val name = parts[0]
                val roll = parts[1]
                val email = if (parts.size > 2 && parts[2].isNotBlank()) parts[2] else "${roll.lowercase()}@gk.edu.in"
                val phone = if (parts.size > 3 && parts[3].isNotBlank()) parts[3] else "+91 90000 00000"

                createStudentAccount(
                    adminUser = adminUser,
                    name = name,
                    email = email,
                    phone = phone,
                    rollNumber = roll,
                    classId = targetClassId,
                    branch = targetClass.branch,
                    semester = targetClass.semester,
                    section = targetClass.section,
                    tempPassword = "pass${roll.takeLast(4)}"
                )
                importedCount++
            }
        }
        Result.success(importedCount)
    }

    // --- Admin Dashboard Summary ---
    fun getAdminSummaryFlow(): Flow<AdminDashboardSummary> = flow {
        val studentsCount = db.studentDao().countTotalStudents()
        val teachersCount = db.teacherDao().countTotalTeachers()
        val classesCount = db.academicDao().countTotalClasses()
        val subjectsCount = db.academicDao().countTotalSubjects()
        val assignmentsCount = db.assignmentDao().countTotalAssignments()
        val pendingEval = db.assignmentDao().countPendingEvaluations()

        val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val todaySessionsCount = db.attendanceDao().countSessionsByDate(todayDate)
        val todayTotalRecords = db.attendanceDao().countTotalRecordsOnDate(todayDate)
        val todayPresentRecords = db.attendanceDao().countPresentRecordsOnDate(todayDate)
        val todayPercent = if (todayTotalRecords > 0) (todayPresentRecords.toFloat() / todayTotalRecords) * 100f else 88.5f

        emit(
            AdminDashboardSummary(
                totalStudents = studentsCount,
                totalTeachers = teachersCount,
                totalClasses = classesCount,
                totalSubjects = subjectsCount,
                todayAttendanceSessionsCount = todaySessionsCount,
                todayPresentPercentage = todayPercent,
                totalAssignments = assignmentsCount,
                pendingEvaluations = pendingEval
            )
        )
    }.flowOn(Dispatchers.IO)

    // --- Queries with Relations ---
    fun getAllStudentsWithDetails(): Flow<List<StudentWithDetails>> = combine(
        db.studentDao().getAllStudents(),
        db.userDao().getAllUsers(),
        db.academicDao().getAllClasses()
    ) { students, users, classes ->
        val userMap = users.associateBy { it.userId }
        val classMap = classes.associateBy { it.classId }
        students.mapNotNull { st ->
            val u = userMap[st.userId]
            if (u != null) {
                StudentWithDetails(st, u, classMap[st.classId])
            } else null
        }
    }

    fun getAllTeachersWithDetails(): Flow<List<TeacherWithDetails>> = combine(
        db.teacherDao().getAllTeachers(),
        db.userDao().getAllUsers(),
        db.teacherDao().getAllTeacherAssignments(),
        db.academicDao().getAllSubjects(),
        db.academicDao().getAllClasses()
    ) { teachers, users, assignments, subjects, classes ->
        val userMap = users.associateBy { it.userId }
        val subjectMap = subjects.associateBy { it.subjectId }
        val classMap = classes.associateBy { it.classId }
        val assignMap = assignments.groupBy { it.teacherId }

        teachers.mapNotNull { tch ->
            val u = userMap[tch.userId]
            if (u != null) {
                val assigned = assignMap[tch.teacherId] ?: emptyList()
                TeacherWithDetails(
                    teacher = tch,
                    user = u,
                    assignedSubjectNames = assigned.mapNotNull { subjectMap[it.subjectId]?.name }.distinct(),
                    assignedClassNames = assigned.mapNotNull { classMap[it.classId]?.let { c -> "${c.name} (${c.section})" } }.distinct()
                )
            } else null
        }
    }

    suspend fun getStudentProfileForUser(userId: String): StudentProfileEntity? = withContext(Dispatchers.IO) {
        db.studentDao().getStudentByUserId(userId)
    }

    suspend fun getTeacherProfileForUser(userId: String): TeacherProfileEntity? = withContext(Dispatchers.IO) {
        db.teacherDao().getTeacherByUserId(userId)
    }

    // --- Teacher Attendance Management ---
    suspend fun markAttendance(
        teacher: TeacherProfileEntity,
        classId: String,
        subjectId: String,
        date: String,
        sessionSlot: String,
        studentRecords: List<AttendanceStudentItem>
    ): Result<Unit> = withContext(Dispatchers.IO) {
        // Duplicate check
        val existing = db.attendanceDao().findExistingSession(classId, subjectId, date, sessionSlot)
        val sessionId = existing?.sessionId ?: "sess_${UUID.randomUUID().toString().take(8)}"

        val session = AttendanceSessionEntity(
            sessionId = sessionId,
            classId = classId,
            subjectId = subjectId,
            teacherId = teacher.teacherId,
            date = date,
            sessionSlot = sessionSlot
        )

        val records = studentRecords.map { item ->
            AttendanceRecordEntity(
                recordId = "rec_${UUID.randomUUID().toString().take(8)}",
                sessionId = sessionId,
                studentId = item.studentId,
                status = item.status,
                remarks = item.remarks
            )
        }

        db.attendanceDao().saveOrUpdateAttendance(session, records)

        // Send low attendance alert to students whose attendance is now low
        for (item in studentRecords) {
            val total = db.attendanceDao().countStudentTotalRecords(item.studentId)
            val attended = db.attendanceDao().countStudentAttendedRecords(item.studentId)
            if (total >= 4) {
                val pct = (attended.toFloat() / total) * 100f
                if (pct < 75.0f) {
                    val st = db.studentDao().getStudentById(item.studentId)
                    if (st != null) {
                        db.activityDao().insertNotification(
                            NotificationEntity(
                                notificationId = "notif_${UUID.randomUUID()}",
                                recipientUserId = st.userId,
                                title = "Low Attendance Warning (<75%)",
                                message = "Your current overall attendance is ${String.format(Locale.US, "%.1f", pct)}%. Please attend classes regularly to maintain exam eligibility.",
                                type = NotificationType.ATTENDANCE_LOW
                            )
                        )
                    }
                }
            }
        }

        db.activityDao().insertAuditLog(
            AuditLogEntity(
                logId = "log_${UUID.randomUUID()}",
                actorUserId = teacher.userId,
                actorName = "Teacher",
                actorRole = UserRole.TEACHER,
                action = "MARK_ATTENDANCE",
                affectedRecord = sessionId,
                details = "Marked attendance for class $classId, subject $subjectId on $date ($sessionSlot). Present: ${studentRecords.count { it.status == AttendanceStatus.PRESENT }}/${studentRecords.size}."
            )
        )

        Result.success(Unit)
    }

    suspend fun getStudentsForAttendanceMarking(classId: String): List<AttendanceStudentItem> = withContext(Dispatchers.IO) {
        val students = db.studentDao().getStudentsByClassIdDirect(classId)
        val users = db.userDao().getAllUsers() // We can get direct
        val userMap = mutableMapOf<String, String>()
        for (st in students) {
            val u = db.userDao().getUserById(st.userId)
            if (u != null) userMap[st.studentId] = u.name
        }

        students.map { st ->
            AttendanceStudentItem(
                studentId = st.studentId,
                rollNumber = st.rollNumber,
                name = userMap[st.studentId] ?: "Student ${st.rollNumber}",
                status = AttendanceStatus.PRESENT
            )
        }
    }

    // --- Student Attendance Statistics ---
    fun getStudentSubjectAttendanceStats(studentId: String): Flow<List<SubjectAttendanceStat>> = flow {
        val records = db.attendanceDao().getRecordsForStudentDirect(studentId)
        val sessions = mutableMapOf<String, AttendanceSessionEntity>()
        for (r in records) {
            if (!sessions.containsKey(r.sessionId)) {
                db.attendanceDao().getSessionById(r.sessionId)?.let { sessions[r.sessionId] = it }
            }
        }

        // Group by subjectId
        val subjectRecords = mutableMapOf<String, MutableList<AttendanceRecordEntity>>()
        for (r in records) {
            val sess = sessions[r.sessionId]
            if (sess != null) {
                subjectRecords.getOrPut(sess.subjectId) { mutableListOf() }.add(r)
            }
        }

        val allSubjects = db.academicDao().getAllSubjects() // flow or direct
        val statsList = mutableListOf<SubjectAttendanceStat>()

        for ((subId, recList) in subjectRecords) {
            val sub = db.academicDao().getSubjectById(subId)
            val total = recList.size
            val presentCount = recList.count { it.status == AttendanceStatus.PRESENT }
            val lateCount = recList.count { it.status == AttendanceStatus.LATE }
            val attended = presentCount + lateCount
            val missed = recList.count { it.status == AttendanceStatus.ABSENT }
            val pct = if (total > 0) (attended.toFloat() / total) * 100f else 0f

            statsList.add(
                SubjectAttendanceStat(
                    subjectId = subId,
                    subjectName = sub?.name ?: "Subject $subId",
                    subjectCode = sub?.code ?: subId,
                    totalConducted = total,
                    attendedCount = attended,
                    missedCount = missed,
                    lateCount = lateCount,
                    attendancePercentage = pct
                )
            )
        }

        emit(statsList)
    }.flowOn(Dispatchers.IO)

    // --- Assignment Management ---
    suspend fun createAssignment(
        teacherUser: UserEntity,
        teacherId: String,
        title: String,
        description: String,
        subjectId: String,
        classId: String,
        deadline: String,
        maxMarks: Int,
        instructions: String,
        attachmentName: String?
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val assignmentId = "asg_${UUID.randomUUID().toString().take(8)}"
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val assignment = AssignmentEntity(
            assignmentId = assignmentId,
            title = title.trim(),
            description = description.trim(),
            subjectId = subjectId,
            classId = classId,
            teacherId = teacherId,
            issueDate = today,
            deadline = deadline,
            maxMarks = maxMarks,
            instructions = instructions.trim(),
            attachmentName = attachmentName
        )
        db.assignmentDao().insertAssignment(assignment)

        // Notify students of the class
        val studentsInClass = db.studentDao().getStudentsByClassIdDirect(classId)
        val notifs = studentsInClass.map { st ->
            NotificationEntity(
                notificationId = "notif_${UUID.randomUUID()}",
                recipientUserId = st.userId,
                title = "New Assignment: $title",
                message = "New assignment posted with deadline $deadline. Max Marks: $maxMarks.",
                type = NotificationType.ASSIGNMENT_NEW
            )
        }
        db.activityDao().insertNotifications(notifs)

        db.activityDao().insertAuditLog(
            AuditLogEntity(
                logId = "log_${UUID.randomUUID()}",
                actorUserId = teacherUser.userId,
                actorName = teacherUser.name,
                actorRole = UserRole.TEACHER,
                action = "CREATE_ASSIGNMENT",
                affectedRecord = assignmentId,
                details = "Published assignment '$title' for class $classId (Deadline: $deadline)."
            )
        )

        Result.success(Unit)
    }

    suspend fun submitStudentAssignment(
        student: StudentProfileEntity,
        assignmentId: String,
        fileName: String,
        comments: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val asg = db.assignmentDao().getAssignmentById(assignmentId)
            ?: return@withContext Result.failure(Exception("Assignment not found"))

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val isLate = today > asg.deadline
        val submissionId = "subm_${student.studentId}_${assignmentId.takeLast(6)}"

        val submission = SubmissionEntity(
            submissionId = submissionId,
            assignmentId = assignmentId,
            studentId = student.studentId,
            fileName = fileName,
            fileUri = "content://attachments/$fileName",
            comments = comments,
            submittedAt = System.currentTimeMillis(),
            status = if (isLate) SubmissionStatus.LATE else SubmissionStatus.SUBMITTED
        )
        db.assignmentDao().insertOrUpdateSubmission(submission)

        // Notify teacher
        val teacherProfile = db.teacherDao().getTeacherById(asg.teacherId)
        if (teacherProfile != null) {
            db.activityDao().insertNotification(
                NotificationEntity(
                    notificationId = "notif_${UUID.randomUUID()}",
                    recipientUserId = teacherProfile.userId,
                    title = "New Submission: ${asg.title}",
                    message = "Student (Roll: ${student.rollNumber}) submitted assignment file: $fileName.",
                    type = NotificationType.ASSIGNMENT_NEW
                )
            )
        }

        Result.success(Unit)
    }

    suspend fun evaluateSubmission(
        teacherUser: UserEntity,
        submissionId: String,
        marks: Int,
        feedback: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        db.assignmentDao().evaluateSubmission(submissionId, marks, feedback)

        // Record audit
        db.activityDao().insertAuditLog(
            AuditLogEntity(
                logId = "log_${UUID.randomUUID()}",
                actorUserId = teacherUser.userId,
                actorName = teacherUser.name,
                actorRole = UserRole.TEACHER,
                action = "EVALUATE_SUBMISSION",
                affectedRecord = submissionId,
                details = "Graded submission $submissionId: $marks marks. Feedback provided."
            )
        )
        Result.success(Unit)
    }

    // --- Announcements & Notifications ---
    suspend fun publishAnnouncement(
        authorUser: UserEntity,
        title: String,
        message: String,
        targetAudience: AnnouncementTarget,
        targetClassId: String? = null,
        isPinned: Boolean = false
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val announcement = AnnouncementEntity(
            announcementId = "ann_${UUID.randomUUID().toString().take(8)}",
            title = title.trim(),
            message = message.trim(),
            authorId = authorUser.userId,
            authorName = "${authorUser.name} (${authorUser.role.displayName})",
            authorRole = authorUser.role,
            targetAudience = targetAudience,
            targetClassId = targetClassId,
            isPinned = isPinned
        )
        db.activityDao().insertAnnouncement(announcement)

        // Send corresponding notification
        db.activityDao().insertNotification(
            NotificationEntity(
                notificationId = "notif_${UUID.randomUUID()}",
                recipientUserId = "ALL",
                title = "Notice: $title",
                message = message.take(120),
                type = if (authorUser.role == UserRole.ADMIN) NotificationType.ANNOUNCEMENT_ADMIN else NotificationType.ANNOUNCEMENT_TEACHER
            )
        )

        Result.success(Unit)
    }

    // --- Export Reports ---
    suspend fun generateAttendanceReportCsv(classId: String): String = withContext(Dispatchers.IO) {
        val classObj = db.academicDao().getClassById(classId)
        val students = db.studentDao().getStudentsByClassIdDirect(classId)
        val userMap = mutableMapOf<String, UserEntity>()
        for (s in students) {
            db.userDao().getUserById(s.userId)?.let { userMap[s.studentId] = it }
        }

        val sb = StringBuilder()
        sb.append("Vidyut Gyan Kendra - Attendance Report\n")
        sb.append("Class: ${classObj?.name ?: classId}, Section: ${classObj?.section ?: ""}, Session: ${classObj?.academicSession ?: ""}\n")
        sb.append("Generated On: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())}\n\n")
        sb.append("Roll Number,Student Name,Login ID,Total Classes,Attended,Missed,Attendance %\n")

        for (s in students) {
            val total = db.attendanceDao().countStudentTotalRecords(s.studentId)
            val attended = db.attendanceDao().countStudentAttendedRecords(s.studentId)
            val missed = db.attendanceDao().countStudentMissedRecords(s.studentId)
            val pct = if (total > 0) (attended.toFloat() / total) * 100f else 0f
            val u = userMap[s.studentId]
            sb.append("${s.rollNumber},\"${u?.name ?: ""}\",${u?.loginId ?: ""},$total,$attended,$missed,${String.format(Locale.US, "%.1f%%", pct)}\n")
        }
        sb.toString()
    }

    // Expose DB DAOs for direct flows
    val userDao = db.userDao()
    val studentDao = db.studentDao()
    val teacherDao = db.teacherDao()
    val academicDao = db.academicDao()
    val attendanceDao = db.attendanceDao()
    val assignmentDao = db.assignmentDao()
    val activityDao = db.activityDao()
}
