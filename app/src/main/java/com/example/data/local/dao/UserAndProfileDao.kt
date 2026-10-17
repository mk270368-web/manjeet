package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AccountStatus
import com.example.data.model.StudentProfileEntity
import com.example.data.model.TeacherAssignmentEntity
import com.example.data.model.TeacherProfileEntity
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE loginId = :loginId LIMIT 1")
    suspend fun getUserByLoginId(loginId: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId LIMIT 1")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = :role ORDER BY createdAt DESC")
    fun getUsersByRole(role: UserRole): Flow<List<UserEntity>>

    @Query("SELECT COUNT(*) FROM users WHERE role = :role")
    suspend fun countUsersByRole(role: UserRole): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<UserEntity>)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET status = :status WHERE userId = :userId")
    suspend fun updateStatus(userId: String, status: AccountStatus)

    @Query("UPDATE users SET passwordHash = :passwordHash, tempPassword = :tempPassword, mustChangePassword = :mustChange WHERE userId = :userId")
    suspend fun updatePassword(userId: String, passwordHash: String, tempPassword: String?, mustChange: Boolean)
}

@Dao
interface StudentDao {
    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<StudentProfileEntity>>

    @Query("SELECT * FROM students WHERE userId = :userId LIMIT 1")
    suspend fun getStudentByUserId(userId: String): StudentProfileEntity?

    @Query("SELECT * FROM students WHERE studentId = :studentId LIMIT 1")
    suspend fun getStudentById(studentId: String): StudentProfileEntity?

    @Query("SELECT * FROM students WHERE classId = :classId")
    fun getStudentsByClassId(classId: String): Flow<List<StudentProfileEntity>>

    @Query("SELECT * FROM students WHERE classId = :classId")
    suspend fun getStudentsByClassIdDirect(classId: String): List<StudentProfileEntity>

    @Query("SELECT COUNT(*) FROM students WHERE classId = :classId")
    suspend fun countStudentsInClass(classId: String): Int

    @Query("SELECT COUNT(*) FROM students")
    suspend fun countTotalStudents(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<StudentProfileEntity>)

    @Update
    suspend fun updateStudent(student: StudentProfileEntity)

    @Query("DELETE FROM students WHERE studentId = :studentId")
    suspend fun deleteStudent(studentId: String)
}

@Dao
interface TeacherDao {
    @Query("SELECT * FROM teachers")
    fun getAllTeachers(): Flow<List<TeacherProfileEntity>>

    @Query("SELECT * FROM teachers WHERE userId = :userId LIMIT 1")
    suspend fun getTeacherByUserId(userId: String): TeacherProfileEntity?

    @Query("SELECT * FROM teachers WHERE teacherId = :teacherId LIMIT 1")
    suspend fun getTeacherById(teacherId: String): TeacherProfileEntity?

    @Query("SELECT COUNT(*) FROM teachers")
    suspend fun countTotalTeachers(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherProfileEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeachers(teachers: List<TeacherProfileEntity>)

    @Update
    suspend fun updateTeacher(teacher: TeacherProfileEntity)

    // Teacher-Class Assignments
    @Query("SELECT * FROM teacher_assignments WHERE teacherId = :teacherId")
    fun getTeacherAssignments(teacherId: String): Flow<List<TeacherAssignmentEntity>>

    @Query("SELECT * FROM teacher_assignments WHERE teacherId = :teacherId")
    suspend fun getTeacherAssignmentsDirect(teacherId: String): List<TeacherAssignmentEntity>

    @Query("SELECT * FROM teacher_assignments")
    fun getAllTeacherAssignments(): Flow<List<TeacherAssignmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacherAssignment(assignment: TeacherAssignmentEntity)

    @Query("DELETE FROM teacher_assignments WHERE assignmentId = :assignmentId")
    suspend fun deleteTeacherAssignment(assignmentId: String)
}
