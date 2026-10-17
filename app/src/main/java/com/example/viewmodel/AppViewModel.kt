package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AccountStatus
import com.example.data.model.AdminDashboardSummary
import com.example.data.model.AnnouncementEntity
import com.example.data.model.AnnouncementTarget
import com.example.data.model.AssignmentEntity
import com.example.data.model.AssignmentWithDetails
import com.example.data.model.AttendanceRecordEntity
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
import com.example.data.repository.AppRepository
import com.example.ui.navigation.AdminTab
import com.example.ui.navigation.AppScreen
import com.example.ui.navigation.StudentTab
import com.example.ui.navigation.TeacherTab
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    val repository = AppRepository(db)

    // Navigation state with back stack
    private val _screenStack = MutableStateFlow(listOf(AppScreen.SPLASH))
    val currentScreen: StateFlow<AppScreen> = MutableStateFlow(AppScreen.SPLASH)

    fun navigateTo(screen: AppScreen) {
        val currentList = _screenStack.value
        _screenStack.value = currentList + screen
        (currentScreen as MutableStateFlow).value = screen
    }

    fun navigateBack(): Boolean {
        val currentList = _screenStack.value
        if (currentList.size > 1) {
            val updated = currentList.dropLast(1)
            _screenStack.value = updated
            (currentScreen as MutableStateFlow).value = updated.last()
            return true
        }
        return false
    }

    // Sub-tab states
    val adminTab = MutableStateFlow(AdminTab.DASHBOARD)
    val teacherTab = MutableStateFlow(TeacherTab.OVERVIEW)
    val studentTab = MutableStateFlow(StudentTab.OVERVIEW)

    // Current Session
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _currentStudent = MutableStateFlow<StudentProfileEntity?>(null)
    val currentStudent: StateFlow<StudentProfileEntity?> = _currentStudent.asStateFlow()

    private val _currentTeacher = MutableStateFlow<TeacherProfileEntity?>(null)
    val currentTeacher: StateFlow<TeacherProfileEntity?> = _currentTeacher.asStateFlow()

    // Snackbar / Feedback
    private val _snackBarMessage = MutableSharedFlow<String>()
    val snackBarMessage: SharedFlow<String> = _snackBarMessage.asSharedFlow()

    // Login Form State
    val loginRole = MutableStateFlow(UserRole.ADMIN)
    val loginIdInput = MutableStateFlow("ADM001")
    val passwordInput = MutableStateFlow("admin@123")
    val isLoginLoading = MutableStateFlow(false)
    val loginErrorMessage = MutableStateFlow<String?>(null)

    // Shared data flows
    val allStudents: StateFlow<List<StudentWithDetails>> = repository.getAllStudentsWithDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTeachers: StateFlow<List<TeacherWithDetails>> = repository.getAllTeachersWithDetails()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClasses: StateFlow<List<ClassEntity>> = db.academicDao().getAllClasses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSubjects: StateFlow<List<SubjectEntity>> = db.academicDao().getAllSubjects()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAnnouncements: StateFlow<List<AnnouncementEntity>> = db.activityDao().getAllAnnouncements()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentAuditLogs: StateFlow<List<AuditLogEntity>> = db.activityDao().getRecentAuditLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminDashboardSummary: StateFlow<AdminDashboardSummary?> = repository.getAdminSummaryFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Notifications for current user
    val userNotifications: StateFlow<List<NotificationEntity>> = MutableStateFlow(emptyList())
    val unreadNotifCount = MutableStateFlow(0)

    // Teacher Attendance Marking UI State
    val selectedClassForAttendance = MutableStateFlow<String>("")
    val selectedSubjectForAttendance = MutableStateFlow<String>("")
    val attendanceDateInput = MutableStateFlow(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()))
    val attendanceSlotInput = MutableStateFlow("09:00 - 10:00 AM")
    val attendanceStudentList = MutableStateFlow<List<AttendanceStudentItem>>(emptyList())
    val isAttendanceSaving = MutableStateFlow(false)

    // Student specific stats
    val studentSubjectStats = MutableStateFlow<List<SubjectAttendanceStat>>(emptyList())
    val studentRecentAttendance = MutableStateFlow<List<AttendanceRecordEntity>>(emptyList())
    val studentAssignments = MutableStateFlow<List<AssignmentWithDetails>>(emptyList())
    val studentTimetable = MutableStateFlow<List<TimetableSlotWithDetails>>(emptyList())

    // Teacher assignments & submissions
    val teacherAssignmentsList = MutableStateFlow<List<AssignmentWithDetails>>(emptyList())
    val teacherSubmissionsList = MutableStateFlow<List<SubmissionWithStudent>>(emptyList())

    init {
        // Auto select first class when loaded
        viewModelScope.launch {
            allClasses.collect { classes ->
                if (classes.isNotEmpty() && selectedClassForAttendance.value.isEmpty()) {
                    selectedClassForAttendance.value = classes.first().classId
                    loadStudentsForAttendance(classes.first().classId)
                }
            }
        }
        viewModelScope.launch {
            allSubjects.collect { subjects ->
                if (subjects.isNotEmpty() && selectedSubjectForAttendance.value.isEmpty()) {
                    selectedSubjectForAttendance.value = subjects.first().subjectId
                }
            }
        }
    }

    fun selectRoleForLogin(role: UserRole) {
        loginRole.value = role
        loginErrorMessage.value = null
        when (role) {
            UserRole.ADMIN -> {
                loginIdInput.value = "ADM001"
                passwordInput.value = "admin@123"
            }
            UserRole.TEACHER -> {
                loginIdInput.value = "TCH101"
                passwordInput.value = "pass123"
            }
            UserRole.STUDENT -> {
                loginIdInput.value = "STU202401"
                passwordInput.value = "pass123"
            }
        }
    }

    fun performLogin() {
        viewModelScope.launch {
            isLoginLoading.value = true
            loginErrorMessage.value = null

            val result = repository.authenticate(loginIdInput.value, passwordInput.value)
            result.onSuccess { user ->
                // Enforce role consistency
                if (user.role != loginRole.value) {
                    loginErrorMessage.value = "Role mismatch: User ID belongs to ${user.role.displayName}. Please select correct login portal."
                    isLoginLoading.value = false
                    return@onSuccess
                }

                _currentUser.value = user

                // Load role profiles
                when (user.role) {
                    UserRole.ADMIN -> {
                        _screenStack.value = listOf(AppScreen.ADMIN_ROOT)
                        (currentScreen as MutableStateFlow).value = AppScreen.ADMIN_ROOT
                        adminTab.value = AdminTab.DASHBOARD
                    }
                    UserRole.TEACHER -> {
                        val tch = repository.getTeacherProfileForUser(user.userId)
                        _currentTeacher.value = tch
                        _screenStack.value = listOf(AppScreen.TEACHER_ROOT)
                        (currentScreen as MutableStateFlow).value = AppScreen.TEACHER_ROOT
                        teacherTab.value = TeacherTab.OVERVIEW
                        loadTeacherData(tch)
                    }
                    UserRole.STUDENT -> {
                        val stu = repository.getStudentProfileForUser(user.userId)
                        _currentStudent.value = stu
                        _screenStack.value = listOf(AppScreen.STUDENT_ROOT)
                        (currentScreen as MutableStateFlow).value = AppScreen.STUDENT_ROOT
                        studentTab.value = StudentTab.OVERVIEW
                        loadStudentData(stu)
                    }
                }
                loadNotifications(user.userId)
                _snackBarMessage.emit("Welcome, ${user.name} (${user.role.displayName})")
            }.onFailure { err ->
                loginErrorMessage.value = err.message ?: "Login failed. Please check credentials."
            }
            isLoginLoading.value = false
        }
    }

    fun logout() {
        _currentUser.value = null
        _currentStudent.value = null
        _currentTeacher.value = null
        _screenStack.value = listOf(AppScreen.WELCOME_ROLES)
        (currentScreen as MutableStateFlow).value = AppScreen.WELCOME_ROLES
        loginErrorMessage.value = null
    }

    private fun loadNotifications(userId: String) {
        viewModelScope.launch {
            db.activityDao().getNotificationsForUser(userId).collect { list ->
                (userNotifications as MutableStateFlow).value = list
                unreadNotifCount.value = list.count { !it.isRead }
            }
        }
    }

    fun markNotificationAsRead(notifId: String) {
        viewModelScope.launch {
            db.activityDao().markAsRead(notifId)
        }
    }

    fun markAllNotificationsRead() {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            db.activityDao().markAllAsRead(user.userId)
            _snackBarMessage.emit("All notifications marked as read.")
        }
    }

    fun loadTeacherData(teacher: TeacherProfileEntity?) {
        if (teacher == null) return
        viewModelScope.launch {
            // Load assignments created by teacher with details
            db.assignmentDao().getAssignmentsByTeacher(teacher.teacherId).collect { list ->
                val subjects = db.academicDao().getAllSubjects().stateIn(viewModelScope).value.associateBy { it.subjectId }
                val classes = db.academicDao().getAllClasses().stateIn(viewModelScope).value.associateBy { it.classId }

                val details = list.map { asg ->
                    val subCount = db.assignmentDao().countSubmissionsForAssignment(asg.assignmentId)
                    val evalCount = db.assignmentDao().countEvaluatedForAssignment(asg.assignmentId)
                    val totalClassStudents = db.studentDao().countStudentsInClass(asg.classId)
                    AssignmentWithDetails(
                        assignment = asg,
                        subjectName = subjects[asg.subjectId]?.name ?: asg.subjectId,
                        subjectCode = subjects[asg.subjectId]?.code ?: "",
                        className = classes[asg.classId]?.let { "${it.name} (${it.section})" } ?: asg.classId,
                        teacherName = _currentUser.value?.name ?: "Faculty",
                        submissionCount = subCount,
                        evaluatedCount = evalCount,
                        totalStudentsInClass = totalClassStudents
                    )
                }
                teacherAssignmentsList.value = details
            }
        }
    }

    fun loadSubmissionsForAssignment(assignmentId: String) {
        viewModelScope.launch {
            db.assignmentDao().getSubmissionsForAssignment(assignmentId).collect { submissions ->
                val list = mutableListOf<SubmissionWithStudent>()
                for (subm in submissions) {
                    val st = db.studentDao().getStudentById(subm.studentId)
                    if (st != null) {
                        val u = db.userDao().getUserById(st.userId)
                        if (u != null) {
                            list.add(SubmissionWithStudent(subm, st, u))
                        }
                    }
                }
                teacherSubmissionsList.value = list
            }
        }
    }

    fun loadStudentData(student: StudentProfileEntity?) {
        if (student == null) return
        viewModelScope.launch {
            // Load subject attendance stats
            repository.getStudentSubjectAttendanceStats(student.studentId).collect { stats ->
                studentSubjectStats.value = stats
            }
        }
        viewModelScope.launch {
            // Load student recent attendance records
            db.attendanceDao().getRecordsForStudent(student.studentId).collect { records ->
                studentRecentAttendance.value = records.take(15)
            }
        }
        viewModelScope.launch {
            // Load assignments for class with student submission state
            db.assignmentDao().getAssignmentsForClass(student.classId).collect { assignments ->
                val subjects = db.academicDao().getAllSubjects().stateIn(viewModelScope).value.associateBy { it.subjectId }
                val classes = db.academicDao().getAllClasses().stateIn(viewModelScope).value.associateBy { it.classId }
                val teachers = db.teacherDao().getAllTeachers().stateIn(viewModelScope).value.associateBy { it.teacherId }
                val users = db.userDao().getAllUsers().stateIn(viewModelScope).value.associateBy { it.userId }

                val list = assignments.map { asg ->
                    val mySubm = db.assignmentDao().getSubmissionForStudent(asg.assignmentId, student.studentId)
                    val tchUser = teachers[asg.teacherId]?.userId?.let { users[it] }
                    AssignmentWithDetails(
                        assignment = asg,
                        subjectName = subjects[asg.subjectId]?.name ?: asg.subjectId,
                        subjectCode = subjects[asg.subjectId]?.code ?: "",
                        className = classes[asg.classId]?.let { "${it.name} (${it.section})" } ?: asg.classId,
                        teacherName = tchUser?.name ?: "Faculty",
                        currentStudentSubmission = mySubm
                    )
                }
                studentAssignments.value = list
            }
        }
        viewModelScope.launch {
            // Load student timetable
            db.academicDao().getTimetableForClass(student.classId).collect { slots ->
                val subjects = db.academicDao().getAllSubjects().stateIn(viewModelScope).value.associateBy { it.subjectId }
                val teachers = db.teacherDao().getAllTeachers().stateIn(viewModelScope).value.associateBy { it.teacherId }
                val users = db.userDao().getAllUsers().stateIn(viewModelScope).value.associateBy { it.userId }
                val classes = db.academicDao().getAllClasses().stateIn(viewModelScope).value.associateBy { it.classId }

                val list = slots.map { slot ->
                    val tchUser = teachers[slot.teacherId]?.userId?.let { users[it] }
                    TimetableSlotWithDetails(
                        slot = slot,
                        subjectName = subjects[slot.subjectId]?.name ?: slot.subjectId,
                        subjectCode = subjects[slot.subjectId]?.code ?: "",
                        teacherName = tchUser?.name ?: "Faculty",
                        className = classes[slot.classId]?.name ?: ""
                    )
                }
                studentTimetable.value = list
            }
        }
    }

    // Attendance Marking Methods
    fun loadStudentsForAttendance(classId: String) {
        selectedClassForAttendance.value = classId
        viewModelScope.launch {
            val list = repository.getStudentsForAttendanceMarking(classId)
            attendanceStudentList.value = list
        }
    }

    fun updateStudentAttendanceStatus(studentId: String, newStatus: AttendanceStatus) {
        val current = attendanceStudentList.value.toMutableList()
        val index = current.indexOfFirst { it.studentId == studentId }
        if (index != -1) {
            current[index] = current[index].copy(status = newStatus)
            attendanceStudentList.value = current
        }
    }

    fun markAllAttendancePresent() {
        val current = attendanceStudentList.value.map { it.copy(status = AttendanceStatus.PRESENT) }
        attendanceStudentList.value = current
    }

    fun saveAttendanceSession() {
        val teacher = _currentTeacher.value ?: return
        val classId = selectedClassForAttendance.value
        val subjectId = selectedSubjectForAttendance.value
        val date = attendanceDateInput.value
        val slot = attendanceSlotInput.value
        val students = attendanceStudentList.value

        if (classId.isEmpty() || subjectId.isEmpty() || students.isEmpty()) {
            viewModelScope.launch { _snackBarMessage.emit("Please select class, subject and students.") }
            return
        }

        viewModelScope.launch {
            isAttendanceSaving.value = true
            val res = repository.markAttendance(teacher, classId, subjectId, date, slot, students)
            res.onSuccess {
                _snackBarMessage.emit("Attendance successfully saved for $date ($slot).")
            }.onFailure { err ->
                _snackBarMessage.emit("Error saving attendance: ${err.message}")
            }
            isAttendanceSaving.value = false
        }
    }

    // Assignment & Evaluation Methods
    fun createAssignment(
        title: String,
        description: String,
        subjectId: String,
        classId: String,
        deadline: String,
        maxMarks: Int,
        instructions: String,
        attachmentName: String?
    ) {
        val teacherUser = _currentUser.value ?: return
        val teacherProfile = _currentTeacher.value ?: return
        viewModelScope.launch {
            val res = repository.createAssignment(
                teacherUser, teacherProfile.teacherId, title, description, subjectId, classId, deadline, maxMarks, instructions, attachmentName
            )
            res.onSuccess {
                _snackBarMessage.emit("Assignment published successfully to class.")
                loadTeacherData(teacherProfile)
            }.onFailure { err ->
                _snackBarMessage.emit("Failed to publish assignment: ${err.message}")
            }
        }
    }

    fun evaluateSubmission(submissionId: String, marks: Int, feedback: String) {
        val teacherUser = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.evaluateSubmission(teacherUser, submissionId, marks, feedback)
            res.onSuccess {
                _snackBarMessage.emit("Grade & feedback recorded successfully.")
                loadTeacherData(_currentTeacher.value)
            }.onFailure { err ->
                _snackBarMessage.emit("Failed to evaluate submission: ${err.message}")
            }
        }
    }

    fun submitStudentAssignment(assignmentId: String, fileName: String, comments: String) {
        val student = _currentStudent.value ?: return
        viewModelScope.launch {
            val res = repository.submitStudentAssignment(student, assignmentId, fileName, comments)
            res.onSuccess {
                _snackBarMessage.emit("Assignment submitted successfully!")
                loadStudentData(student)
            }.onFailure { err ->
                _snackBarMessage.emit("Submission failed: ${err.message}")
            }
        }
    }

    // Admin Actions
    fun createStudent(
        name: String,
        email: String,
        phone: String,
        rollNumber: String,
        classId: String,
        branch: String,
        semester: Int,
        section: String,
        tempPass: String
    ) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.createStudentAccount(admin, name, email, phone, rollNumber, classId, branch, semester, section, tempPass)
            res.onSuccess { user ->
                _snackBarMessage.emit("Student account created. Login ID: ${user.loginId}")
            }.onFailure { err ->
                _snackBarMessage.emit("Error: ${err.message}")
            }
        }
    }

    fun createTeacher(
        name: String,
        email: String,
        phone: String,
        employeeCode: String,
        department: String,
        designation: String,
        qualification: String,
        tempPass: String
    ) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.createTeacherAccount(admin, name, email, phone, employeeCode, department, designation, qualification, tempPass)
            res.onSuccess { user ->
                _snackBarMessage.emit("Faculty account created. Login ID: ${user.loginId}")
            }.onFailure { err ->
                _snackBarMessage.emit("Error: ${err.message}")
            }
        }
    }

    fun toggleUserStatus(targetUserId: String) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.toggleUserAccountStatus(admin, targetUserId)
            res.onSuccess { status ->
                _snackBarMessage.emit("Account status changed to $status.")
            }.onFailure { err ->
                _snackBarMessage.emit("Error: ${err.message}")
            }
        }
    }

    fun adminResetPassword(targetUserId: String, newPass: String) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.adminResetUserPassword(admin, targetUserId, newPass)
            res.onSuccess {
                _snackBarMessage.emit("Temporary password set to: $newPass")
            }.onFailure { err ->
                _snackBarMessage.emit("Error: ${err.message}")
            }
        }
    }

    fun importStudentsCsv(csvContent: String, classId: String) {
        val admin = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.importStudentsFromCsv(admin, csvContent, classId)
            res.onSuccess { count ->
                _snackBarMessage.emit("Successfully imported $count student accounts!")
            }.onFailure { err ->
                _snackBarMessage.emit("CSV Import failed: ${err.message}")
            }
        }
    }

    fun publishAnnouncement(
        title: String,
        message: String,
        targetAudience: AnnouncementTarget,
        targetClassId: String? = null,
        isPinned: Boolean = false
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.publishAnnouncement(user, title, message, targetAudience, targetClassId, isPinned)
            res.onSuccess {
                _snackBarMessage.emit("Announcement published successfully.")
            }.onFailure { err ->
                _snackBarMessage.emit("Failed to publish: ${err.message}")
            }
        }
    }

    fun changePassword(oldPass: String, newPass: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val res = repository.changePassword(user.userId, oldPass, newPass)
            res.onSuccess {
                _snackBarMessage.emit("Password changed successfully!")
            }.onFailure { err ->
                _snackBarMessage.emit("Error: ${err.message}")
            }
        }
    }
}
