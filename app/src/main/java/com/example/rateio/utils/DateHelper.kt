package com.example.rateio.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale


fun parseDate(dateString: String?): LocalDate? {
    if (dateString.isNullOrBlank()) return null
    return runCatching { LocalDate.parse(dateString) }.getOrNull()
}

fun daysUntil(targetDate: LocalDate): Long {
    return ChronoUnit.DAYS.between(LocalDate.now(), targetDate).coerceAtLeast(0)
}

fun formatDate(dateString: String?, locale: Locale = Locale.ENGLISH): String {
    val date = parseDate(dateString) ?: return "N/A"

    return date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", locale))
}
fun formatDate(date: LocalDate?, locale: Locale = Locale.ENGLISH): String {
    if (date == null) return "N/A"

    return date.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", locale))
}