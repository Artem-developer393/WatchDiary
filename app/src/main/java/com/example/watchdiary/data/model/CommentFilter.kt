package com.example.watchdiary.data.model
import androidx.annotation.StringRes
import com.example.watchdiary.R

enum class CommentFilter(@StringRes val labelRes: Int) {
    ALL(R.string.all),
    WITH_COMMENT(R.string.with_comment),
    WITHOUT_COMMENT(R.string.without_comment)
}
