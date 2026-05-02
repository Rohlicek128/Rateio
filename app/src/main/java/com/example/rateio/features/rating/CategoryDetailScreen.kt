package com.example.rateio.features.rating

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.rateio.presentation.components.IndexedItem
import com.example.rateio.presentation.components.RateItemCard


@Composable
fun CategoryDetailScreen(categoryId: Long, onBackClick: () -> Unit, onItemClick: (Long) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Category #$categoryId") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding)
        ) {
            items(10) { id ->
                IndexedItem(
                    modifier = Modifier.padding(PaddingValues(horizontal = 14.dp)),
                    index = id + 1,
                    item = {
                        RateItemCard(
                            title = "Project Hail Mary",
                            subtitle = "2026  |  2h 37m",
                            coverImagePath = "https://image.tmdb.org/t/p/w342/yihdXomYb5kTeSivtFndMy5iDmf.jpg",
                            rating = 0.96f,
                            onClick = { onItemClick(id.toLong()) },
                            padding = PaddingValues(vertical = 4.dp),
                            //placeholderRatio = 1f
                        )
                    }
                )
            }
        }

    }
}