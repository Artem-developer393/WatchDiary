package com.example.watchdiary.ui.components
import androidx.compose.ui.platform.LocalContext
import com.example.watchdiary.R

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.watchdiary.data.model.CommentFilter
import com.example.watchdiary.data.model.ContentType
import com.example.watchdiary.data.model.SortOption
import com.example.watchdiary.util.DateUtils
import java.time.LocalDate

@Composable
fun FilterSection(
    selectedType: ContentType?,
    selectedRating: Int?,
    selectedDate: LocalDate?,
    commentFilter: CommentFilter,
    sortOption: SortOption,
    onTypeChange: (ContentType?) -> Unit,
    onRatingChange: (Int?) -> Unit,
    onDatePick: () -> Unit,
    onClearDate: () -> Unit,
    onCommentFilterChange: (CommentFilter) -> Unit,
    onSortChange: (SortOption) -> Unit,
    onResetFilters: () -> Unit,
    onApply: () -> Unit
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = context.getString(R.string.filters_and_sorting),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )

        FilterLabel(context.getString(R.string.content_type))
        FilterChipRow {
            FilterChip(
                text = context.getString(R.string.all),
                selected = selectedType == null,
                onClick = { onTypeChange(null) }
            )

            ContentType.values().forEach { type ->
                FilterChip(
                    text = type.let { context.getString(it.labelRes) },
                    selected = selectedType == type,
                    onClick = { onTypeChange(type) }
                )
            }
        }

        FilterLabel(context.getString(R.string.rating_2))
        FilterChipRow {
            FilterChip(
                text = context.getString(R.string.any),
                selected = selectedRating == null,
                onClick = { onRatingChange(null) }
            )

            (0..5).forEach { rating ->
                FilterChip(
                    text = "$rating★",
                    selected = selectedRating == rating,
                    onClick = { onRatingChange(rating) }
                )
            }
        }

        FilterLabel(context.getString(R.string.watch_date))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                text = selectedDate?.let { DateUtils.format(it) } ?: context.getString(R.string.select_date),
                selected = selectedDate != null,
                onClick = onDatePick
            )

            if (selectedDate != null) {
                OutlinedButton(onClick = onClearDate) {
                    Text(context.getString(R.string.clear))
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f)) {
                DropdownFilterField(
                    label = context.getString(R.string.comment),
                    value = commentFilter.let { context.getString(it.labelRes) },
                    items = CommentFilter.values().toList(),
                    itemText = { it.let { context.getString(it.labelRes) } },
                    onItemSelected = onCommentFilterChange
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                DropdownFilterField(
                    label = context.getString(R.string.sort),
                    value = sortOption.let { context.getString(it.labelRes) },
                    items = SortOption.values().toList(),
                    itemText = { it.let { context.getString(it.labelRes) } },
                    onItemSelected = onSortChange
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onResetFilters,
                modifier = Modifier.weight(1f)
            ) {
                Text(context.getString(R.string.reset))
            }

            Button(
                onClick = onApply,
                modifier = Modifier.weight(1f)
            ) {
                Text(context.getString(R.string.done))
            }
        }
    }
}

@Composable
private fun FilterLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun FilterChipRow(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        content()
    }
}

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        },
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun <T> DropdownFilterField(
    label: String,
    value: String,
    items: List<T>,
    itemText: (T) -> String,
    onItemSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var fieldWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Box {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        fieldWidth = with(density) { coordinates.size.width.toDp() }
                    }
                    .clickable { expanded = true },
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = value,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    androidx.compose.material3.Icon(
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
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(itemText(item)) },
                        onClick = {
                            onItemSelected(item)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
