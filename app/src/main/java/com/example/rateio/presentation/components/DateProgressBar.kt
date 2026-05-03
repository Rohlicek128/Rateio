package com.example.rateio.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rateio.utils.daysUntil
import com.example.rateio.utils.formatDate
import com.example.rateio.utils.parseDate
import java.time.LocalDate
import java.time.temporal.ChronoUnit


fun dateProgress(startDate: LocalDate, endDate: LocalDate, currentDate: LocalDate): Float {
    if (currentDate >= endDate) return 1f

    val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toFloat()
    val elapsedDays = ChronoUnit.DAYS.between(startDate, currentDate).toFloat()

    return (elapsedDays / totalDays).coerceIn(0f, 1f)
}

@Composable
fun DateProgressBar(
    startDateString: String?,
    endDateString: String?,
    modifier: Modifier = Modifier,
    todayDate: LocalDate = LocalDate.now(),
) {
    val startDate = remember(startDateString) { parseDate(startDateString) }
    val endDate = remember(endDateString) { parseDate(endDateString) }

    if (endDate == null) return

    val isDone = todayDate >= endDate
    val daysLeft = daysUntil(endDate)

    val progress = remember(endDate, startDate) {
        if (startDate != null) {
            dateProgress(startDate, endDate, todayDate)
        } else {
            if (isDone) 1f else 0f
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatDate(startDate),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = when {
                    todayDate > endDate -> "Aired"
                    todayDate == endDate -> "Airing"
                    else -> "Airs in $daysLeft days"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatDate(endDate),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LinearWavyProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            waveSpeed = 10.dp,
            wavelength = 22.dp,
        )
    }
}