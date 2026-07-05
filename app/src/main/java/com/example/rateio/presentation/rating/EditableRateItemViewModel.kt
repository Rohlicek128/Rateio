package com.example.rateio.presentation.rating

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.model.RateItem

class EditableRateItemViewModel(
    item: RateItem
) : ViewModel() {
    var itemState by mutableStateOf(item)
        private set

    fun updateItem(update: (RateItem) -> RateItem) {
        itemState = update(itemState)
    }

    companion object {
        fun factory(item: RateItem) = viewModelFactory {
            initializer { EditableRateItemViewModel(item) }
        }
    }
}