package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AssignmentEntity
import com.example.data.model.SubmissionEntity
import com.example.data.model.SubmissionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {
    @Query("SELECT * FROM assignments ORDER BY createdAt DESC")
    fun getAllAssignments(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE classId = :classId ORDER BY deadline ASC")
    fun getAssignmentsForClass(classId: String): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE classId = :classId ORDER BY deadline ASC")
    suspend fun getAssignmentsForClassDirect(classId: String): List<AssignmentEntity>

    @Query("SELECT * FROM assignments WHERE teacherId = :teacherId ORDER BY createdAt DESC")
    fun getAssignmentsByTeacher(teacherId: String): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE assignmentId = :assignmentId LIMIT 1")
    suspend fun getAssignmentById(assignmentId: String): AssignmentEntity?

    @Query("SELECT COUNT(*) FROM assignments")
    suspend fun countTotalAssignments(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignment(assignment: AssignmentEntity)

    @Update
    suspend fun updateAssignment(assignment: AssignmentEntity)

    @Query("DELETE FROM assignments WHERE assignmentId = :assignmentId")
    suspend fun deleteAssignment(assignmentId: String)

    // Submissions
    @Query("SELECT * FROM submissions WHERE assignmentId = :assignmentId ORDER BY submittedAt DESC")
    fun getSubmissionsForAssignment(assignmentId: String): Flow<List<SubmissionEntity>>

    @Query("SELECT * FROM submissions WHERE assignmentId = :assignmentId")
    suspend fun getSubmissionsForAssignmentDirect(assignmentId: String): List<SubmissionEntity>

    @Query("SELECT * FROM submissions WHERE assignmentId = :assignmentId AND studentId = :studentId LIMIT 1")
    suspend fun getSubmissionForStudent(assignmentId: String, studentId: String): SubmissionEntity?

    @Query("SELECT * FROM submissions WHERE studentId = :studentId")
    fun getSubmissionsByStudent(studentId: String): Flow<List<SubmissionEntity>>

    @Query("SELECT * FROM submissions WHERE studentId = :studentId")
    suspend fun getSubmissionsByStudentDirect(studentId: String): List<SubmissionEntity>

    @Query("SELECT COUNT(*) FROM submissions WHERE assignmentId = :assignmentId")
    suspend fun countSubmissionsForAssignment(assignmentId: String): Int

    @Query("SELECT COUNT(*) FROM submissions WHERE assignmentId = :assignmentId AND status = 'EVALUATED'")
    suspend fun countEvaluatedForAssignment(assignmentId: String): Int

    @Query("SELECT COUNT(*) FROM submissions WHERE status != 'EVALUATED'")
    suspend fun countPendingEvaluations(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSubmission(submission: SubmissionEntity)

    @Query("UPDATE submissions SET marksObtained = :marks, feedback = :feedback, status = :status, evaluatedAt = :evaluatedAt WHERE submissionId = :submissionId")
    suspend fun evaluateSubmission(
        submissionId: String,
        marks: Int,
        feedback: String,
        status: SubmissionStatus = SubmissionStatus.EVALUATED,
        evaluatedAt: Long = System.currentTimeMillis()
    )
}
