package com.example.watchdiary

import android.app.Application
import androidx.room.Room
import com.example.watchdiary.data.local.AppDatabase
import com.example.watchdiary.data.repository.DiaryRepository

class WatchDiaryApp : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var repository: DiaryRepository
        private set

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "watch_diary_db"
        ).build()

        repository = DiaryRepository(database.diaryEntryDao())
    }
}