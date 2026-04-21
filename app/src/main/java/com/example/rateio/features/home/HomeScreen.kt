package com.example.rateio.features.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


data class ItemData(val id: Int, val title: String, val description: String)

val sampleItems = List(15) { i ->
    ItemData(i, "Category #$i", "This is the detailed information for item $i. It appears in a fullscreen Material 3 layout.")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(contentPadding: PaddingValues, onItemClick: (Int) -> Unit) {
    Column(modifier = Modifier.padding(contentPadding)) {
        Text("Categories", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(sampleItems) { item ->
                Card(
                    onClick = { onItemClick(item.id) },
                    shape = RoundedCornerShape(size = 16.dp),
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Text(item.title, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }


}