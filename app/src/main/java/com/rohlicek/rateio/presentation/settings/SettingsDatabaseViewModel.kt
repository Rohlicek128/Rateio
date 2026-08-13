package com.rohlicek.rateio.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rohlicek.rateio.data.backup.parseImdbCsv
import com.rohlicek.rateio.data.repository.CategoryRepository
import com.rohlicek.rateio.data.repository.RateItemRepository
import kotlinx.coroutines.launch
import java.io.InputStream


const val LOAD_RATINGS_FROM_ZERO = true

class SettingsDatabaseViewModel(
    private val categoryRepository: CategoryRepository,
    private val itemRepository: RateItemRepository,
) : ViewModel() {

    fun importImdb(file: InputStream) {
        viewModelScope.launch {
            val imdbRecords = parseImdbCsv(file)
        }
    }

    companion object {
        fun factory(categoryRepository: CategoryRepository, itemRepository: RateItemRepository) = viewModelFactory {
            initializer { SettingsDatabaseViewModel(categoryRepository, itemRepository) }
        }
    }
}