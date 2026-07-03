package com.example.rateio.presentation.settings.rating

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.rateio.presentation.rating.display.RatingTransformation


class RatingTransformationSettingsViewModel(
    defaultTransformations: RatingTransformation
) : ViewModel() {
    var uiState by mutableStateOf(defaultTransformations)
        private set
    private val initialRating: Float? = 0.9f
    var testRating by mutableStateOf(initialRating)

    fun updateTransformations(update: (RatingTransformation) -> RatingTransformation) {
        uiState = update(uiState)
    }

    companion object {
        fun factory(defaultTransformations: RatingTransformation) = viewModelFactory {
            initializer { RatingTransformationSettingsViewModel(defaultTransformations) }
        }
    }
}