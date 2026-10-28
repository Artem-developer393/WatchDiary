package com.example.watchdiary.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Transaction
import com.example.watchdiary.data.model.DiaryEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DiaryEntryDao {

    @Query("SELECT * FROM diary_entries")
    fun getAllEntries(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries WHERE id = :id LIMIT 1")
    fun getEntryById(id: Int): Flow<DiaryEntry?>

    @Insert
    suspend fun insertEntry(entry: DiaryEntry)

    @Update
    suspend fun updateEntry(entry: DiaryEntry)

    @Delete
    suspend fun deleteEntry(entry: DiaryEntry)

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query(
        """
        SELECT COUNT(*) FROM diary_entries
        WHERE title = :title AND watchDate = :watchDate
        """
    )
    suspend fun countByTitleAndDate(title: String, watchDate: LocalDate): Int
    @Query("SELECT * FROM diary_entries")
    suspend fun getAllEntriesOnce(): List<DiaryEntry>

    /** One transaction prevents a partially imported backup when a database write fails. */
    @Transaction
    suspend fun insertUniqueEntries(entries: List<DiaryEntry>): Int {
        var count = 0
        entries.forEach { entry ->
            if (countByTitleAndDate(entry.title, entry.watchDate) == 0) {
                insertEntry(entry.copy(id = 0))
                count++
            }
        }
        return count
    }
}
