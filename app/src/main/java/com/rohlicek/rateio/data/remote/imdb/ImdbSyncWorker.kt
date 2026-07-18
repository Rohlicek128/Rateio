package com.rohlicek.rateio.data.remote.imdb

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rohlicek.rateio.data.db.RateioDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import androidx.work.workDataOf
import com.rohlicek.rateio.data.preferences.SyncPreferences


class ImdbSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 1. Get your dependencies (Ideally use Hilt/Dagger, but this is the manual way)
            val database = RateioDatabase.getDatabase(applicationContext)
            val repository = ImdbRatingRepository(database.imdbRatingDao())
            val okHttpClient = OkHttpClient() // Or get your singleton instance
            val preferences = SyncPreferences(applicationContext)

            // 2. Run the sync method we built earlier
            syncImdbRatings(
                okHttpClient = okHttpClient,
                repository = repository,
                url = "https://datasets.imdbws.com/title.ratings.tsv.gz"
            )

            preferences.saveLastSyncTime(System.currentTimeMillis())

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}


class ManualSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return try {
            val database = RateioDatabase.getDatabase(applicationContext)
            val dao = database.imdbRatingDao()
            val preferences = SyncPreferences(applicationContext)

            // Trigger the fast sync
            fastSyncImdbRatings(
                okHttpClient = OkHttpClient(),
                database = database,
                dao = dao,
                onProgress = { progress ->
                    // Push progress state back to the UI
                    setProgress(workDataOf("PROGRESS" to progress))
                }
            )

            // Save the successful sync time
            preferences.saveLastSyncTime(System.currentTimeMillis())

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}