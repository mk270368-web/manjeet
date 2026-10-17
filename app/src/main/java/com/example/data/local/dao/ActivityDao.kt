package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.AnnouncementEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    // Announcements
    @Query("SELECT * FROM announcements ORDER BY isPinned DESC, timestamp DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    @Query("SELECT * FROM announcements WHERE targetAudience IN ('ALL', :roleTarget) OR targetClassId = :classId ORDER BY isPinned DESC, timestamp DESC")
    fun getAnnouncementsForAudience(roleTarget: String, classId: String?): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity)

    // Notifications
    @Query("SELECT * FROM notifications WHERE recipientUserId = :userId OR recipientUserId = 'ALL' ORDER BY timestamp DESC")
    fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE (recipientUserId = :userId OR recipientUserId = 'ALL') AND isRead = 0")
    fun getUnreadNotificationCount(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)

    @Query("UPDATE notifications SET isRead = 1 WHERE notificationId = :notificationId")
    suspend fun markAsRead(notificationId: String)

    @Query("UPDATE notifications SET isRead = 1 WHERE recipientUserId = :userId OR recipientUserId = 'ALL'")
    suspend fun markAllAsRead(userId: String)

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity)
}
