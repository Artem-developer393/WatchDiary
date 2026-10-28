package com.example.watchdiary.viewmodel.state

import com.example.watchdiary.data.model.DiaryEntry
import com.example.watchdiary.util.UiMessage

data class DetailsUiState(
    val entry: DiaryEntry? = null,
    val isLoading: Boolean = true,
    val errorMessage: UiMessage? = null
)
