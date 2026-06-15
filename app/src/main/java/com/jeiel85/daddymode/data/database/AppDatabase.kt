package com.jeiel85.daddymode.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.jeiel85.daddymode.data.dao.AppDao
import com.jeiel85.daddymode.data.model.MoodCheck
import com.jeiel85.daddymode.data.model.BreathSession
import com.jeiel85.daddymode.data.model.DadAction
import com.jeiel85.daddymode.data.model.DailyDadMode

@Database(
    entities = [
        MoodCheck::class,
        BreathSession::class,
        DadAction::class,
        DailyDadMode::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dad_mode_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
