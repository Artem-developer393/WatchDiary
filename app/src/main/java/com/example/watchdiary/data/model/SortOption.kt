package com.example.watchdiary.data.model
import androidx.annotation.StringRes
import com.example.watchdiary.R

enum class SortOption(@StringRes val labelRes: Int) {
    DATE_DESC(R.string.date),
    DATE_ASC(R.string.date_2),
    TITLE_ASC(R.string.title_a_z),
    RATING_DESC(R.string.rating_3),
    RATING_ASC(R.string.rating_4)
}
