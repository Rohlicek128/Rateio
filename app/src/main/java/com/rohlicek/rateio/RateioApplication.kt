package com.rohlicek.rateio

import android.app.Application
import com.rohlicek.rateio.data.remote.imdb.ImdbSyncScheduler


class RateioApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        ImdbSyncScheduler.scheduleSync(this)
    }
}