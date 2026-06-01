package com.example.data

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

@JsonClass(generateAdapter = true)
data class SyllabusModelJson(
    val moduleName: String,
    val description: String,
    val difficulty: String,
    val weightage: String,
    val prerequisites: String,
    val weekNumber: Int
)

@JsonClass(generateAdapter = true)
data class SyllabusListContainer(
    val syllabus: List<SyllabusModelJson>
)

@JsonClass(generateAdapter = true)
data class AnnouncementJson(
    val groupName: String,
    val title: String,
    val body: String,
    val timeString: String,
    val isUrgent: Boolean
)

@JsonClass(generateAdapter = true)
data class ExtractedTaskJson(
    val title: String,
    val source: String,
    val deadlineText: String
)

@JsonClass(generateAdapter = true)
data class WhatsAppExtractionContainer(
    val announcements: List<AnnouncementJson>,
    val tasks: List<ExtractedTaskJson>,
    val aiReplyToSuresh: String
)

class AppRepository(private val dao: AppDao) {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    // Database Streaming
    val studentProfile: Flow<StudentProfile?> = dao.getStudentProfile()
    val syllabusModules: Flow<List<SyllabusModule>> = dao.getAllSyllabusModules()
    val studyTasks: Flow<List<StudyTask>> = dao.getStudyTasks()
    val whatsappAnnouncements: Flow<List<WhatsAppAnnouncement>> = dao.getWhatsAppAnnouncements()
    val extractedTasks: Flow<List<ExtractedTask>> = dao.getExtractedTasks()
    val whatsappMessages: Flow<List<WhatsAppMessage>> = dao.getWhatsAppMessages()

    suspend fun saveProfile(profile: StudentProfile) = withContext(Dispatchers.IO) {
        dao.insertStudentProfile(profile)
    }

    suspend fun updateTaskStatus(task: StudyTask, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        dao.updateStudyTask(task.copy(isCompleted = isCompleted))
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        dao.clearSyllabusModules()
        dao.clearStudyTasks()
        dao.clearWhatsAppAnnouncements()
        dao.clearExtractedTasks()
        dao.clearWhatsAppMessages()
    }

    // Load static initial seed data for demonstration (exactly matches the prototype screens)
    suspend fun loadInitialSeedDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentProfile = studentProfile.firstOrNull()
        if (currentProfile == null) {
            dao.insertStudentProfile(
                StudentProfile(
                    name = "Rahul",
                    branch = "CSE",
                    semester = 6,
                    streakDays = 12,
                    overallProgress = 70,
                    currentFocusSubject = "Engineering Mechanics"
                )
            )
        }

        val tasks = studyTasks.firstOrNull() ?: emptyList()
        if (tasks.isEmpty()) {
            dao.insertStudyTasks(
                listOf(
                    StudyTask(
                        subject = "Engineering-Math",
                        topic = "Solve Fourier Transform Problems",
                        explanation = "AI Objective: Master the frequency domain analysis for non-periodic signals. Focus on Question 4.2 in the problem set.",
                        estimatedMinutes = 45,
                        categoryTag = "#ENGINEERING-MATH",
                        isUrgent = true,
                        isCompleted = false,
                        tipOrResource = "Watch NPTEL Math Lectures, Unit 4"
                    ),
                    StudyTask(
                        subject = "Data-Structures",
                        topic = "Implement Red-Black Trees",
                        explanation = "AI Objective: Complete the rotation logic and handle rebalancing cases for the balanced search tree implementation.",
                        estimatedMinutes = 60,
                        categoryTag = "#DATA-STRUCTURES",
                        isUrgent = false,
                        isCompleted = false,
                        tipOrResource = "GeeksforGeeks balanced trees tutorial"
                    ),
                    StudyTask(
                        subject = "Operating-Systems",
                        topic = "Review Semaphore Concepts",
                        explanation = "AI Objective: Understand the 'Dining Philosophers' problem and how semaphores resolve deadlocks in shared memory.",
                        estimatedMinutes = 30,
                        categoryTag = "#OPERATING-SYSTEMS",
                        isUrgent = false,
                        isCompleted = false,
                        tipOrResource = "OS Course notes, Chapter 5"
                    ),
                    StudyTask(
                        subject = "Ethics",
                        topic = "Weekly Reflection Log",
                        explanation = "Brief explanation: Summarize the ethical implications of AI in engineering careers as discussed in yesterday's lecture.",
                        estimatedMinutes = 15,
                        categoryTag = "#ETHICS",
                        isUrgent = false,
                        isCompleted = false,
                        tipOrResource = "Submit on course portal"
                    )
                )
            )
        }

