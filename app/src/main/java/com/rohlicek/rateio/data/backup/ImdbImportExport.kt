package com.rohlicek.rateio.data.backup

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.InputStream


data class ImdbRecord(
    val constId: String,
    val title: String,
    val year: Int?,
    val rating: Int?, // 1.0 - 10.0
    val type: String?,
    val dateRated: String?
)

fun parseImdbCsv(inputStream: InputStream): List<ImdbRecord> {
    val records = mutableListOf<ImdbRecord>()

    csvReader().open(inputStream) {
        readAllWithHeaderAsSequence().forEach { row ->
            val constId = row["Const"] ?: return@forEach
            records.add(
                ImdbRecord(
                    constId = constId,
                    title = row["Title"] ?: "Unknown",
                    year = row["Year"]?.toIntOrNull(),
                    rating = row["Your Rating"]?.toIntOrNull(),
                    type = row["Title Type"]?.ifBlank { null },
                    dateRated = row["Date Rated"]?.ifBlank { null }
                )
            )
        }
    }
    return records
}