package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.utils.TimeLeftUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TimeLeftViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val appDao = db.appDao()

    // Base flows from database
    val allTasks: StateFlow<List<Task>> = appDao.getAllTasksFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSessions: StateFlow<List<FocusSession>> = appDao.getAllSessionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<Settings> = appDao.getSettingsFlow()
        .map { it ?: Settings() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Settings())

    // Filtered Flow for TODAY'S tasks and sessions
    val todaysTasks = allTasks.map { tasks ->
        tasks.filter { TimeLeftUtils.isToday(it.createdAt) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todaysSessions = allSessions.map { sessions ->
        sessions.filter { TimeLeftUtils.isToday(it.completedAt) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Derived statistics
    val tasksCompletedCount = todaysTasks.map { tasks ->
        tasks.count { it.completed }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalTasksCount = todaysTasks.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val focusMinutes = todaysSessions.map { sessions ->
        sessions.filter { it.type == "Work" }.sumOf { it.duration } / 60
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val productivityScore = combine(tasksCompletedCount, totalTasksCount, focusMinutes) { completed, total, focus ->
        TimeLeftUtils.calculateProductivityScore(completed, total, focus)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Initialize default settings if not exists
        viewModelScope.launch {
            val existing = appDao.getSettingsDirect()
            if (existing == null) {
                appDao.insertSettings(Settings())
            }
        }
    }

    // ACTIONS
    fun addTask(title: String, category: String) {
        viewModelScope.launch {
            appDao.insertTask(
                Task(
                    title = title,
                    category = category,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            appDao.updateTask(task.copy(completed = !task.completed))
        }
    }

    fun incrementTaskPomodoro(task: Task) {
        viewModelScope.launch {
            appDao.updateTask(task.copy(pomodorosUsed = task.pomodorosUsed + 1))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            appDao.deleteTaskById(task.id)
        }
    }

    fun addFocusSession(type: String, durationInSeconds: Int) {
        viewModelScope.launch {
            appDao.insertSession(
                FocusSession(
                    type = type,
                    duration = durationInSeconds,
                    completedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateSettings(workDuration: Int, breakDuration: Int, startHour: Int, endHour: Int) {
        viewModelScope.launch {
            appDao.insertSettings(
                Settings(
                    workDuration = workDuration,
                    breakDuration = breakDuration,
                    dailyStartHour = startHour,
                    dailyEndHour = endHour
                )
            )
        }
    }

    fun clearTodayData() {
        viewModelScope.launch {
            appDao.clearAllTasks()
            appDao.clearAllSessions()
        }
    }
}
