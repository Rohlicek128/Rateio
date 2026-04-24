package com.example.rateio.features.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage


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
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    AsyncImage(
                        model = "https://image.tmdb.org/t/p/w342/yihdXomYb5kTeSivtFndMy5iDmf.jpg",
                        contentDescription = "Translated description of what the image contains",
                        modifier = Modifier
                            .width(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .padding(12.dp)
                    )

                    Text("Project Hail Mary #$id", modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}