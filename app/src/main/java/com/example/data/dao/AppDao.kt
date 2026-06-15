package com.example.data.dao

import androidx.room.*
import com.example.data.model.MoodCheck
import com.example.data.model.BreathSession
import com.example.data.model.DadAction
import com.example.data.model.DailyDadMode
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // --- Mood Checks ---
    @Query("SELECT * FROM mood_checks ORDER BY date DESC")
    fun getAllMoodChecks(): Flow<List<MoodCheck>>

    @Query("SELECT * FROM mood_checks WHERE date >= :startDate ORDER BY date ASC")
    fun getMoodChecksFromDate(startDate: String): Flow<List<MoodCheck>>

    @Query("SELECT * FROM mood_checks WHERE date = :date LIMIT 1")
    suspend fun getMoodCheckByDate(date: String): MoodCheck?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodCheck(moodCheck: MoodCheck)

    @Query("DELETE FROM mood_checks WHERE id = :id")
    suspend fun deleteMoodCheck(id: Int)


    // --- Breath Sessions ---
    @Query("SELECT * FROM breath_sessions ORDER BY date DESC")
    fun getAllBreathSessions(): Flow<List<BreathSession>>

    @Query("SELECT * FROM breath_sessions WHERE date = :date LIMIT 1")
    suspend fun getBreathSessionByDate(date: String): BreathSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreathSession(session: BreathSession)


    // --- Dad Actions ---
    @Query("SELECT * FROM dad_actions WHERE isActive = 1 ORDER BY isDefault DESC, id ASC")
    fun getActiveActions(): Flow<List<DadAction>>

    @Query("SELECT * FROM dad_actions ORDER BY id ASC")
    fun getAllActions(): Flow<List<DadAction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAction(action: DadAction): Long

    @Update
    suspend fun updateAction(action: DadAction)

    @Delete
    suspend fun deleteAction(action: DadAction)

    @Query("SELECT * FROM dad_actions WHERE id = :id LIMIT 1")
    suspend fun getActionById(id: Int): DadAction?


    // --- Daily Dad Mode ---
    @Query("SELECT * FROM daily_dad_modes WHERE date = :date LIMIT 1")
    fun getDailyDadModeByDate(date: String): Flow<DailyDadMode?>

    @Query("SELECT * FROM daily_dad_modes WHERE date = :date LIMIT 1")
    suspend fun getDailyDadModeByDateSync(date: String): DailyDadMode?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyDadMode(dailyDadMode: DailyDadMode)

    @Update
    suspend fun updateDailyDadMode(dailyDadMode: DailyDadMode)

    @Query("SELECT * FROM daily_dad_modes ORDER BY date DESC")
    fun getAllDailyDadModes(): Flow<List<DailyDadMode>>
}
