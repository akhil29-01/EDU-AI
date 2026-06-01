package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "student_profile")
data class StudentProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Rahul",
    val branch: String = "CSE",
    val semester: Int = 6,
    val streakDays: Int = 12,
    val overallProgress: Int = 70,
    val currentFocusSubject: String = "Engineering Mechanics",
    val syllabusText: String = "",
    val whatsappText: String = ""
)

@Entity(tableName = "syllabus_module")
data class SyllabusModule(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val moduleName: String,
    val description: String,
    val difficulty: String, // e.g. "High", "Medium", "Low"
    val weightage: String, // e.g. "High Weightage", "Core Unit"
    val prerequisites: String, // e.g. "Calculus I"
    val weekNumber: Int = 1
)

@Entity(tableName = "study_task")
data class StudyTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subject: String, // e.g. "Engineering Math"
    val topic: String, // e.g. "Solve Fourier Transform Problems"
    val explanation: String,
    val estimatedMinutes: Int,
    val categoryTag: String, // e.g. "#ENGINEERING-MATH"
    val isUrgent: Boolean = false,
    val isCompleted: Boolean = false,
    val tipOrResource: String = ""
)

@Entity(tableName = "whatsapp_announcement")
data class WhatsAppAnnouncement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupName: String, // e.g. "BTech Section A", "Project Group - Delta"
    val title: String,
    val body: String,
    val timeString: String, // e.g. "14:20 PM" or "11:05 AM"
    val isUrgent: Boolean = false,
    val fileUrl: String? = null
)

@Entity(tableName = "extracted_task")
data class ExtractedTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String, // e.g. "Thermodynamics Report"
    val source: String, // e.g. "#TheorySection"
    val deadlineText: String, // e.g. "DUE IN 4 HOURS"
    val isSynced: Boolean = false
)

@Entity(tableName = "whatsapp_message")
data class WhatsAppMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val senderTag: String,
    val text: String,
    val timeString: String,
    val isFromAi: Boolean = false
)
