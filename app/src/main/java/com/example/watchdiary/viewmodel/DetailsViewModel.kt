package com.example.watchdiary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.watchdiary.data.repository.DiaryRepository
import com.example.watchdiary.util.AppError
import com.example.watchdiary.viewmodel.state.DetailsUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job

class DetailsViewModel(
    private val repository: DiaryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()
    private var entryObservation: Job? = null

    fun loadEntry(id: Int) {
        entryObservation?.cancel()
        _uiState.value = DetailsUiState(isLoading = true)
        entryObservation = viewModelScope.launch {
            repository.getEntryById(id).catch {
                _uiState.value = DetailsUiState(isLoading = false, errorMessage = AppError.LOAD.message())
            }.collect { entry ->
                _uiState.value = DetailsUiState(
                    entry = entry,
                    isLoading = false
                )
            }
        }
    }

    fun deleteEntryById(id: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteById(id)
                onSuccess()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = AppError.DELETE.message())
            }
        }
    }
}
