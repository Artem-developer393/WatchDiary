package com.example.watchdiary.viewmodel.state

import com.example.watchdiary.data.model.CommentFilter
import com.example.watchdiary.data.model.ContentType
import com.example.watchdiary.data.model.DiaryEntry
import com.example.watchdiary.data.model.SortOption
import com.example.watchdiary.util.UiMessage
import java.time.LocalDate

data class ListUiState(
    val allEntries: List<DiaryEntry> = emptyList(),
    val visibleEntries: List<DiaryEntry> = emptyList(),
    val searchQuery: String = "",
    val selectedType: ContentType? = null,
    val selectedRating: Int? = null,
    val selectedDate: LocalDate? = null,
    val commentFilter: CommentFilter = CommentFilter.ALL,
    val sortOption: SortOption = SortOption.DATE_DESC,
    val isLoading: Boolean = true,
    val errorMessage: UiMessage? = null
)
