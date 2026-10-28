package com.example.watchdiary.viewmodel.state

import com.example.watchdiary.data.model.ContentType
import com.example.watchdiary.util.UiMessage
import java.time.LocalDate

data class AddEntryUiState(
    val title: String = "",
    val contentType: ContentType = ContentType.MOVIE,
    val watchDate: LocalDate? = null,
    val rating: Int = 0,
    val comment: String = "",
    val saveSuccess: Boolean = false,
    val errorMessage: UiMessage? = null
)
