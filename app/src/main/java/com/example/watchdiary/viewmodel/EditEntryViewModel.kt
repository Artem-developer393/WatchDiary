package com.example.watchdiary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watchdiary.data.model.ContentType
import com.example.watchdiary.data.model.DiaryEntry
import com.example.watchdiary.data.repository.DiaryRepository
import com.example.watchdiary.util.AppError
import com.example.watchdiary.util.UiMessage
import com.example.watchdiary.R
import com.example.watchdiary.viewmodel.state.EditEntryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import java.time.LocalDate

class EditEntryViewModel(
    private val repository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditEntryUiState())
    val uiState: StateFlow<EditEntryUiState> = _uiState.asStateFlow()

    fun loadEntry(entryId: Int) {
        viewModelScope.launch {
            val entry = try {
                repository.getEntryById(entryId).first()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = EditEntryUiState(isLoading = false, errorMessage = AppError.LOAD.message())
                return@launch
            }

            if (entry != null) {
                _uiState.value = EditEntryUiState(
                    entryId = entry.id,
                    title = entry.title,
                    contentType = entry.contentType,
                    watchDate = entry.watchDate,
                    rating = entry.rating,
                    comment = entry.comment,
                    isLoading = false
                )
            } else {
                _uiState.value = EditEntryUiState(
                    isLoading = false,
                    errorMessage = UiMessage(R.string.entry_not_found)
                )
            }
        }
    }

    fun updateTitle(value: String) {
        _uiState.value = _uiState.value.copy(title = value, errorMessage = null)
    }

    fun updateContentType(value: ContentType) {
        _uiState.value = _uiState.value.copy(contentType = value, errorMessage = null)
    }

    fun updateWatchDate(value: LocalDate?) {
        _uiState.value = _uiState.value.copy(watchDate = value, errorMessage = null)
    }

    fun updateRating(value: Int) {
        _uiState.value = _uiState.value.copy(rating = value, errorMessage = null)
    }

    fun updateComment(value: String) {
        _uiState.value = _uiState.value.copy(comment = value, errorMessage = null)
    }

    fun saveChanges() {
        val state = _uiState.value

        if (state.title.trim().isBlank()) {
            _uiState.value = state.copy(errorMessage = UiMessage(R.string.enter_a_title))
            return
        }

        viewModelScope.launch {
            try {
                repository.updateEntry(
                    DiaryEntry(
                        id = state.entryId,
                        title = state.title.trim(),
                        contentType = state.contentType,
                        watchDate = state.watchDate ?: LocalDate.now(),
                        rating = state.rating,
                        comment = state.comment.trim()
                    )
                )

                _uiState.value = _uiState.value.copy(
                    saveSuccess = true,
                    errorMessage = null
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = AppError.SAVE.message())
            }
        }
    }

    fun consumeSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
}
