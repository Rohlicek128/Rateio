package com.example.rateio.data.db

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(
    entities = [
        CategoryEntity::class,
        RateItemEntity::class
    ],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 2, to = 3)
    ],
    exportSchema = true,
)
abstract class RateioDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun rateItemDao(): RateItemDao

    companion object {
        @Volatile
        private var INSTANCE: RateioDatabase? = null

        fun getDatabase(context: Context): RateioDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RateioDatabase::class.java,
                    "rateio"
                ).fallbackToDestructiveMigration(true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}