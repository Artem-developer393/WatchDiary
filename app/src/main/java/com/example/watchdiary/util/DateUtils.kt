package com.example.watchdiary.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateUtils {
    private val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())

    fun format(date: LocalDate): String = date.format(formatter)
}