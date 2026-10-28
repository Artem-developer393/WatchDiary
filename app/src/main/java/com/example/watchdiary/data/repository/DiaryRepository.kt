package com.example.watchdiary.data.repository

import com.example.watchdiary.data.local.DiaryEntryDao
import com.example.watchdiary.data.model.DiaryEntry
import kotlinx.coroutines.flow.Flow

class DiaryRepository(
    private val dao: DiaryEntryDao
) {
    fun getAllEntries(): Flow<List<DiaryEntry>> = dao.getAllEntries()

    fun getEntryById(id: Int): Flow<DiaryEntry?> = dao.getEntryById(id)

    suspend fun insertEntry(entry: DiaryEntry) {
        dao.insertEntry(entry)
    }
    suspend fun getAllEntriesOnce(): List<DiaryEntry> {
        return dao.getAllEntriesOnce()
    }

    suspend fun updateEntry(entry: DiaryEntry) {
        dao.updateEntry(entry)
    }

    suspend fun deleteEntry(entry: DiaryEntry) {
        dao.deleteEntry(entry)
    }

    suspend fun deleteById(id: Int) {
        dao.deleteById(id)
    }

    suspend fun importEntriesIfNotExists(entries: List<DiaryEntry>): Int {
        return dao.insertUniqueEntries(entries)
    }
}
