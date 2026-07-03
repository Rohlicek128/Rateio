package com.example.rateio.presentation.rating.openlibrary

import androidx.compose.runtime.mutableStateSetOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.data.CategoryRegistry
import com.example.rateio.data.remote.openlibrary.OLAuthorDetail
import com.example.rateio.data.remote.openlibrary.OLWorkDetail
import com.example.rateio.data.remote.openlibrary.OLWorkEdition
import com.example.rateio.data.remote.openlibrary.OpenLibraryClient
import com.example.rateio.data.remote.openlibrary.toRateItem
import com.example.rateio.data.repository.CategoryRepository
import com.example.rateio.data.repository.RateItemRepository
import com.example.rateio.model.CategoryType
import com.example.rateio.model.RateItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class OLWorkDetailState(
    val work: OLWorkDetail? = null,
    val author: OLAuthorDetail? = null,
    val editionsWithContents: List<OLWorkEdition> = emptyList(),
    val numberOfPages: Int? = null,
    val savedItem: RateItem? = null,

    val collapsedHeaders: MutableSet<String> = mutableStateSetOf(),

    val isLoading: Boolean = false,
    val error: String? = null,
)

class OLWorkDetailViewModel(
    key: String,
    private val categoryRepository: CategoryRepository,
    private val itemRepository: RateItemRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(OLWorkDetailState())
    val state: StateFlow<OLWorkDetailState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val work = OpenLibraryClient.service.getWork(key)
                _state.update { it.copy(work = work, isLoading = false) }

                if (!work.authors.isNullOrEmpty()) {
                    val authorKey = work.authors.first().author?.key
                    if (authorKey != null) {
                        launch {
                            val author = OpenLibraryClient.service.getAuthors(authorKey.removePrefix("/authors/"))
                            _state.update { it.copy(author = author) }
                        }
                    }
                }

                launch {
                    val editions = OpenLibraryClient.service.getWorkEditions(key).entries
                    if (!editions.isNullOrEmpty()) {
                        val contents = editions.filter { it.tableOfContents != null }
                        if (contents.isNotEmpty()) {
                            _state.update { it.copy(editionsWithContents = contents) }
                        }

                        val pages = editions.find { it.numberOfPages != null }?.numberOfPages
                        if (pages != null && pages > 0) {
                            _state.update { it.copy(numberOfPages = pages) }
                        }
                    }
                }

                launch {
                    val booksCategory = categoryRepository.getCategoryByType(CategoryType.OPEN_LIBRARY_BOOKS)
                    booksCategory?.let { cat ->
                        val existing = itemRepository.getByExternalId(
                            externalId = work.key.toString(),
                            categoryId = cat.id,
                        )
                        if (existing != null) _state.update { it.copy(savedItem = existing) }
                    }
                }
            } catch (e: Exception) {
                _state.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun onToggleSaved(work: OLWorkDetail, author: OLAuthorDetail? = null) {
        viewModelScope.launch {
            val state = _state.value
            if (state.savedItem != null) {
                itemRepository.delete(RateItem(
                    id = state.savedItem.id,
                    categoryId = categoryRepository.getCategoryByType(CategoryType.OPEN_LIBRARY_BOOKS)?.id ?: 0,
                    title = work.title ?: "Unknown Name",
                ))
                _state.update { it.copy(savedItem = null) }
            } else {
                val cat = categoryRepository.getCategoryByType(CategoryType.OPEN_LIBRARY_BOOKS)
                    ?: categoryRepository.addCategory(
                        CategoryRegistry.forType(CategoryType.OPEN_LIBRARY_BOOKS)!!
                    ).let { id -> CategoryRegistry.forType(CategoryType.OPEN_LIBRARY_BOOKS)!!.copy(id = id) }

                val id = itemRepository.save(work.toRateItem(cat.id, author?.name))
                val item = itemRepository.getById(id)
                _state.update { it.copy(savedItem = item) }
            }
        }
    }

    companion object {
        fun factory(key: String, categoryRepository: CategoryRepository, itemRepository: RateItemRepository) = viewModelFactory {
            initializer { OLWorkDetailViewModel(key, categoryRepository, itemRepository) }
        }
    }
}