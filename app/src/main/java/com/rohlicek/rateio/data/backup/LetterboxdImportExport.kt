package com.rohlicek.rateio.data.backup

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import java.io.InputStream


data class LetterboxdRecord(
    val title: String,
    val year: Int?,
    val letterboxdUri: String,
    val rating: Float?, // Letterboxd uses 0.5 to 5.0 scale
    val watchDate: String?
)

fun parseLetterboxdCsv(inputStream: InputStream): List<LetterboxdRecord> {
    val records = mutableListOf<LetterboxdRecord>()

    csvReader().open(inputStream) {
        readAllWithHeaderAsSequence().forEach { row ->
            val title = row["Name"] ?: return@forEach
            val year = row["Year"]?.toIntOrNull()
            val uri = row["Letterboxd URI"] ?: ""
            val rating = row["Rating"]?.toFloatOrNull()
            val date = row["Date"]?.ifBlank { null }

            records.add(
                LetterboxdRecord(
                    title = title,
                    year = year,
                    letterboxdUri = uri,
                    rating = rating,
                    watchDate = date
                )
            )
        }
    }
    return records
}