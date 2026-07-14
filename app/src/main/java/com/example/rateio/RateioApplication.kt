package com.example.rateio

import android.app.Application
import com.example.rateio.data.remote.imdb.ImdbSyncScheduler


class RateioApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        ImdbSyncScheduler.scheduleSync(this)
    }
}