package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    @Query("SELECT * FROM student_profile WHERE id = 1 LIMIT 1")
    fun getStudentProfile(): Flow<StudentProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentProfile(profile: StudentProfile)

    @Query("SELECT * FROM syllabus_module ORDER BY weekNumber ASC, id ASC")
    fun getAllSyllabusModules(): Flow<List<SyllabusModule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyllabusModules(modules: List<SyllabusModule>)

    @Query("DELETE FROM syllabus_module")
    suspend fun clearSyllabusModules()

    @Query("SELECT * FROM study_task ORDER BY isCompleted ASC, isUrgent DESC, id ASC")
    fun getStudyTasks(): Flow<List<StudyTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyTask(task: StudyTask)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyTasks(tasks: List<StudyTask>)

    @Update
    suspend fun updateStudyTask(task: StudyTask)

    @Query("DELETE FROM study_task")
    suspend fun clearStudyTasks()

    @Query("SELECT * FROM whatsapp_announcement ORDER BY id DESC")
    fun getWhatsAppAnnouncements(): Flow<List<WhatsAppAnnouncement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWhatsAppAnnouncements(announcements: List<WhatsAppAnnouncement>)

    @Query("DELETE FROM whatsapp_announcement")
    suspend fun clearWhatsAppAnnouncements()

    @Query("SELECT * FROM extracted_task ORDER BY id DESC")
    fun getExtractedTasks(): Flow<List<ExtractedTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExtractedTasks(tasks: List<ExtractedTask>)

    @Update
    suspend fun updateExtractedTask(task: ExtractedTask)

    @Query("DELETE FROM extracted_task")
    suspend fun clearExtractedTasks()

    @Query("SELECT * FROM whatsapp_message ORDER BY id ASC")
    fun getWhatsAppMessages(): Flow<List<WhatsAppMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWhatsAppMessage(message: WhatsAppMessage)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWhatsAppMessages(messages: List<WhatsAppMessage>)

    @Query("DELETE FROM whatsapp_message")
    suspend fun clearWhatsAppMessages()
}
