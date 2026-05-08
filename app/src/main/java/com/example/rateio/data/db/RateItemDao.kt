package com.example.rateio.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface RateItemDao {
    @Query("""
        SELECT * FROM rate_items 
        WHERE categoryId = :categoryId AND parentId IS NULL 
        ORDER BY createdAt DESC
    """)
    fun observeRootItems(categoryId: Long): Flow<List<RateItemEntity>>

    @Query("SELECT * FROM rate_items WHERE parentId = :parentId ORDER BY subtitle ASC")
    fun observeChildren(parentId: Long): Flow<List<RateItemEntity>>


    @Query("SELECT * FROM rate_items WHERE id = :id")
    suspend fun getById(id: Long): RateItemEntity?

    @Query("SELECT * FROM rate_items WHERE externalId = :externalId AND categoryId = :categoryId")
    suspend fun getByExternalId(externalId: String, categoryId: Long): RateItemEntity?


    @Query("SELECT COUNT(*) FROM rate_items WHERE categoryId = :categoryId AND parentId IS NULL")
    fun observeRootItemCount(categoryId: Long): Flow<Int>

    @Query("SELECT categoryId, COUNT(*) as count FROM rate_items WHERE parentId IS NULL GROUP BY categoryId")
    fun observeRootItemCounts(): Flow<List<CategoryCount>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RateItemEntity): Long

    @Update
    suspend fun update(item: RateItemEntity)

    @Delete
    suspend fun delete(item: RateItemEntity)


    @Query("UPDATE rate_items SET rating = :rating, updatedAt = :now WHERE id = :id")
    suspend fun updateRating(id: Long, rating: Float, now: Long = System.currentTimeMillis())

    @Query("UPDATE rate_items SET status = :status, updatedAt = :now WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, now: Long = System.currentTimeMillis())
}

data class CategoryCount(
    val categoryId: Long,
    val count: Int,
)