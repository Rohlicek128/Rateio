package com.example.rateio.data.repository

import com.example.rateio.data.db.RateItemDao
import com.example.rateio.data.db.RateItemEntity
import com.example.rateio.data.db.toDomain
import com.example.rateio.data.db.toEntity
import com.example.rateio.data.remote.TmdbEpisodeMetadata
import com.example.rateio.model.ItemStatus
import com.example.rateio.model.RateItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json


class RateItemRepository(private val dao: RateItemDao) {

    fun observeRootItems(categoryId: Long): Flow<List<RateItem>> =
        dao.observeRootItems(categoryId).map { it.map(RateItemEntity::toDomain) }

    fun observeChildren(parentId: Long?): Flow<List<RateItem>> {
        if (parentId == null) return emptyFlow()
        return dao.observeChildren(parentId).map { it.map(RateItemEntity::toDomain) }
    }

    fun observeSeasonEpisodeRatings(showId: Long?): Flow<Map<Int, Map<Int, Float?>>> {
        if (showId == null) return emptyFlow()
        return dao.observeGrandchildren(showId).map { entities ->
            entities.mapNotNull { entity ->
                val metadata = entity.metadataJSON?.let {
                    try {
                        Json.decodeFromString<TmdbEpisodeMetadata>(it)
                    } catch (_: Exception) { null }
                }

                if (metadata != null) metadata to entity.rating else null
            }
                .groupBy { it.first.seasonNumber }
                .mapValues { entry ->
                    entry.value.associate { it.first.episodeNumber to it.second }
                }
        }
    }


    suspend fun getById(id: Long): RateItem? =
        dao.getById(id)?.toDomain()

    suspend fun getByExternalId(externalId: String, categoryId: Long, ): RateItem? =
        dao.getByExternalId(externalId, categoryId)?.toDomain()

    suspend fun getParentById(childId: Long): RateItem? =
        dao.getParent(childId)?.toDomain()



    fun observeRootItemCount(categoryId: Long): Flow<Int> =
        dao.observeRootItemCount(categoryId)

    fun observeRootItemCounts(): Flow<Map<Long, Int>> =
        dao.observeRootItemCounts()
            .map { list -> list.associate { it.categoryId to it.count } }

    fun observeRatedItemCount(): Int =
        dao.observeRatedItemCount()


    /** Finds an existing item by external id or inserts a new skeleton one */
    suspend fun findOrCreate(
        externalId: String,
        categoryId: Long,
        parentId: Long? = null,
        build: () -> RateItem,
    ): Long {
        val existing = dao.getByExternalId(externalId, categoryId)
        if (existing != null) {
            val updated = existing.copy(
                //parentId = build().parentId,
                title = build().title,
                subtitle = build().subtitle,
                coverImageUrl = build().coverImageUrl,
                coverImageLowUrl = build().coverImageLowUrl,
                metadataJSON = build().metadataJSON,
            )
            dao.update(updated)
            return existing.id
        }
        return dao.insert(build().toEntity())
    }

    suspend fun save(item: RateItem): Long =
        dao.insert(item.toEntity())

    suspend fun rate(id: Long, rating: Float?) =
        dao.updateRating(id, rating?.coerceIn(0f, 1f))

    suspend fun setStatus(id: Long, status: ItemStatus) =
        dao.updateStatus(id, status.name)

    suspend fun delete(item: RateItem) =
        dao.delete(item.toEntity())

    suspend fun deleteChildrenWithMatchingSource() =
        dao.deleteChildrenWithMatchingSource()

    suspend fun deleteId(id: Long) =
        dao.deleteId(id)
}