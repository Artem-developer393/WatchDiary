package com.example.watchdiary.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val contentType: ContentType,
    val watchDate: LocalDate,
    val rating: Int,
    val comment: String
)