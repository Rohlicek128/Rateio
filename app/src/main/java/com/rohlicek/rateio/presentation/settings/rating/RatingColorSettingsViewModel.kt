package com.rohlicek.rateio.presentation.settings.rating

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.rohlicek.rateio.presentation.rating.display.RatingColorBucket
import com.rohlicek.rateio.presentation.rating.display.RatingColorBuckets


class RatingColorSettingsViewModel(
    defaultBuckets: RatingColorBuckets
) : ViewModel() {
    var uiState by mutableStateOf(defaultBuckets)
        private set
    var testRating by mutableStateOf<Float?>(0.9f)

    fun updateBuckets(update: (RatingColorBuckets) -> RatingColorBuckets) {
        uiState = update(uiState)
    }


    fun updateBucketAtIndex(index: Int, bucket: RatingColorBucket) {
        val updatedList = uiState.buckets.toMutableList().apply {
            if (index in indices) {
                this[index] = bucket
            }
        }.sortedByDescending { it.equalOrGreaterThen }
        uiState = uiState.copy(buckets = updatedList)
    }

    fun updateNullBucket(transform: (RatingColorBucket) -> RatingColorBucket) {
        uiState = uiState.copy(nullBucket = transform(uiState.nullBucket))
    }

    fun addBucket(bucket: RatingColorBucket) {
        uiState = uiState.copy(buckets = uiState.buckets + bucket)
    }

    fun removeBucketAtIndex(index: Int) {
        if (index in uiState.buckets.indices) {
            uiState = uiState.copy(buckets = uiState.buckets.filterIndexed { i, _ -> i != index })
        }
    }

    companion object {
        fun factory(defaultBuckets: RatingColorBuckets) = viewModelFactory {
            initializer { RatingColorSettingsViewModel(defaultBuckets) }
        }
    }
}