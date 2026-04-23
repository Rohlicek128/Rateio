package com.example.rateio.features.home

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun HomeScreen(contentPadding: PaddingValues, onItemClick: (Int) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = contentPadding
    ) {
        items(50) { id ->
            Card(
                onClick = { onItemClick(id) },
                shape = RoundedCornerShape(size = 16.dp),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text("Category #$id", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}