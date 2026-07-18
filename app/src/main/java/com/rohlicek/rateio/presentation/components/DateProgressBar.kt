package com.rohlicek.rateio.presentation.components

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
import com.rohlicek.rateio.utils.formatDateCompact
import com.rohlicek.rateio.utils.hoursUntil
import com.rohlicek.rateio.utils.parseDate
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.floor


fun dateProgress(startDate: LocalDate, endDate: LocalDate, currentDateTime: LocalDateTime = LocalDateTime.now()): Float {
    val startDateTime = startDate.atStartOfDay()
    val endDateTime = endDate.atStartOfDay()
    if (currentDateTime >= endDateTime) return 1f

    val totalHours = ChronoUnit.HOURS.between(startDateTime, endDateTime).toFloat()
    val elapsedHours = ChronoUnit.HOURS.between(startDateTime, currentDateTime).toFloat()

    return (elapsedHours / totalHours).coerceIn(0f, 1f)
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

    var hoursLeft = hoursUntil(endDate)
    val daysLeft = floor(hoursLeft.toFloat() / 24f).toLong()
    hoursLeft = (hoursLeft % 24f).toLong()

    val progress = remember(endDate, startDate) {
        if (startDate != null) {
            dateProgress(startDate, endDate)
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
                text = formatDateCompact(startDate),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = when {
                    todayDate > endDate -> "Aired"
                    todayDate == endDate -> "Airs Today"
                    else -> "Airs in ${if (daysLeft > 0L) "$daysLeft day${if (daysLeft != 1L) "s" else ""}, " else ""}$hoursLeft hour${if (hoursLeft != 1L) "s" else ""}"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatDateCompact(endDate),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        LinearWavyProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            waveSpeed = 10.dp,
            wavelength = 22.dp,
        )
    }
}