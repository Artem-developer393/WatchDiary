package com.example.watchdiary.ui.screens
import com.example.watchdiary.R
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import androidx.core.content.FileProvider
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.watchdiary.data.model.CommentFilter
import com.example.watchdiary.data.export.ExportManager
import com.example.watchdiary.data.model.DiaryEntry
import com.example.watchdiary.ui.components.EntryCard
import com.example.watchdiary.ui.components.FilterSection
import com.example.watchdiary.ui.components.SectionCard
import com.example.watchdiary.ui.components.StatsCardRow
import com.example.watchdiary.util.DateProvider
import com.example.watchdiary.util.AppError
import com.example.watchdiary.util.DateUtils
import com.example.watchdiary.viewmodel.ListViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    viewModel: ListViewModel,
    exportManager: ExportManager,
    paddingValues: PaddingValues,
    snackbarHostState: SnackbarHostState,
    onEntryClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    fun showMessage(message: String) {
        coroutineScope.launch { snackbarHostState.showSnackbar(message) }
    }
    val savePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) coroutineScope.launch {
            val message = try {
                exportManager.exportToUri(uri)
                context.getString(R.string.file_saved)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                AppError.FILE_WRITE.message().resolve(context)
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    var showFiltersSheet by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    var entryToDelete by remember { mutableStateOf<DiaryEntry?>(null) }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it.resolve(context))
            viewModel.consumeError()
        }
    }

    val dateBase = uiState.selectedDate ?: DateProvider.today()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            viewModel.updateDate(DateProvider.of(year, month + 1, dayOfMonth))
        },
        dateBase.year,
        dateBase.monthValue - 1,
        dateBase.dayOfMonth
    )

    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text(context.getString(R.string.delete_entry_2)) },
            text = { Text(context.getString(R.string.this_action_cannot_be_undone)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val deletedEntry = entryToDelete
                        entryToDelete = null

                        if (deletedEntry != null) coroutineScope.launch {
                            if (!viewModel.deleteEntry(deletedEntry)) return@launch
                            val result = snackbarHostState.showSnackbar(
                                message = context.getString(R.string.deleted_title, deletedEntry.title),
                                actionLabel = context.getString(R.string.undo),
                                duration = SnackbarDuration.Short
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                viewModel.restoreDeletedEntry(deletedEntry)
                            }
                        }
                    }
                ) {
                    Text(context.getString(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) {
                    Text(context.getString(R.string.cancel))
                }
            }
        )
    }

    if (showFiltersSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFiltersSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                FilterSection(
                    selectedType = uiState.selectedType,
                    selectedRating = uiState.selectedRating,
                    selectedDate = uiState.selectedDate,
                    commentFilter = uiState.commentFilter,
                    sortOption = uiState.sortOption,
                    onTypeChange = viewModel::updateType,
                    onRatingChange = viewModel::updateRating,
                    onDatePick = { datePickerDialog.show() },
                    onClearDate = { viewModel.updateDate(null) },
                    onCommentFilterChange = viewModel::updateCommentFilter,
                    onSortChange = viewModel::updateSortOption,
                    onResetFilters = viewModel::resetFilters,
                    onApply = { showFiltersSheet = false }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    LazyColumn(
        modifier = Modifier.padding(paddingValues),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text(
                    text = context.getString(R.string.diary_screen_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = {
                    val current = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "ru"
                    val next = if (current == "ru") "en" else "ru"
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(next))
                }) {
                    val current = AppCompatDelegate.getApplicationLocales().get(0)?.language ?: "ru"
                    Text(if (current == "ru") "EN" else "RU")
                }
                Box {
                    IconButton(onClick = { showExportMenu = true }) {
                        Icon(Icons.Outlined.IosShare, contentDescription = context.getString(R.string.export_json))
                    }
                    DropdownMenu(expanded = showExportMenu, onDismissRequest = { showExportMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(context.getString(R.string.save_to_downloads)) },
                            onClick = {
                                showExportMenu = false
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    coroutineScope.launch {
                                        try {
                                            val filename = exportManager.saveToDownloads()
                                            showMessage(context.getString(R.string.saved_to_downloads, filename))
                                        } catch (e: CancellationException) {
                                            throw e
                                        } catch (e: Exception) {
                                            showMessage(AppError.FILE_WRITE.message().resolve(context))
                                        }
                                    }
                                } else {
                                    savePicker.launch("watch_diary_export.json")
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(context.getString(R.string.share_json)) },
                            onClick = {
                                showExportMenu = false
                                coroutineScope.launch {
                                    try {
                                        val file = exportManager.exportToJson()
                                        val uri = FileProvider.getUriForFile(
                                            context, "${context.packageName}.provider", file
                                        )
                                        val share = Intent(Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(Intent.EXTRA_STREAM, uri)
                                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(Intent.createChooser(share, context.getString(R.string.share_json)))
                                    } catch (e: CancellationException) {
                                        throw e
                                    } catch (e: Exception) {
                                        showMessage(AppError.EXPORT.message().resolve(context))
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        item {
            StatsCardRow(
                entries = uiState.allEntries,
                selectedType = uiState.selectedType,
                onTypeSelected = viewModel::updateType
            )
        }

        item {
            SectionCard {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = viewModel::updateSearchQuery,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(context.getString(R.string.search_by_title)) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = null
                        )
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { showFiltersSheet = true },
                        label = { Text(context.getString(R.string.filters)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.FilterList,
                                contentDescription = null
                            )
                        }
                    )

                    AssistChip(
                        onClick = { viewModel.resetFilters() },
                        label = { Text(context.getString(R.string.reset)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = null
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val selectedType = uiState.selectedType
                val selectedRating = uiState.selectedRating
                val selectedDate = uiState.selectedDate
                val commentFilter = uiState.commentFilter

                AnimatedVisibility(
                    visible = selectedType != null ||
                            selectedRating != null ||
                            selectedDate != null ||
                            commentFilter != CommentFilter.ALL,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (selectedType != null) {
                            ActiveFilterTag(text = selectedType.let { context.getString(it.labelRes) })
                        }
                        if (selectedRating != null) {
                            ActiveFilterTag(text = "$selectedRating★")
                        }
                        if (selectedDate != null) {
                            ActiveFilterTag(text = DateUtils.format(selectedDate))
                        }
                        if (commentFilter != CommentFilter.ALL) {
                            ActiveFilterTag(text = commentFilter.let { context.getString(it.labelRes) })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                ActiveFilterTag(text = context.getString(R.string.found_count, uiState.visibleEntries.size))
            }
        }

        when {
            uiState.isLoading -> {
                item {
                    CircularProgressIndicator()
                }
            }

            uiState.visibleEntries.isEmpty() -> {
                item {
                    SectionCard {
                        Text(
                            text = if (uiState.allEntries.isEmpty()) {
                                context.getString(R.string.no_entries_yet)
                            } else {
                                context.getString(R.string.no_entries_match_the_filters)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (uiState.allEntries.isEmpty()) {
                                context.getString(R.string.tap_add_to_create_your_first_diary_entry)
                            } else {
                                context.getString(R.string.try_changing_your_search_or_clearing_the_filters)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                items(
                    items = uiState.visibleEntries,
                    key = { it.id }
                ) { entry ->
                    EntryCard(
                        entry = entry,
                        onClick = { onEntryClick(entry.id) },
                        onEditClick = { onEditClick(entry.id) },
                        onDeleteClick = { entryToDelete = entry }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(90.dp))
        }
    }
}

@Composable
private fun ActiveFilterTag(text: String) {
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
