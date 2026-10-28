package com.example.watchdiary.data.local

import androidx.room.TypeConverter
import com.example.watchdiary.data.model.ContentType
import java.time.LocalDate

class Converters {

    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun fromContentType(value: ContentType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toContentType(value: String?): ContentType? {
        return value?.let { ContentType.valueOf(it) }
    }
}