package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    SYLLABUS,
    TASKS,
    GROUPS,
    PROFILE
}

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AppRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database.dao())
        
        // Load initial mock seed data to make the app looks gorgeous immediately
        viewModelScope.launch {
            repository.loadInitialSeedDataIfEmpty()
        }
    }

    // Navigation state
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    // Input States
    private val _syllabusInput = MutableStateFlow("")
    val syllabusInput: StateFlow<String> = _syllabusInput.asStateFlow()

    private val _whatsappInput = MutableStateFlow("")
    val whatsappInput: StateFlow<String> = _whatsappInput.asStateFlow()

    // Loading & Progress States
    private val _isSyllabusProcessing = MutableStateFlow(false)
    val isSyllabusProcessing: StateFlow<Boolean> = _isSyllabusProcessing.asStateFlow()

    private val _syllabusProgressValue = MutableStateFlow(35) // Overall Progress % defaults to 35
    val syllabusProgressValue: StateFlow<Int> = _syllabusProgressValue.asStateFlow()

    private val _isWhatsAppProcessing = MutableStateFlow(false)
    val isWhatsAppProcessing: StateFlow<Boolean> = _isWhatsAppProcessing.asStateFlow()

    // Overwhelmed Mode State
    private val _isOverwhelmed = MutableStateFlow(false)
    val isOverwhelmed: StateFlow<Boolean> = _isOverwhelmed.asStateFlow()

    // Database Flows collected as StateFlows
    val studentProfile: StateFlow<StudentProfile?> = repository.studentProfile
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val syllabusModules: StateFlow<List<SyllabusModule>> = repository.syllabusModules
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val studyTasks: StateFlow<List<StudyTask>> = repository.studyTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val whatsappAnnouncements: StateFlow<List<WhatsAppAnnouncement>> = repository.whatsappAnnouncements
        .stateIn(
            scope = viewModelScope,
            started = SharingSharedFlow(),
            initialValue = emptyList()
        )

    val extractedTasks: StateFlow<List<ExtractedTask>> = repository.extractedTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val whatsappMessages: StateFlow<List<WhatsAppMessage>> = repository.whatsappMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Overall metrics
    val tasksFinishedCount: StateFlow<Int> = studyTasks.map { list ->
        list.count { it.isCompleted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalTasksCount: StateFlow<Int> = studyTasks.map { list ->
        list.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Action execution
    fun onSyllabusInputChanged(text: String) {
        _syllabusInput.value = text
    }

    fun onWhatsAppInputChanged(text: String) {
        _whatsappInput.value = text
    }

    fun setOverwhelmed(active: Boolean) {
        _isOverwhelmed.value = active
    }

    fun toggleTaskStatus(task: StudyTask) {
        viewModelScope.launch {
            repository.updateTaskStatus(task, !task.isCompleted)
        }
    }

    fun processSyllabusText() {
        val text = _syllabusInput.value
        if (text.isBlank()) return

        viewModelScope.launch {
            _isSyllabusProcessing.value = true
            _syllabusProgressValue.value = 45
            
            // Artificial incremental progress bar for gorgeous visual UX matching screenshot
            launch {
                repeat(4) {
                    kotlinx.coroutines.delay(600)
                    _syllabusProgressValue.value += (10..15).random()
                }
                _syllabusProgressValue.value = 100
            }

            val success = repository.processSyllabusWithAi(text)
            if (success) {
                _syllabusInput.value = ""
            }
            _isSyllabusProcessing.value = false
            _syllabusProgressValue.value = 100
            // Take the student straight to the roadmap tasks!
            navigateTo(Screen.TASKS)
        }
    }

    fun processWhatsAppMessages() {
        val text = _whatsappInput.value
        if (text.isBlank()) return

        viewModelScope.launch {
            _isWhatsAppProcessing.value = true
            val success = repository.processWhatsAppWithAi(text)
            if (success) {
                _whatsappInput.value = ""
            }
            _isWhatsAppProcessing.value = false
            navigateTo(Screen.GROUPS)
        }
    }

    fun saveStudentProfile(name: String, branch: String, semester: Int) {
        viewModelScope.launch {
            val current = studentProfile.value ?: StudentProfile()
            repository.saveProfile(
                current.copy(
                    name = name,
                    branch = branch,
                    semester = semester
                )
            )
        }
    }

    fun resetData() {
        viewModelScope.launch {
            repository.clearAllData()
            repository.loadInitialSeedDataIfEmpty()
            _isOverwhelmed.value = false
        }
    }

    // Encouraging mentorship helper strings
    fun getMotivationalOneLiner(): String {
        val quotes = listOf(
            "Engineering is the art of organizing and design. You've got this!",
            "Small daily iterations turn massive syllabus mountains into solved steps.",
            "Great developers are not born; they compile themselves block by block.",
            "There's no compiler error you cannot solve with structured logic.",
            "Focus is a resource. Power through this unit, then take a deep breath!",
            "The best way to predict the semester outcome is to build it daily."
        )
        return quotes.random()
    }
}

private fun SharingSharedFlow(): SharingStarted = SharingStarted.WhileSubscribed(5000)
