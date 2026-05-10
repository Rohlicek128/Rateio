package com.example.rateio.data.repository

import com.example.rateio.data.CategoryRegistry
import com.example.rateio.data.db.CategoryDao
import com.example.rateio.data.db.CategoryEntity
import com.example.rateio.data.db.toDomain
import com.example.rateio.data.db.toEntity
import com.example.rateio.model.Category
import com.example.rateio.model.CategoryType
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