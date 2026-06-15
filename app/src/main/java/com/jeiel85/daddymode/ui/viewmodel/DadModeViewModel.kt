package com.jeiel85.daddymode.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jeiel85.daddymode.data.model.MoodCheck
import com.jeiel85.daddymode.data.model.BreathSession
import com.jeiel85.daddymode.data.model.DadAction
import com.jeiel85.daddymode.data.model.DailyDadMode
import com.jeiel85.daddymode.data.repository.AppRepository
import com.jeiel85.daddymode.notification.CommuteNotificationHelper
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DadModeViewModel(
    application: Application,
    private val repository: AppRepository
) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("dad_mode_settings", Context.MODE_PRIVATE)

    // Current Date
    val todayDateString: StateFlow<String> = flow {
        while (true) {
            emit(getCurrentDateString())
            delay(60000) // check every minute
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), getCurrentDateString())

    // --- Screen State Flows ---
    val allMoodChecks: StateFlow<List<MoodCheck>> = repository.allMoodChecks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allBreathSessions: StateFlow<List<BreathSession>> = repository.allBreathSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeActions: StateFlow<List<DadAction>> = repository.activeActions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDailyDadModes: StateFlow<List<DailyDadMode>> = repository.allDailyDadModes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Observe today's MoodCheck
    val todayMoodCheck: StateFlow<MoodCheck?> = todayDateString.flatMapLatest { date ->
        repository.allMoodChecks.map { list -> list.find { it.date == date } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Observe today's BreathSession
    val todayBreathSession: StateFlow<BreathSession?> = todayDateString.flatMapLatest { date ->
        repository.allBreathSessions.map { list -> list.find { it.date == date } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Observe today's DailyDadMode configuration
    val todayDailyDadMode: StateFlow<DailyDadMode?> = todayDateString.flatMapLatest { date ->
        repository.getDailyDadModeByDate(date)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)


    // --- Local Shared Preferences Configurations ---
    private val _notificationEnabled = MutableStateFlow(sharedPrefs.getBoolean("notification_enabled", true))
    val notificationEnabled: StateFlow<Boolean> = _notificationEnabled.asStateFlow()

    private val _notificationHour = MutableStateFlow(sharedPrefs.getInt("notification_hour", 18))
    val notificationHour: StateFlow<Int> = _notificationHour.asStateFlow()

    private val _notificationMinute = MutableStateFlow(sharedPrefs.getInt("notification_minute", 30))
    val notificationMinute: StateFlow<Int> = _notificationMinute.asStateFlow()

    private val _breathDurationMinutes = MutableStateFlow(sharedPrefs.getInt("breath_duration_minutes", 3))
    val breathDurationMinutes: StateFlow<Int> = _breathDurationMinutes.asStateFlow()

    // Breathing Patterns
    private val _inhaleSeconds = MutableStateFlow(sharedPrefs.getInt("inhale_seconds", 4))
    val inhaleSeconds: StateFlow<Int> = _inhaleSeconds.asStateFlow()

    private val _holdSeconds = MutableStateFlow(sharedPrefs.getInt("hold_seconds", 2))
    val holdSeconds: StateFlow<Int> = _holdSeconds.asStateFlow()

    private val _exhaleSeconds = MutableStateFlow(sharedPrefs.getInt("exhale_seconds", 6))
    val exhaleSeconds: StateFlow<Int> = _exhaleSeconds.asStateFlow()

    // Phrases Recommendations List
    private val defaultPhrases = setOf(
        "“오늘 하루 어땠어?”",
        "“아빠 왔다. 보고 싶었어.”",
        "“오늘 고생 많았지?”",
        "“우리 잠깐 안아볼까?”",
        "“아빠랑 산책할 사람 누구?”",
        "“보고 싶어서 얼른 퇴근했지.”"
    )
    private val _phrases = MutableStateFlow<List<String>>(emptyList())
    val phrases: StateFlow<List<String>> = _phrases.asStateFlow()

    init {
        viewModelScope.launch {
            // Prepopulate actions in DB if empty
            repository.checkAndPrepopulateActions()
            // Load custom phrase list
            loadPhrases()
            // Initial alarm setup
            syncAlarm()
        }
    }

    // --- Alarm Configurations ---
    fun updateNotificationSettings(enabled: Boolean, hour: Int, minute: Int) {
        _notificationEnabled.value = enabled
        _notificationHour.value = hour
        _notificationMinute.value = minute

        sharedPrefs.edit().apply {
            putBoolean("notification_enabled", enabled)
            putInt("notification_hour", hour)
            putInt("notification_minute", minute)
            apply()
        }
        syncAlarm()
    }

    private fun syncAlarm() {
        if (_notificationEnabled.value) {
            CommuteNotificationHelper.scheduleNotification(
                getApplication(),
                _notificationHour.value,
                _notificationMinute.value
            )
        } else {
            CommuteNotificationHelper.cancelNotification(getApplication())
        }
    }

    // --- Breathing Configuration ---
    fun updateBreathSettings(durationMin: Int, inhale: Int, hold: Int, exhale: Int) {
        _breathDurationMinutes.value = durationMin
        _inhaleSeconds.value = inhale
        _holdSeconds.value = hold
        _exhaleSeconds.value = exhale

        sharedPrefs.edit().apply {
            putInt("breath_duration_minutes", durationMin)
            putInt("inhale_seconds", inhale)
            putInt("hold_seconds", hold)
            putInt("exhale_seconds", exhale)
            apply()
        }
    }

    // --- Phrase Management ---
    private fun loadPhrases() {
        val saved = sharedPrefs.getStringSet("custom_phrases", null)
        if (saved == null) {
            _phrases.value = defaultPhrases.toList()
        } else {
            _phrases.value = saved.toList()
        }
    }

    fun addPhrase(phraseText: String) {
        if (phraseText.isBlank()) return
        val currentSet = _phrases.value.toMutableSet()
        val cleaned = if (phraseText.startsWith("“") && phraseText.endsWith("”")) phraseText else "“$phraseText”"
        currentSet.add(cleaned)
        _phrases.value = currentSet.toList()
        sharedPrefs.edit().putStringSet("custom_phrases", currentSet).apply()
    }

    fun deletePhrase(phraseText: String) {
        val currentSet = _phrases.value.toMutableSet()
        currentSet.remove(phraseText)
        _phrases.value = currentSet.toList()
        sharedPrefs.edit().putStringSet("custom_phrases", currentSet).apply()
    }


    // --- Mood Checks Functions ---
    fun saveMoodCheck(stressScore: Int, bodyState: String, mindState: String, memo: String) {
        viewModelScope.launch {
            val mood = MoodCheck(
                date = todayDateString.value,
                stressScore = stressScore,
                bodyState = bodyState,
                mindState = mindState,
                memo = memo
            )
            repository.insertMoodCheck(mood)
        }
    }


    // --- Breath Session Functions ---
    fun saveBreathSession(isCompleted: Boolean) {
        viewModelScope.launch {
            val session = BreathSession(
                date = todayDateString.value,
                durationSeconds = _breathDurationMinutes.value * 60,
                pattern = "${_inhaleSeconds.value}-${_holdSeconds.value}-${_exhaleSeconds.value}",
                isCompleted = isCompleted
            )
            repository.insertBreathSession(session)
        }
    }


    // --- Custom Action Functions ---
    fun addCustomAction(titleText: String) {
        if (titleText.isBlank()) return
        viewModelScope.launch {
            val custom = DadAction(
                title = titleText,
                isDefault = false,
                isActive = true
            )
            repository.insertAction(custom)
        }
    }

    fun deleteAction(action: DadAction) {
        viewModelScope.launch {
            repository.deleteAction(action)
        }
    }


    // --- Daily Dad Mode functions (Linking chosen sentence & Action) ---
    fun saveTodayPhraseAndAction(phrase: String, actionId: Int) {
        viewModelScope.launch {
            val existing = repository.getDailyDadModeByDateSync(todayDateString.value)
            if (existing != null) {
                val updated = existing.copy(
                    selectedPhrase = phrase,
                    selectedActionId = actionId
                )
                repository.updateDailyDadMode(updated)
            } else {
                val newMode = DailyDadMode(
                    date = todayDateString.value,
                    selectedPhrase = phrase,
                    selectedActionId = actionId,
                    isActionDone = false
                )
                repository.insertDailyDadMode(newMode)
            }
        }
    }

    fun markTodayActionAsDone(isDone: Boolean) {
        viewModelScope.launch {
            val existing = repository.getDailyDadModeByDateSync(todayDateString.value)
            Calendar.getInstance()
            if (existing != null) {
                val updated = existing.copy(isActionDone = isDone)
                repository.updateDailyDadMode(updated)
            } else {
                // If they perform action without configuring phrase/action first, handle gracefully
                val newMode = DailyDadMode(
                    date = todayDateString.value,
                    selectedPhrase = "따뜻한 눈빛으로 바라보기",
                    selectedActionId = 1, // assume first default item
                    isActionDone = isDone
                )
                repository.insertDailyDadMode(newMode)
            }
        }
    }


    // --- Reset All Data ---
    fun resetAllData() {
        viewModelScope.launch {
            // Delete mood checks
            val moods = repository.allMoodChecks.first()
            for (m in moods) {
                repository.deleteMoodCheck(m.id)
            }

            // Restore preferences to defaults
            sharedPrefs.edit().clear().apply()
            _notificationEnabled.value = true
            _notificationHour.value = 18
            _notificationMinute.value = 30
            _breathDurationMinutes.value = 3
            _inhaleSeconds.value = 4
            _holdSeconds.value = 2
            _exhaleSeconds.value = 6
            _phrases.value = defaultPhrases.toList()
            syncAlarm()

            // Re-populate system default actions
            val actions = repository.allActions.first()
            for (a in actions) {
                repository.deleteAction(a)
            }
            repository.checkAndPrepopulateActions()
        }
    }

    fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }
}

class DadModeViewModelFactory(
    private val application: Application,
    private val repository: AppRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DadModeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DadModeViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
