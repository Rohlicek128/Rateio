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

    @Query("""
    SELECT * FROM rate_items 
    WHERE parentId IN (SELECT id FROM rate_items WHERE parentId = :parentId)
""")
    fun observeGrandchildren(parentId: Long): Flow<List<RateItemEntity>>



    @Query("SELECT * FROM rate_items WHERE id = :id")
    suspend fun getById(id: Long): RateItemEntity?

    @Query("SELECT * FROM rate_items WHERE externalId = :externalId AND categoryId = :categoryId LIMIT 1")
    suspend fun getByExternalId(externalId: String, categoryId: Long): RateItemEntity?

    @Query("""
    SELECT parent.* FROM rate_items AS child
    INNER JOIN rate_items AS parent ON child.parentId = parent.id
    WHERE child.id = :childId
""")
    suspend fun getParent(childId: Long): RateItemEntity?



    @Query("SELECT COUNT(*) FROM rate_items WHERE categoryId = :categoryId AND parentId IS NULL")
    fun observeRootItemCount(categoryId: Long): Flow<Int>

    @Query("SELECT categoryId, COUNT(*) as count FROM rate_items WHERE parentId IS NULL GROUP BY categoryId")
    fun observeRootItemCounts(): Flow<List<CategoryCount>>

    @Query("SELECT COUNT(*) FROM rate_items WHERE rating IS NOT NULL")
    fun observeRatedItemCount(): Int


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: RateItemEntity): Long

    @Update
    suspend fun update(item: RateItemEntity)

    @Delete
    suspend fun delete(item: RateItemEntity)

    @Query("""
    DELETE FROM rate_items
    WHERE parentId IS NOT NULL 
    AND externalSource = (
        SELECT p.externalSource 
        FROM rate_items AS p 
        WHERE p.id = rate_items.parentId
    )
""")
    suspend fun deleteChildrenWithMatchingSource()

    @Query("""
    DELETE FROM rate_items 
    WHERE parentId IS NOT NULL 
    AND parentId NOT IN (SELECT id FROM rate_items)
""")
    suspend fun deleteOrphanRateItems()

    @Query("""
    DELETE FROM rate_items 
    WHERE parentId = :id or id = :id
""")
    suspend fun deleteId(id: Long)


    @Query("UPDATE rate_items SET rating = :rating, updatedAt = :now WHERE id = :id")
    suspend fun updateRating(id: Long, rating: Float, now: Long = System.currentTimeMillis())

    @Query("UPDATE rate_items SET status = :status, updatedAt = :now WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String, now: Long = System.currentTimeMillis())
}

data class CategoryCount(
    val categoryId: Long,
    val count: Int,
)

data class EpisodeRatingRow(
    val seasonNum: Int,
    val episodeNum: Int,
    val rating: Float?
)