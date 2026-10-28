package com.example.watchdiary.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.watchdiary.data.repository.DiaryRepository

class ViewModelFactory(
    private val repository: DiaryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ListViewModel::class.java) -> {
                ListViewModel(repository) as T
            }
            modelClass.isAssignableFrom(AddEntryViewModel::class.java) -> {
                AddEntryViewModel(repository) as T
            }
            modelClass.isAssignableFrom(DetailsViewModel::class.java) -> {
                DetailsViewModel(repository) as T
            }
            modelClass.isAssignableFrom(EditEntryViewModel::class.java) -> {
                EditEntryViewModel(repository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}