        val announcements = whatsappAnnouncements.firstOrNull() ?: emptyList()
        if (announcements.isEmpty()) {
            dao.insertWhatsAppAnnouncements(
                listOf(
                    WhatsAppAnnouncement(
                        groupName = "BTech Section A",
                        title = "End Semester Exam Dates Released",
                        body = "The HOD has confirmed the dates for 6th Sem exams. Starting from May 15th. Full schedule attached in the main group PDF.",
                        timeString = "14:20 PM",
                        isUrgent = true,
                        fileUrl = "exam_schedule_6th_sem.pdf"
                    ),
                    WhatsAppAnnouncement(
                        groupName = "Project Group - Delta",
                        title = "Guide Meeting Rescheduled",
                        body = "Dr. Sharma shifted tomorrow's review to 3:00 PM in Lab 4. Bring the PCB prototype.",
                        timeString = "11:05 AM",
                        isUrgent = false
                    ),
                    WhatsAppAnnouncement(
                        groupName = "Training & Placement",
                        title = "Mock Interview Drive",
                        body = "Registration for the Friday mock interview drive closes at midnight today.",
                        timeString = "Yesterday",
                        isUrgent = false
                    )
                )
            )
        }

        val extTasks = extractedTasks.firstOrNull() ?: emptyList()
        if (extTasks.isEmpty()) {
            dao.insertExtractedTasks(
                listOf(
                    ExtractedTask(
                        title = "Thermodynamics Report",
                        source = "#TheorySection",
                        deadlineText = "DUE IN 4 HOURS"
                    ),
                    ExtractedTask(
                        title = "DSA Lab Submission",
                        source = "CS Lab GRP",
                        deadlineText = "MONDAY, 10 AM"
                    )
                )
            )
        }

        val msgs = whatsappMessages.firstOrNull() ?: emptyList()
        if (msgs.isEmpty()) {
            dao.insertWhatsAppMessages(
                listOf(
                    WhatsAppMessage(
                        sender = "Suresh A.",
                        senderTag = "BTech Section A",
                        text = "Did anyone note down the reference books for Microprocessors? Prof mentioned them in the morning class.",
                        timeString = "12:45 PM",
                        isFromAi = false
                    ),
                    WhatsAppMessage(
                        sender = "AI Assistant",
                        senderTag = "System Core",
                        text = "Based on the lecture recording, the reference books are:\n\n• Gaonkar - Microprocessor Architecture\n• Douglas Hall - Microprocessors and Interfacing",
                        timeString = "Just now",
                        isFromAi = true
                    )
                )
            )
        }

