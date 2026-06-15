package com.jeiel85.daddymode.data.repository

import com.jeiel85.daddymode.data.dao.AppDao
import com.jeiel85.daddymode.data.model.MoodCheck
import com.jeiel85.daddymode.data.model.BreathSession
import com.jeiel85.daddymode.data.model.DadAction
import com.jeiel85.daddymode.data.model.DailyDadMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val appDao: AppDao) {

    val allMoodChecks: Flow<List<MoodCheck>> = appDao.getAllMoodChecks()
    val allBreathSessions: Flow<List<BreathSession>> = appDao.getAllBreathSessions()
    val activeActions: Flow<List<DadAction>> = appDao.getActiveActions()
    val allActions: Flow<List<DadAction>> = appDao.getAllActions()
    val allDailyDadModes: Flow<List<DailyDadMode>> = appDao.getAllDailyDadModes()

    fun getMoodChecksFromDate(startDate: String): Flow<List<MoodCheck>> {
        return appDao.getMoodChecksFromDate(startDate)
    }

    suspend fun getMoodCheckByDate(date: String): MoodCheck? = withContext(Dispatchers.IO) {
        appDao.getMoodCheckByDate(date)
    }

    suspend fun insertMoodCheck(moodCheck: MoodCheck) = withContext(Dispatchers.IO) {
        appDao.insertMoodCheck(moodCheck)
    }

    suspend fun deleteMoodCheck(id: Int) = withContext(Dispatchers.IO) {
        appDao.deleteMoodCheck(id)
    }

    suspend fun insertBreathSession(session: BreathSession) = withContext(Dispatchers.IO) {
        appDao.insertBreathSession(session)
    }

    suspend fun getBreathSessionByDate(date: String): BreathSession? = withContext(Dispatchers.IO) {
        appDao.getBreathSessionByDate(date)
    }

    suspend fun insertAction(action: DadAction): Long = withContext(Dispatchers.IO) {
        appDao.insertAction(action)
    }

    suspend fun updateAction(action: DadAction) = withContext(Dispatchers.IO) {
        appDao.updateAction(action)
    }

    suspend fun deleteAction(action: DadAction) = withContext(Dispatchers.IO) {
        appDao.deleteAction(action)
    }

    suspend fun getActionById(id: Int): DadAction? = withContext(Dispatchers.IO) {
        appDao.getActionById(id)
    }

    fun getDailyDadModeByDate(date: String): Flow<DailyDadMode?> {
        return appDao.getDailyDadModeByDate(date)
    }

    suspend fun getDailyDadModeByDateSync(date: String): DailyDadMode? = withContext(Dispatchers.IO) {
        appDao.getDailyDadModeByDateSync(date)
    }

    suspend fun insertDailyDadMode(dailyDadMode: DailyDadMode) = withContext(Dispatchers.IO) {
        appDao.insertDailyDadMode(dailyDadMode)
    }

    suspend fun updateDailyDadMode(dailyDadMode: DailyDadMode) = withContext(Dispatchers.IO) {
        appDao.updateDailyDadMode(dailyDadMode)
    }

    // Prepopulate default actions on launch
    suspend fun checkAndPrepopulateActions() = withContext(Dispatchers.IO) {
        val actions = appDao.getAllActions().firstOrNull() ?: emptyList()
        if (actions.isEmpty()) {
            val defaults = listOf(
                DadAction(title = "아이 눈 맞추며 이야기 3분 들어주기", isDefault = true, isActive = true),
                DadAction(title = "아내에게 따뜻한 손잡기와 고맙다고 말하기", isDefault = true, isActive = true),
                DadAction(title = "집에 들어가서 휴대폰 10분간 완전히 내려놓기", isDefault = true, isActive = true),
                DadAction(title = "아이 들어가자마자 꼬옥 안아주기", isDefault = true, isActive = true),
                DadAction(title = "가족과 함께 저녁 시간 동안 오늘 하루 이야기 나누기", isDefault = true, isActive = true)
            )
            for (action in defaults) {
                appDao.insertAction(action)
            }
        }
    }
}
