package com.example.watchdiary.ui.components
import com.example.watchdiary.R

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.watchdiary.data.model.ContentType
import com.example.watchdiary.data.model.DiaryEntry

@Composable
fun StatsCardRow(
    entries: List<DiaryEntry>,
    selectedType: ContentType?,
    onTypeSelected: (ContentType?) -> Unit
) {
    val movies = entries.count { it.contentType == ContentType.MOVIE }
    val series = entries.count { it.contentType == ContentType.SERIES }
    val anime = entries.count { it.contentType == ContentType.ANIME }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MiniStatCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.total),
            value = entries.size.toString(),
            selected = selectedType == null,
            onClick = { onTypeSelected(null) }
        )
        MiniStatCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.movies),
            value = movies.toString(),
            selected = selectedType == ContentType.MOVIE,
            onClick = { onTypeSelected(ContentType.MOVIE) }
        )
        MiniStatCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.series_2),
            value = series.toString(),
            selected = selectedType == ContentType.SERIES,
            onClick = { onTypeSelected(ContentType.SERIES) }
        )
        MiniStatCard(
            modifier = Modifier.weight(1f),
            title = stringResource(R.string.anime),
            value = anime.toString(),
            selected = selectedType == ContentType.ANIME,
            onClick = { onTypeSelected(ContentType.ANIME) }
        )
    }
}

@Composable
private fun MiniStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
