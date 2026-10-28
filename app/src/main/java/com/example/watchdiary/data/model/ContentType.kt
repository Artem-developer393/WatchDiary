package com.example.watchdiary.data.model
import androidx.annotation.StringRes
import com.example.watchdiary.R

enum class ContentType(@StringRes val labelRes: Int) {
    MOVIE(R.string.movie),
    SERIES(R.string.series),
    ANIME(R.string.anime)
}
