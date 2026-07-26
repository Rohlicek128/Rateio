package com.rohlicek.rateio.data.repository

import com.rohlicek.rateio.data.db.RateItemDao
import com.rohlicek.rateio.data.db.RateItemEntity
import com.rohlicek.rateio.data.db.toDomain
import com.rohlicek.rateio.data.db.toEntity
import com.rohlicek.rateio.data.remote.tmdb.TmdbEpisodeMetadata
import com.rohlicek.rateio.model.CategoryType
import com.rohlicek.rateio.model.ItemStatus
import com.rohlicek.rateio.model.RateItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json


class RateItemRepository(private val dao: RateItemDao) {

    fun observeItems(): Flow<List<RateItem>> =
        dao.observeItems().map { it.map(RateItemEntity::toDomain) }

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

    fun observeGrandchildrenByChildren(id: Long?): Flow<Map<String?, Map<String?, RateItem>>> {
        if (id == null) return emptyFlow()
        return dao.observeGrandchildren(id).map { entities ->
            entities
                .groupBy { it.parentId }
                .mapValues { entry ->
                    entry.value.associate { it.externalId to it.toDomain() }
                }
                .mapKeys {
                    getById(it.key ?: 0)?.externalId
                }
        }
    }

    fun observeBySource(source: CategoryType): Flow<List<RateItem>> {
        return dao.observeBySource(source.name).map { it.map(RateItemEntity::toDomain) }
    }

    fun observeBySources(sources: List<CategoryType>): Flow<List<RateItem>> {
        if (sources.isEmpty()) return flowOf(emptyList())

        return dao.observeBySources(sources.map { it.name })
            .map { entities -> entities.map(RateItemEntity::toDomain) }
    }


    suspend fun getById(id: Long): RateItem? =
        dao.getById(id)?.toDomain()

    suspend fun getByExternalId(externalId: String, categoryId: Long): RateItem? =
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


    suspend fun getRankInExternalSource(id: Long, externalSource: CategoryType): Int {
        return dao.getRankInExternalSource(id, externalSource.name)
    }


    suspend fun findAndUpdateMetadata(
        externalId: String,
        categoryId: Long,
        parentId: Long? = null,
        build: () -> RateItem,
    ): RateItem? {
        val existing = dao.getByExternalId(externalId, categoryId) ?: return null

        val built = build()
        val updated = existing.copy(
            parentId = parentId,
            title = built.title,
            subtitle = built.subtitle,
            length = built.length,
            coverImageUrl = built.coverImageUrl ?: existing.coverImageUrl,
            coverImageLowUrl = built.coverImageLowUrl ?: existing.coverImageLowUrl,
            backdropImageUrl = built.backdropImageUrl ?: existing.backdropImageUrl,
            backdropImageLowUrl = built.backdropImageLowUrl ?: existing.backdropImageLowUrl,
            metadataJSON = built.metadataJSON ?: existing.metadataJSON,
        )
        dao.update(updated)
        return updated.toDomain()
    }

    /** Finds an existing item by external id or inserts a new skeleton one */
    suspend fun findOrCreate(
        externalId: String,
        categoryId: Long,
        parentId: Long? = null,
        build: () -> RateItem,
    ): Long {
        val existing = findAndUpdateMetadata(
            externalId = externalId,
            categoryId = categoryId,
            parentId = parentId,
            build = build,
        )
        if (existing != null) return existing.id
        return dao.insert(build().copy(id = 0, parentId = parentId, rating = null).toEntity())
    }

    suspend fun save(item: RateItem): Long =
        dao.insert(item.toEntity())

    suspend fun rate(id: Long, rating: Float?) =
        dao.updateRating(id, rating?.coerceIn(0f, 1f))

    suspend fun setWeight(id: Long, weight: Float?) =
        dao.updateWeight(id, weight)

    suspend fun setStatus(id: Long, status: ItemStatus) =
        dao.updateStatus(id, status.name)

    suspend fun setCoverOverride(id: Long, override: String?) =
        dao.updateCoverOverride(id, override)

    suspend fun setMetadata(id: Long, metadata: String?) =
        dao.updateMetadata(id, metadata)

    suspend fun update(item: RateItem) =
        dao.update(item.toEntity())

    suspend fun delete(item: RateItem) =
        dao.delete(item.toEntity())

    suspend fun deleteChildrenWithMatchingSource() =
        dao.deleteChildrenWithMatchingSource()

    suspend fun deleteId(id: Long) =
        dao.deleteId(id)
}