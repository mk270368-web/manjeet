package com.example.ui.navigation

enum class AppScreen {
    SPLASH,
    WELCOME_ROLES,
    LOGIN,
    ADMIN_ROOT,
    TEACHER_ROOT,
    STUDENT_ROOT,
    NOTIFICATIONS,
    ANNOUNCEMENTS,
    PROFILE_PASSWORD
}

enum class AdminTab {
    DASHBOARD,
    USERS,
    ACADEMICS,
    REPORTS,
    AUDIT
}

enum class TeacherTab {
    OVERVIEW,
    ATTENDANCE,
    ASSIGNMENTS,
    MATERIALS,
    REPORTS
}

enum class StudentTab {
    OVERVIEW,
    ATTENDANCE,
    ASSIGNMENTS,
    TIMETABLE,
    FACULTY
}
