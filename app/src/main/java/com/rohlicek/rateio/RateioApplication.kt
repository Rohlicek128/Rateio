package com.rohlicek.rateio

import android.app.Application
import com.rohlicek.rateio.data.preferences.SyncPreferences
import com.rohlicek.rateio.data.remote.imdb.ImdbSyncScheduler
import com.rohlicek.rateio.data.remote.tmdb.TmdbClient


class RateioApplication : Application() {

    lateinit var syncPreferences: SyncPreferences
        private set

    lateinit var tmdbClient: TmdbClient
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        ImdbSyncScheduler.scheduleSync(this)

        syncPreferences = SyncPreferences(applicationContext)
        tmdbClient = TmdbClient(syncPreferences)
    }

    companion object {
        lateinit var instance: RateioApplication
            private set
    }
}