package com.example.rateio.data.remote.imdb

import androidx.sqlite.db.SimpleSQLiteQuery
import com.example.rateio.data.db.ImdbRatingDao
import com.example.rateio.data.db.ImdbRatingEntity
import com.example.rateio.data.db.RateioDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.zip.GZIPInputStream


suspend fun syncImdbRatings(
    okHttpClient: OkHttpClient,
    repository: ImdbRatingRepository,
    url: String,
) {
    withContext(Dispatchers.IO) {
        val request = Request.Builder().url(url).build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Failed to download file")

            val inputStream = response.body?.byteStream() ?: return@withContext

            // Wrap the stream in GZIPInputStream to decompress on the fly
            GZIPInputStream(inputStream).bufferedReader().use { reader ->
                reader.use { bufferedReader ->
                    val batchSize = 10_000
                    val batch = mutableListOf<ImdbRatingEntity>()

                    // Skip the TSV header row
                    bufferedReader.readLine()

                    var line: String? = bufferedReader.readLine()
                    while (line != null) {
                        val parts = line.split("\t")
                        if (parts.size >= 3) {
                            val tconst = parts[0]
                            val rawRating = parts[1].toFloatOrNull() ?: 0f
                            val votes = parts[2].toIntOrNull() ?: 0

                            batch.add(
                                ImdbRatingEntity(
                                    tconst = tconst,
                                    averageRating = rawRating / 10.0f, // Normalize to 0.0 - 1.0
                                    numVotes = votes
                                )
                            )

                            // Insert batch and clear
                            if (batch.size >= batchSize) {
                                repository.insertRatings(batch)
                                batch.clear()
                            }
                        }
                        // Read the next line
                        line = bufferedReader.readLine()
                    }

                    // Insert any remaining items left over in the final batch
                    if (batch.isNotEmpty()) {
                        repository.insertRatings(batch)
                    }
                }
            }
        }
    }
}


suspend fun fastSyncImdbRatings(
    okHttpClient: OkHttpClient,
    database: RateioDatabase,
    dao: ImdbRatingDao,
    onProgress: suspend (Int) -> Unit // Callback to track progress (0 to 100)
) {
    val url = "https://datasets.imdbws.com/title.ratings.tsv.gz"

    // 1. Temporarily relax SQLite safety limits for massive speed boosts
    database.query(SimpleSQLiteQuery("PRAGMA synchronous = OFF"))
    database.query(SimpleSQLiteQuery("PRAGMA journal_mode = MEMORY"))

    val request = Request.Builder().url(url).build()

    okHttpClient.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw Exception("Network failed")

        val body = response.body ?: return
        val totalBytes = body.contentLength() // Size of the compressed GZ file (~7MB)
        val inputStream = body.byteStream()

        // Wrap the stream to count bytes as we download for accurate progress estimation
        var bytesRead = 0L
        val progressStream = object : java.io.FilterInputStream(inputStream) {
            override fun read(): Int {
                val b = super.read()
                if (b != -1) bytesRead++
                return b
            }
            override fun read(b: ByteArray, off: Int, len: Int): Int {
                val n = super.read(b, off, len)
                if (n != -1) bytesRead += n
                return n
            }
        }

        GZIPInputStream(progressStream).bufferedReader().use { reader ->
            reader.readLine() // Skip header

            val batchSize = 15_000 // Slightly larger batch size for raw insertion speed
            val batch = mutableListOf<ImdbRatingEntity>()

            var line: String? = reader.readLine()
            while (line != null) {
                val parts = line.split("\t")
                if (parts.size >= 3) {
                    val tconst = parts[0]
                    val rawRating = parts[1].toFloatOrNull() ?: 0f
                    val votes = parts[2].toIntOrNull() ?: 0

                    batch.add(
                        ImdbRatingEntity(
                            tconst = tconst,
                            averageRating = rawRating / 10.0f,
                            numVotes = votes
                        )
                    )

                    if (batch.size >= batchSize) {
                        dao.insertRatings(batch)
                        batch.clear()

                        // Calculate percentage based on zipped download stream progress
                        if (totalBytes > 0) {
                            val percentage = ((bytesRead * 100) / totalBytes).toInt()
                            onProgress(percentage.coerceIn(0, 99)) // reserve 100% for the actual end
                        }
                    }
                }
                line = reader.readLine()
            }
            if (batch.isNotEmpty()) {
                dao.insertRatings(batch)
            }
        }
    }

    // 2. Restore SQLite safety settings! (Crucial to prevent future DB corruption)
    database.query(SimpleSQLiteQuery("PRAGMA synchronous = NORMAL"))
    database.query(SimpleSQLiteQuery("PRAGMA journal_mode = WAL"))
    onProgress(100)
}