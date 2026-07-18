package com.rohlicek.rateio.data.repository

import com.rohlicek.rateio.data.CategoryRegistry
import com.rohlicek.rateio.data.db.CategoryDao
import com.rohlicek.rateio.data.db.CategoryEntity
import com.rohlicek.rateio.data.db.toDomain
import com.rohlicek.rateio.data.db.toEntity
import com.rohlicek.rateio.model.Category
import com.rohlicek.rateio.model.CategoryType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


class CategoryRepository(private val dao: CategoryDao) {

    fun observeUserCategories(): Flow<List<Category>> =
        dao.observeAll().map { it.map(CategoryEntity::toDomain) }

    suspend fun getUserCategories(): List<Category> =
        dao.observeAll().first().map(CategoryEntity::toDomain)


    suspend fun getAvailableCategories(): List<Category> {
        val addedTypes = getUserCategories().map { it.type }.toSet()
        return CategoryRegistry.all.filter { it.type !in addedTypes }
    }

    suspend fun addCategory(category: Category): Long =
        dao.insert(category.toEntity())

    suspend fun removeCategory(category: Category) =
        dao.delete(category.toEntity())

    suspend fun getCategoryByType(type: CategoryType): Category? =
        dao.getByType(type.name)?.toDomain()

    suspend fun getCategoryById(id: Long): Category? =
        dao.getById(id)?.toDomain()
}