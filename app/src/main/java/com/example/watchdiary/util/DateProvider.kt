package com.example.watchdiary.util

import java.time.LocalDate

object DateProvider {

    fun today(): LocalDate {
        return LocalDate.now()
    }

    fun of(year: Int, month: Int, day: Int): LocalDate {
        return LocalDate.of(year, month, day)
    }
}