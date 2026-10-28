package com.example.watchdiary.ui.screens
import com.example.watchdiary.R

import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.watchdiary.data.model.ContentType
import com.example.watchdiary.ui.components.RatingBar
import com.example.watchdiary.ui.components.SectionCard
import com.example.watchdiary.util.DateUtils
import com.example.watchdiary.util.AppError
import com.example.watchdiary.viewmodel.AddEntryViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import java.time.LocalDate
import java.io.ByteArrayOutputStream

private const val MAX_JSON_IMPORT_BYTES = 2 * 1024 * 1024
private class ImportTooLargeException : Exception()

@Composable
fun AddScreen(
    viewModel: AddEntryViewModel,
    paddingValues: PaddingValues
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val localSnackbarHostState = remember { SnackbarHostState() }
    val jsonPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                try {
                    val json = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            val output = ByteArrayOutputStream()
                            val buffer = ByteArray(8192)
                            var totalBytes = 0
                            while (true) {
                                val bytesRead = input.read(buffer)
                                if (bytesRead == -1) break
                                totalBytes += bytesRead
                                if (totalBytes > MAX_JSON_IMPORT_BYTES) throw ImportTooLargeException()
                                output.write(buffer, 0, bytesRead)
                            }
                            output.toString(Charsets.UTF_8.name())
                        } ?: error("No input stream")
                    }
                    viewModel.importFromJson(json) { message ->
                        coroutineScope.launch { localSnackbarHostState.showSnackbar(message.resolve(context)) }
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: ImportTooLargeException) {
                    localSnackbarHostState.showSnackbar(context.getString(R.string.import_file_too_large))
                } catch (e: Exception) {
                    localSnackbarHostState.showSnackbar(AppError.FILE_READ.message().resolve(context))
                }
            }
        }
    }

    var expanded by remember { mutableStateOf(false) }
    var fieldWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    var importText by remember { mutableStateOf("") }
    var showGuideDialog by remember { mutableStateOf(false) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            localSnackbarHostState.showSnackbar(
                message = context.getString(R.string.entry_saved),
                duration = SnackbarDuration.Short
            )
            viewModel.consumeSaveSuccess()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            localSnackbarHostState.showSnackbar(
                message = it.resolve(context),
                duration = SnackbarDuration.Short
            )
        }
    }

    val today = LocalDate.now()
    val dateToShow = uiState.watchDate ?: today

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            viewModel.updateWatchDate(LocalDate.of(year, month + 1, dayOfMonth))
        },
        dateToShow.year,
        dateToShow.monthValue - 1,
        dateToShow.dayOfMonth
    )

    val nonBlankImportLines = remember(importText) {
        importText.lines().map { it.trim() }.filter { it.isNotBlank() }
    }
    val importLinesCount = nonBlankImportLines.size

    if (showGuideDialog) {
        AlertDialog(
            onDismissRequest = { showGuideDialog = false },
            title = { Text(context.getString(R.string.how_to_import_a_list)) },
            text = {
                Text(
                    context.getString(R.string.import_guide_text)
                )
            },
            confirmButton = {
                TextButton(onClick = { showGuideDialog = false }) {
                    Text(context.getString(R.string.close))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("import_prompt", context.getString(R.string.import_prompt_text))
                        clipboard.setPrimaryClip(clip)

                        coroutineScope.launch {
                            localSnackbarHostState.showSnackbar(
                                message = context.getString(R.string.prompt_copied),
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                ) {
                    Text(context.getString(R.string.copy_prompt))
                }
            }
        )
    }

    if (showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showImportConfirmDialog = false },
            title = { Text(context.getString(R.string.confirm_import)) },
            text = {
                Text(
                    if (importLinesCount > 0) {
                        context.getString(R.string.lines_to_process, importLinesCount)
                    } else {
                        context.getString(R.string.import_field_is_empty)
                    }
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportConfirmDialog = false
                        viewModel.importFromText(importText) { message ->
                            coroutineScope.launch {
                                localSnackbarHostState.showSnackbar(
                                    message = message.resolve(context),
                                    duration = SnackbarDuration.Short
                                )
                            }
                        }
                    }
                ) {
                    Text(context.getString(R.string.import_2))
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmDialog = false }) {
                    Text(context.getString(R.string.cancel))
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = context.getString(R.string.add_content),
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(14.dp))

            SectionCard {
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = viewModel::updateTitle,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(context.getString(R.string.title)) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = context.getString(R.string.content_type),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Box {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .onGloballyPositioned { coordinates ->
                                fieldWidth = with(density) { coordinates.size.width.toDp() }
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                        ),
                        onClick = { expanded = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.contentType.let { context.getString(it.labelRes) },
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Outlined.KeyboardArrowDown,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.width(fieldWidth)
                    ) {
                        ContentType.values().forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.let { context.getString(it.labelRes) }) },
                                onClick = {
                                    viewModel.updateContentType(type)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = context.getString(R.string.watch_date),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = { datePickerDialog.show() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.watchDate?.let { DateUtils.format(it) }
                            ?: context.getString(R.string.not_selected_today_s_date_will_be_used)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = context.getString(R.string.rating),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                RatingBar(
                    rating = uiState.rating,
                    onRatingChanged = { viewModel.updateRating(it) }
                )

                Spacer(modifier = Modifier.height(18.dp))

                OutlinedTextField(
                    value = uiState.comment,
                    onValueChange = viewModel::updateComment,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(context.getString(R.string.comment)) },
                    minLines = 5,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.saveEntry() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(context.getString(R.string.save))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SectionCard {
                Text(
                    text = context.getString(R.string.import_label),
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { jsonPicker.launch(arrayOf("application/json", "text/plain", "*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(context.getString(R.string.import_from_json_file))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = context.getString(R.string.select_a_watchdiary_backup_file_duplicate_entries_wi),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = context.getString(R.string.format_title_anime_movie_series_yyyy_mm_dd_0_5_comme),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = importText,
                    onValueChange = { importText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(context.getString(R.string.paste_entries_to_import)) },
                    minLines = 6,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = context.getString(R.string.nonempty_lines_count, importLinesCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = { importText = context.getString(R.string.import_example_text) },
                        label = { Text(context.getString(R.string.example)) }
                    )

                    AssistChip(
                        onClick = { importText = "" },
                        label = { Text(context.getString(R.string.clear_2)) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (importLinesCount == 0) {
                            coroutineScope.launch {
                                localSnackbarHostState.showSnackbar(
                                    message = context.getString(R.string.import_field_is_empty_2),
                                    duration = SnackbarDuration.Short
                                )
                            }
                        } else {
                            showImportConfirmDialog = true
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(context.getString(R.string.import_from_text))
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = { showGuideDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(context.getString(R.string.how_to_prepare_an_import_list))
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        SnackbarHost(
            hostState = localSnackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .navigationBarsPadding()
        )
    }
}