        val modules = syllabusModules.firstOrNull() ?: emptyList()
        if (modules.isEmpty()) {
            dao.insertSyllabusModules(
                listOf(
                    SyllabusModule(
                        moduleName = "Unit 1: Infinite Series & Fourier Transforms",
                        description = "Harmonic analysis, Dirichlet conditions, Euler's formula, Fourier expansion of periodic functions.",
                        difficulty = "High",
                        weightage = "25% Weightage",
                        prerequisites = "Single variable calculus, Infinite sequences",
                        weekNumber = 1
                    ),
                    SyllabusModule(
                        moduleName = "Unit 2: Balanced Trees",
                        description = "Analysis of binary trees, rotation rules for Red-Black trees, insertion algorithms, and node-color balancing.",
                        difficulty = "Medium",
                        weightage = "15% Weightage",
                        prerequisites = "Binary Search Trees, Pointers & Recursion",
                        weekNumber = 2
                    ),
                    SyllabusModule(
                        moduleName = "Unit 3: Operating System Processes",
                        description = "Concurrency control, Critical Section problem, Semaphores implementation, and dining philosophers synchronization.",
                        difficulty = "High",
                        weightage = "20% Weightage",
                        prerequisites = "Thread states, CPU schedules",
                        weekNumber = 3
                    )
                )
            )
        }
    }

    // Real syllabus analysis using Gemini API (with robust offline mockup fallback)
    suspend fun processSyllabusWithAi(syllabusText: String): Boolean = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

        if (!hasKey) {
            // Emulate AI delay, and parse mock response matching current syllabus context
            Thread.sleep(2500)
            mockSyllabusCreation(syllabusText)
            return@withContext true
        }

        val prompt = """
            You are an expert BTech Syllabus Analyzer. Cleanly parse the following syllabus text:
            ${syllabusText}
            
            Identify 3 key academic modules/units. Each module should include:
            1. Name of module (one short string)
            2. High-level description detailing key concepts (one string)
            3. Level of course difficulty (either "High", "Medium", or "Low")
            4. Estimated weightage or core status (e.g. "20% in Finals" or "Core Unit")
            5. Recommended prerequisite academic concept (one string)
            6. Relevant week number to study (from 1 to 3)

            IMPORTANT: Response format MUST be a valid JSON array wrapped inside a parent object with a single "syllabus" key.
            Do not include any Markdown tags or delimiters. Provide pure JSON only!
            Example format:
            {
              "syllabus": [
                {
                  "moduleName": "Mathematics III: Fourier Transforms",
                  "description": "Dirichlet conditions, frequency spectrum analysis, Parsevals theorem.",
                  "difficulty": "High",
                  "weightage": "25% weightage",
                  "prerequisites": "Single Variable Calculus",
                  "weekNumber": 1
                }
              ]
            }
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(responseMimeType = "application/json", temperature = 0.2f)
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonString = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?.trim() ?: ""

            // Clean any potential markdown wrappers if the model ignored request
            val cleanedJson = cleanJsonString(jsonString)

            val adapter = moshi.adapter(SyllabusListContainer::class.java)
            val container = adapter.fromJson(cleanedJson)

            if (container != null && container.syllabus.isNotEmpty()) {
                dao.clearSyllabusModules()
                val converted = container.syllabus.map {
                    SyllabusModule(
                        moduleName = it.moduleName,
                        description = it.description,
                        difficulty = it.difficulty,
                        weightage = it.weightage,
                        prerequisites = it.prerequisites,
                        weekNumber = it.weekNumber
                    )
                }
                dao.insertSyllabusModules(converted)

                // Populate corresponding tasks derived from these models for the student!
                dao.clearStudyTasks()
                val newTasks = container.syllabus.flatMapIndexed { index, m ->
                    listOf(
                        StudyTask(
                            subject = m.moduleName.take(15),
                            topic = "Master: ${m.moduleName}",
                            explanation = m.description,
                            estimatedMinutes = if (m.difficulty == "High") 60 else 45,
                            categoryTag = "#${m.moduleName.split(" ").firstOrNull()?.replace(":", "")?.uppercase() ?: "BTECH"}",
                            isUrgent = index == 0,
                            tipOrResource = "Refer to pre-requisite: ${m.prerequisites}"
                        )
                    )
                }
                dao.insertStudyTasks(newTasks)
                return@withContext true
            }
        } catch (e: Exception) {
            Log.e("AppRepository", "Gemini API failed, using fallback mock creator", e)
        }

        // Fallback or failed
        mockSyllabusCreation(syllabusText)
        return@withContext true
    }

    // Helper mock creator for custom syllabus texts when API is unavailable
    private suspend fun mockSyllabusCreation(syllabusText: String) {
        dao.clearSyllabusModules()
        val cleaned = syllabusText.trim()
        val lines = cleaned.split("\n").filter { it.isNotBlank() }
        val keyword = if (lines.isNotEmpty()) lines[0].take(30) else "Engineering Mechanics"

        val generated = listOf(
            SyllabusModule(
                moduleName = "Unit 1: Introduction to $keyword",
                description = "Primary focus on system design, base rules, and core concepts matching: $cleaned",
                difficulty = "High",
                weightage = "25% Exam Weight",
                prerequisites = "BTech Foundations Phase I",
                weekNumber = 1
            ),
            SyllabusModule(
                moduleName = "Unit 2: Applied Calculations in $keyword",
                description = "Solving advanced formula structures and practicing numerical modeling algorithms.",
                difficulty = "Medium",
                weightage = "20% Semester Score",
                prerequisites = "Differential Mathematics",
                weekNumber = 2
            ),
            SyllabusModule(
                moduleName = "Unit 3: Integration and Research on $keyword",
                description = "Constructing practical lab configurations and compiling technical reports.",
                difficulty = "Low",
                weightage = "15% Lab Weight",
                prerequisites = "Basic Laboratory Conduct",
                weekNumber = 3
            )
        )
        dao.insertSyllabusModules(generated)

        // Generate matching Study Tasks
        dao.clearStudyTasks()
        dao.insertStudyTasks(listOf(
            StudyTask(
                subject = keyword.take(15).uppercase(),
                topic = "Solve Fourier Transforms in $keyword",
                explanation = "AI Objective: Study the frequency response structure. Answer critical question worksheets.",
                estimatedMinutes = 45,
                categoryTag = "#${keyword.take(8).replace(" ", "").uppercase()}",
                isUrgent = true,
                tipOrResource = "YouTube lectures or portal course material"
            ),
            StudyTask(
                subject = "COMPUTATION",
                topic = "Model Formula parameters in Python",
                explanation = "Practical Coding task. Run system models of $keyword on the collaborative notebooks.",
                estimatedMinutes = 60,
                categoryTag = "#CODING",
                isUrgent = false,
                tipOrResource = "Official Docs / Github code repositories"
            )
        ))
    }

    // Extract WhatsApp Group messages (with robust fallback)
    suspend fun processWhatsAppWithAi(copiedText: String): Boolean = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

        if (!hasKey) {
            Thread.sleep(2500)
            mockWhatsAppExtraction(copiedText)
            return@withContext true
        }

        val prompt = """
            You are a college college WhatsApp group announcement and task extractor. Analyze these messages:
            ${copiedText}

            Extract:
            1. Important Announcements: Max 3 items. For each provide groupName, title, body, timeString, and isUrgent (boolean).
            2. Extracted Tasks: Max 3 items. For each provide title, source, deadlineText.
            3. AI Assistant response: Write a concise, supportive help response answering Suresh A's question about microprocessor reference books!
               And address Suresh's message directly. Keep it to 2-3 friendly lines listing books.

            IMPORTANT: Response format MUST be a valid JSON object wrapped with keys:
            "announcements" (array of announcement objects), "tasks" (array of task objects), and "aiReplyToSuresh" (string).
            Provide pure JSON only! No markdown!
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
            generationConfig = GeminiGenerationConfig(responseMimeType = "application/json", temperature = 0.3f)
        )

        try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            val jsonString = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?.trim() ?: ""

            val cleanedJson = cleanJsonString(jsonString)
            val adapter = moshi.adapter(WhatsAppExtractionContainer::class.java)
            val container = adapter.fromJson(cleanedJson)

            if (container != null) {
                dao.clearWhatsAppAnnouncements()
                val annList = container.announcements.map {
                    WhatsAppAnnouncement(
                        groupName = it.groupName,
                        title = it.title,
                        body = it.body,
                        timeString = it.timeString,
                        isUrgent = it.isUrgent
                    )
                }
                dao.insertWhatsAppAnnouncements(annList)

                dao.clearExtractedTasks()
                val tList = container.tasks.map {
                    ExtractedTask(
                        title = it.title,
                        source = it.source,
                        deadlineText = it.deadlineText
                    )
                }
                dao.insertExtractedTasks(tList)

                // Inject Suresh's question and AI response into the Message Pulse
                dao.clearWhatsAppMessages()
                dao.insertWhatsAppMessages(
                    listOf(
                        WhatsAppMessage(
                            sender = "Suresh A.",
                            senderTag = "BTech Section A",
                            text = if (copiedText.contains("Suresh")) copiedText.take(150) else "Hey, has anyone noted down the reference books for Microprocessors? Our prof suggested some today.",
                            timeString = "12:45 PM"
                        ),
                        WhatsAppMessage(
                            sender = "AI Assistant",
                            senderTag = "System Core",
                            text = container.aiReplyToSuresh,
                            timeString = "Just now",
                            isFromAi = true
                        )
                    )
                )
                return@withContext true
            }
        } catch (e: Exception) {
            Log.e("AppRepository", "WhatsApp Extraction failed with Gemini, using mockup fallback", e)
        }

        mockWhatsAppExtraction(copiedText)
        return@withContext true
    }

    private suspend fun mockWhatsAppExtraction(copiedText: String) {
        val snippet = copiedText.trim()
        val keyword = if (snippet.isNotEmpty()) snippet.take(40) else "Syllabus dates"

        dao.clearWhatsAppAnnouncements()
        dao.insertWhatsAppAnnouncements(
            listOf(
                WhatsAppAnnouncement(
                    groupName = "BTech Section A",
                    title = "Extracted Notice: $keyword",
                    body = "Extracted from messages: \"$snippet\"",
                    timeString = "14:20 PM",
                    isUrgent = true
                ),
                WhatsAppAnnouncement(
                    groupName = "Training & Placement",
                    title = "Mock Interview Drive",
                    body = "Registration for Friday drive closes at midnight. Keep resume files prepared.",
                    timeString = "11:05 AM",
                    isUrgent = false
                )
            )
        )

        dao.clearExtractedTasks()
        dao.insertExtractedTasks(
            listOf(
                ExtractedTask(
                    title = "Finish Syllabus Review",
                    source = "#PersonalizedAI",
                    deadlineText = "DUE IN 2 HOURS"
                ),
                ExtractedTask(
                    title = "Lab Submissions",
                    source = "Section GRP",
                    deadlineText = "MONDAY, 10 AM"
                )
            )
        )

        dao.clearWhatsAppMessages()
        dao.insertWhatsAppMessages(
            listOf(
                WhatsAppMessage(
                    sender = "Suresh A.",
                    senderTag = "BTech Section A",
                    text = "Did anyone note down the reference books for Microprocessors? Prof mentioned them in the morning class.",
                    timeString = "12:45 PM",
                    isFromAi = false
                ),
                WhatsAppMessage(
                    sender = "AI Assistant",
                    senderTag = "System Core",
                    text = "Aha! I recorded that. The core reference books for Microprocessors are:\n1. Gaonkar - Microprocessor Architecture\n2. Douglas Hall - Microprocessors and Interfacing.\nGood luck studying!",
                    timeString = "Just now",
                    isFromAi = true
                )
            )
        )
    }

    private fun cleanJsonString(raw: String): String {
        return raw.lines()
            .filter { !it.trim().startsWith("```") }
            .joinToString("\n")
            .trim()
    }
}
