package com.example.watchdiary.ui.components
import com.example.watchdiary.R
import androidx.compose.ui.platform.LocalContext

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.watchdiary.ui.theme.StarColor

@Composable
fun RatingBar(
    rating: Int,
    onRatingChanged: ((Int) -> Unit)? = null,
    maxStars: Int = 5
) {
    val context = LocalContext.current
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (i in 1..maxStars) {
            val filled = i <= rating
            Icon(
                imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = context.getString(R.string.star_number, i),
                tint = if (filled) StarColor else MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(24.dp)
                    .then(
                        if (onRatingChanged != null) {
                            Modifier.clickable { onRatingChanged(i) }
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}
