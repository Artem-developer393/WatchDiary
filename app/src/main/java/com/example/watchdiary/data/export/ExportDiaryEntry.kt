package com.example.watchdiary.data.export

data class ExportDiaryEntry(
    val id: Int,
    val title: String,
    val contentType: String,
    val watchDate: String,
    val rating: Int,
    val comment: String
)
