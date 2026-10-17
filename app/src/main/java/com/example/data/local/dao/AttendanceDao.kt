package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.AttendanceRecordEntity
import com.example.data.model.AttendanceSessionEntity
import com.example.data.model.AttendanceStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance_sessions WHERE classId = :classId AND subjectId = :subjectId AND date = :date AND sessionSlot = :sessionSlot LIMIT 1")
    suspend fun findExistingSession(
        classId: String,
        subjectId: String,
        date: String,
        sessionSlot: String
    ): AttendanceSessionEntity?

    @Query("SELECT * FROM attendance_sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): AttendanceSessionEntity?

    @Query("SELECT * FROM attendance_sessions WHERE teacherId = :teacherId ORDER BY date DESC, createdAt DESC")
    fun getSessionsForTeacher(teacherId: String): Flow<List<AttendanceSessionEntity>>

    @Query("SELECT * FROM attendance_sessions WHERE classId = :classId ORDER BY date DESC, createdAt DESC")
    fun getSessionsForClass(classId: String): Flow<List<AttendanceSessionEntity>>

    @Query("SELECT * FROM attendance_sessions WHERE date = :date")
    suspend fun getSessionsByDate(date: String): List<AttendanceSessionEntity>

    @Query("SELECT COUNT(*) FROM attendance_sessions WHERE date = :date")
    suspend fun countSessionsByDate(date: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AttendanceSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<AttendanceRecordEntity>)

    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId")
    fun getRecordsForSession(sessionId: String): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId")
    suspend fun getRecordsForSessionDirect(sessionId: String): List<AttendanceRecordEntity>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY markedAt DESC")
    fun getRecordsForStudent(studentId: String): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE studentId = :studentId ORDER BY markedAt DESC")
    suspend fun getRecordsForStudentDirect(studentId: String): List<AttendanceRecordEntity>

    @Query("SELECT COUNT(*) FROM attendance_records WHERE studentId = :studentId")
    suspend fun countStudentTotalRecords(studentId: String): Int

    @Query("SELECT COUNT(*) FROM attendance_records WHERE studentId = :studentId AND (status = 'PRESENT' OR status = 'LATE')")
    suspend fun countStudentAttendedRecords(studentId: String): Int

    @Query("SELECT COUNT(*) FROM attendance_records WHERE studentId = :studentId AND status = 'ABSENT'")
    suspend fun countStudentMissedRecords(studentId: String): Int

    @Query("SELECT COUNT(*) FROM attendance_records WHERE sessionId IN (SELECT sessionId FROM attendance_sessions WHERE date = :date)")
    suspend fun countTotalRecordsOnDate(date: String): Int

    @Query("SELECT COUNT(*) FROM attendance_records WHERE status = 'PRESENT' AND sessionId IN (SELECT sessionId FROM attendance_sessions WHERE date = :date)")
    suspend fun countPresentRecordsOnDate(date: String): Int

    @Query("DELETE FROM attendance_records WHERE sessionId = :sessionId")
    suspend fun deleteRecordsForSession(sessionId: String)

    @Transaction
    suspend fun saveOrUpdateAttendance(
        session: AttendanceSessionEntity,
        records: List<AttendanceRecordEntity>
    ) {
        insertSession(session)
        deleteRecordsForSession(session.sessionId)
        insertRecords(records)
    }
}
