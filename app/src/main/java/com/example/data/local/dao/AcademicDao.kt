package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ClassEntity
import com.example.data.model.StudyMaterialEntity
import com.example.data.model.SubjectEntity
import com.example.data.model.TimetableSlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademicDao {
    // Classes
    @Query("SELECT * FROM classes ORDER BY semester ASC, name ASC")
    fun getAllClasses(): Flow<List<ClassEntity>>

    @Query("SELECT * FROM classes WHERE classId = :classId LIMIT 1")
    suspend fun getClassById(classId: String): ClassEntity?

    @Query("SELECT COUNT(*) FROM classes")
    suspend fun countTotalClasses(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClass(classEntity: ClassEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClasses(classes: List<ClassEntity>)

    // Subjects
    @Query("SELECT * FROM subjects ORDER BY semester ASC, code ASC")
    fun getAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE subjectId = :subjectId LIMIT 1")
    suspend fun getSubjectById(subjectId: String): SubjectEntity?

    @Query("SELECT * FROM subjects WHERE semester = :semester")
    fun getSubjectsBySemester(semester: Int): Flow<List<SubjectEntity>>

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun countTotalSubjects(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: SubjectEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    // Timetable
    @Query("SELECT * FROM timetable_slots WHERE classId = :classId ORDER BY dayOfWeek ASC, startTime ASC")
    fun getTimetableForClass(classId: String): Flow<List<TimetableSlotEntity>>

    @Query("SELECT * FROM timetable_slots WHERE teacherId = :teacherId ORDER BY dayOfWeek ASC, startTime ASC")
    fun getTimetableForTeacher(teacherId: String): Flow<List<TimetableSlotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableSlot(slot: TimetableSlotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableSlots(slots: List<TimetableSlotEntity>)

    // Study Materials
    @Query("SELECT * FROM study_materials WHERE classId = :classId ORDER BY uploadedAt DESC")
    fun getMaterialsForClass(classId: String): Flow<List<StudyMaterialEntity>>

    @Query("SELECT * FROM study_materials WHERE teacherId = :teacherId ORDER BY uploadedAt DESC")
    fun getMaterialsByTeacher(teacherId: String): Flow<List<StudyMaterialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyMaterial(material: StudyMaterialEntity)
}
