package com.example.watchdiary.ui.screens
import androidx.compose.ui.platform.LocalContext
import com.example.watchdiary.R

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.expandVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.watchdiary.ui.components.RatingBar
import com.example.watchdiary.ui.components.SectionCard
import com.example.watchdiary.util.DateUtils
import com.example.watchdiary.viewmodel.DetailsViewModel

@Composable
fun DetailsScreen(
    entryId: Int,
    viewModel: DetailsViewModel,
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onDeletedOrBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(entryId) {
        viewModel.loadEntry(entryId)
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(context.getString(R.string.delete_entry_2)) },
            text = { Text(context.getString(R.string.the_entry_will_be_removed_from_the_diary)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEntryById(entryId, onDeletedOrBack)
                        showDeleteDialog = false
                    }
                ) {
                    Text(context.getString(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(context.getString(R.string.cancel))
                }
            }
        )
    }

    val entry = uiState.entry

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = context.getString(R.string.back)
                )
            }

            Text(
                text = context.getString(R.string.entry_details),
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        uiState.errorMessage?.let { message ->
            Text(message.resolve(context), color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }

            entry == null -> {
                SectionCard {
                    Text(uiState.errorMessage?.resolve(context) ?: context.getString(R.string.entry_not_found))
                }
            }

            else -> {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically()
                ) {
                    SectionCard {
                        Text(
                            text = entry.title,
                            style = MaterialTheme.typography.headlineSmall
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailTag(context.getString(R.string.entry_type_label, context.getString(entry.contentType.labelRes)))
                            DetailTag(context.getString(R.string.entry_date_label, DateUtils.format(entry.watchDate)))
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = context.getString(R.string.rating_2),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        RatingBar(rating = entry.rating)

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = context.getString(R.string.comment),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (entry.comment.isBlank()) context.getString(R.string.no_comment) else entry.comment,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(onClick = { showDeleteDialog = true }) {
                            Text(context.getString(R.string.delete_entry))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailTag(text: String) {
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium
        )
    }
}
