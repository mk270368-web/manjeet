package com.example.data.model

enum class UserRole(val displayName: String) {
    ADMIN("Administrator"),
    TEACHER("Faculty / Teacher"),
    STUDENT("Student")
}

enum class AccountStatus {
    ACTIVE,
    INACTIVE
}

enum class AttendanceStatus(val label: String, val shortCode: String) {
    PRESENT("Present", "P"),
    ABSENT("Absent", "A"),
    LATE("Late", "L"),
    ON_LEAVE("On Leave", "OL")
}

enum class SubmissionStatus(val label: String) {
    PENDING("Not Submitted"),
    SUBMITTED("Submitted"),
    LATE("Submitted Late"),
    EVALUATED("Graded")
}

enum class AnnouncementTarget(val label: String) {
    ALL("All Users"),
    TEACHERS("Faculty Only"),
    STUDENTS("Students Only"),
    CLASS("Specific Class")
}

enum class NotificationType {
    ASSIGNMENT_NEW,
    ASSIGNMENT_DEADLINE,
    ASSIGNMENT_EVALUATED,
    ATTENDANCE_MARKED,
    ATTENDANCE_LOW,
    ANNOUNCEMENT_ADMIN,
    ANNOUNCEMENT_TEACHER,
    SYSTEM
}
