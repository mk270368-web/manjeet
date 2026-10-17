package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.AccountStatus
import com.example.data.model.AnnouncementTarget
import com.example.data.model.AttendanceStatus
import com.example.data.model.NotificationType
import com.example.data.model.SubmissionStatus
import com.example.data.model.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(value: UserRole): String = value.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = try {
        UserRole.valueOf(value)
    } catch (_: Exception) {
        UserRole.STUDENT
    }

    @TypeConverter
    fun fromAccountStatus(value: AccountStatus): String = value.name

    @TypeConverter
    fun toAccountStatus(value: String): AccountStatus = try {
        AccountStatus.valueOf(value)
    } catch (_: Exception) {
        AccountStatus.ACTIVE
    }

    @TypeConverter
    fun fromAttendanceStatus(value: AttendanceStatus): String = value.name

    @TypeConverter
    fun toAttendanceStatus(value: String): AttendanceStatus = try {
        AttendanceStatus.valueOf(value)
    } catch (_: Exception) {
        AttendanceStatus.PRESENT
    }

    @TypeConverter
    fun fromSubmissionStatus(value: SubmissionStatus): String = value.name

    @TypeConverter
    fun toSubmissionStatus(value: String): SubmissionStatus = try {
        SubmissionStatus.valueOf(value)
    } catch (_: Exception) {
        SubmissionStatus.PENDING
    }

    @TypeConverter
    fun fromAnnouncementTarget(value: AnnouncementTarget): String = value.name

    @TypeConverter
    fun toAnnouncementTarget(value: String): AnnouncementTarget = try {
        AnnouncementTarget.valueOf(value)
    } catch (_: Exception) {
        AnnouncementTarget.ALL
    }

    @TypeConverter
    fun fromNotificationType(value: NotificationType): String = value.name

    @TypeConverter
    fun toNotificationType(value: String): NotificationType = try {
        NotificationType.valueOf(value)
    } catch (_: Exception) {
        NotificationType.SYSTEM
    }
}
