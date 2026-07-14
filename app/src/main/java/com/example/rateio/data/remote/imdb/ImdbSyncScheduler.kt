package com.example.rateio.data.remote.imdb

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit


object ImdbSyncScheduler {

    private const val UNIQUE_WORK_NAME = "rateio_imdb_ratings_sync"

    fun scheduleSync(context: Context) {
        // 1. Define the strict overnight/idle constraints
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED) // Only on Wi-Fi (saves data)
            .setRequiresCharging(true)                    // Only when plugged in (saves battery)
            .setRequiresDeviceIdle(true)                  // Only when the user is NOT using the phone (overnight)
            .build()

        // 2. Create the periodic request (Repeat every 7 days)
        val syncRequest = PeriodicWorkRequestBuilder<ImdbSyncWorker>(
            1, TimeUnit.DAYS // Repeat interval
        )
            .setConstraints(constraints)
            .build()

        // 3. Enqueue the work safely
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // KEEP means: if already scheduled, don't reset the 7-day timer
            syncRequest
        )
    }
}