package com.example.watchdiary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watchdiary.data.model.ContentType
import com.example.watchdiary.data.model.DiaryEntry
import com.example.watchdiary.data.repository.DiaryRepository
import com.example.watchdiary.util.ImportParser
import com.example.watchdiary.R
import com.example.watchdiary.util.UiMessage
import com.example.watchdiary.util.AppError
import com.example.watchdiary.viewmodel.state.AddEntryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class AddEntryViewModel(
    private val repository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEntryUiState())
    val uiState: StateFlow<AddEntryUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title, errorMessage = null, saveSuccess = false)
    }

    fun updateContentType(contentType: ContentType) {
        _uiState.value = _uiState.value.copy(contentType = contentType, errorMessage = null, saveSuccess = false)
    }

    fun updateWatchDate(date: LocalDate?) {
        _uiState.value = _uiState.value.copy(watchDate = date, errorMessage = null, saveSuccess = false)
    }

    fun updateRating(rating: Int) {
        _uiState.value = _uiState.value.copy(rating = rating, errorMessage = null, saveSuccess = false)
    }

    fun updateComment(comment: String) {
        _uiState.value = _uiState.value.copy(comment = comment, errorMessage = null, saveSuccess = false)
    }

    fun saveEntry() {
        val state = _uiState.value

        if (state.title.trim().isBlank()) {
            _uiState.value = state.copy(errorMessage = UiMessage(R.string.enter_a_title))
            return
        }

        viewModelScope.launch {
            try {
                val entry = DiaryEntry(
                    title = state.title.trim(),
                    contentType = state.contentType,
                    watchDate = state.watchDate ?: LocalDate.now(),
                    rating = state.rating,
                    comment = state.comment.trim()
                )

                repository.insertEntry(entry)
                _uiState.value = AddEntryUiState(saveSuccess = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = state.copy(errorMessage = AppError.SAVE.message())
            }
        }
    }

    fun importFromText(text: String, onResult: (UiMessage) -> Unit) {
        viewModelScope.launch {
            try {
                val result = withContext(Dispatchers.Default) { ImportParser.parseWithReport(text) }
                if (result.entries.isEmpty() && result.invalidLines == 0) {
                    onResult(UiMessage(R.string.file_contains_no_entries))
                    return@launch
                }

                val importedCount = repository.importEntriesIfNotExists(result.entries)
                onResult(UiMessage(R.string.text_import_summary,
                    importedCount, result.entries.size - importedCount, result.invalidLines))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onResult(AppError.IMPORT.message())
            }
        }
    }

    fun importFromJson(json: String, onResult: (UiMessage) -> Unit) {
        viewModelScope.launch {
            val parsed = try {
                withContext(Dispatchers.Default) { ImportParser.parseJson(json) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onResult(UiMessage(R.string.json_import_invalid))
                return@launch
            }
            if (parsed.isEmpty()) {
                onResult(UiMessage(R.string.file_contains_no_entries))
                return@launch
            }
            try {
                val count = repository.importEntriesIfNotExists(parsed)
                onResult(UiMessage(R.string.imported_and_skipped, count, parsed.size - count))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                onResult(AppError.IMPORT.message())
            }
        }
    }

    fun consumeSaveSuccess() {
        _uiState.value = _uiState.value.copy(saveSuccess = false)
    }
}
