package com.example.rateio.data.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(
    entities = [
        CategoryEntity::class,
        RateItemEntity::class,
        ImdbRatingEntity::class
    ],
    version = 5,
    autoMigrations = [
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
    ],
    exportSchema = true,
)
abstract class RateioDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun rateItemDao(): RateItemDao
    abstract fun imdbRatingDao(): ImdbRatingDao

    companion object {
        @Volatile
        private var INSTANCE: RateioDatabase? = null

        fun getDatabase(context: Context): RateioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RateioDatabase::class.java,
                    "rateio"
                )
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}