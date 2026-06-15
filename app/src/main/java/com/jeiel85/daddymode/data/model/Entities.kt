package com.jeiel85.daddymode.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_checks")
data class MoodCheck(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // yyyy-MM-dd
    val stressScore: Int, // 1 ~ 5
    val bodyState: String, // 괜찮음 / 피곤함 / 지침 / 예민함
    val mindState: String, // 평온 / 답답함 / 화남 / 걱정 / 무기력
    val memo: String
)

@Entity(tableName = "breath_sessions")
data class BreathSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // yyyy-MM-dd
    val durationSeconds: Int,
    val pattern: String, // "4-2-6"
    val isCompleted: Boolean
)

@Entity(tableName = "dad_actions")
data class DadAction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val isDefault: Boolean = false,
    val isActive: Boolean = true
)

@Entity(tableName = "daily_dad_modes")
data class DailyDadMode(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String, // yyyy-MM-dd (unique per day)
    val selectedPhrase: String,
    val selectedActionId: Int, // Refers to DadAction.id
    val isActionDone: Boolean = false
)
