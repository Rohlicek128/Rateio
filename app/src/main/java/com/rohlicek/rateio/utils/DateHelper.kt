package com.rohlicek.rateio.utils

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale


fun parseDate(dateString: String?): LocalDate? {
    if (dateString.isNullOrBlank()) return null
    val local = runCatching {
        LocalDate.parse(dateString)
    }.getOrNull()

    if (local != null) return local

    val instant = runCatching {
        Instant.parse(dateString)
    }.getOrNull()
    return instant?.atZone(ZoneId.systemDefault())?.toLocalDate()
}
fun parseDate(timeMillis: Long): LocalDate? {
    val instant = Instant.ofEpochMilli(timeMillis)
    return instant.atZone(ZoneId.systemDefault()).toLocalDate()
}


fun daysUntil(targetDate: LocalDate): Long {
    return ChronoUnit.DAYS.between(
        LocalDate.now(),
        targetDate).coerceAtLeast(0)
}
fun hoursUntil(targetDate: LocalDate): Long {
    return ChronoUnit.HOURS.between(
        LocalDateTime.now(),
        targetDate.atStartOfDay()).coerceAtLeast(0)
}

fun formatDate(dateString: String?, locale: Locale = Locale.ENGLISH, pattern: String = "MMMM d, yyyy"): String {
    val date = parseDate(dateString) ?: return "N/A"
    return date.format(DateTimeFormatter.ofPattern(pattern, locale))
}
fun formatDate(date: LocalDate?, locale: Locale = Locale.ENGLISH, pattern: String = "MMMM d, yyyy"): String {
    if (date == null) return "N/A"
    return date.format(DateTimeFormatter.ofPattern(pattern, locale))
}

fun formatDateCompact(dateString: String?, locale: Locale = Locale.ENGLISH): String {
    val date = parseDate(dateString) ?: return "N/A"
    return date.format(DateTimeFormatter.ofPattern("MMM. d, yyyy", locale))
}
fun formatDateCompact(date: LocalDate?, locale: Locale = Locale.ENGLISH): String {
    if (date == null) return "N/A"
    return date.format(DateTimeFormatter.ofPattern("MMM. d, yyyy", locale))
}