package com.example.watchdiary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watchdiary.data.model.CommentFilter
import com.example.watchdiary.data.model.ContentType
import com.example.watchdiary.data.model.DiaryEntry
import com.example.watchdiary.data.model.SortOption
import com.example.watchdiary.data.repository.DiaryRepository
import com.example.watchdiary.util.AppError
import com.example.watchdiary.viewmodel.state.ListUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import java.time.LocalDate

class ListViewModel(
    private val repository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListUiState())
    val uiState: StateFlow<ListUiState> = _uiState.asStateFlow()

    init {
        observeEntries()
    }

    private fun observeEntries() {
        viewModelScope.launch {
            repository.getAllEntries().catch {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = AppError.LOAD.message())
            }.collect { entries ->
                val currentState = _uiState.value.copy(
                    allEntries = entries,
                    isLoading = false
                )
                _uiState.value = applyFiltersAndSort(currentState)
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = applyFiltersAndSort(_uiState.value.copy(searchQuery = query))
    }

    fun updateType(type: ContentType?) {
        _uiState.value = applyFiltersAndSort(_uiState.value.copy(selectedType = type))
    }

    fun updateRating(rating: Int?) {
        _uiState.value = applyFiltersAndSort(_uiState.value.copy(selectedRating = rating))
    }

    fun updateDate(date: LocalDate?) {
        _uiState.value = applyFiltersAndSort(_uiState.value.copy(selectedDate = date))
    }

    fun updateCommentFilter(filter: CommentFilter) {
        _uiState.value = applyFiltersAndSort(_uiState.value.copy(commentFilter = filter))
    }

    fun updateSortOption(sortOption: SortOption) {
        _uiState.value = applyFiltersAndSort(_uiState.value.copy(sortOption = sortOption))
    }

    fun resetFilters() {
        _uiState.value = applyFiltersAndSort(
            _uiState.value.copy(
                searchQuery = "",
                selectedType = null,
                selectedRating = null,
                selectedDate = null,
                commentFilter = CommentFilter.ALL,
                sortOption = SortOption.DATE_DESC
            )
        )
    }

    /** Wait for Room to finish before offering an Undo action. */
    suspend fun deleteEntry(entry: DiaryEntry): Boolean {
        return try {
            repository.deleteEntry(entry)
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(errorMessage = AppError.DELETE.message())
            false
        }
    }

    fun restoreDeletedEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            try {
                repository.insertEntry(entry.copy(id = 0))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = AppError.SAVE.message())
            }
        }
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun applyFiltersAndSort(state: ListUiState): ListUiState {
        var filtered = state.allEntries

        if (state.searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.title.contains(state.searchQuery.trim(), ignoreCase = true)
            }
        }

        state.selectedType?.let { type ->
            filtered = filtered.filter { it.contentType == type }
        }

        state.selectedRating?.let { rating ->
            filtered = filtered.filter { it.rating == rating }
        }

        state.selectedDate?.let { date ->
            filtered = filtered.filter { it.watchDate == date }
        }

        filtered = when (state.commentFilter) {
            CommentFilter.ALL -> filtered
            CommentFilter.WITH_COMMENT -> filtered.filter { it.comment.isNotBlank() }
            CommentFilter.WITHOUT_COMMENT -> filtered.filter { it.comment.isBlank() }
        }

        filtered = when (state.sortOption) {
            SortOption.DATE_DESC -> filtered.sortedByDescending { it.watchDate }
            SortOption.DATE_ASC -> filtered.sortedBy { it.watchDate }
            SortOption.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
            SortOption.RATING_DESC -> filtered.sortedByDescending { it.rating }
            SortOption.RATING_ASC -> filtered.sortedBy { it.rating }
        }

        return state.copy(visibleEntries = filtered)
    }
}
