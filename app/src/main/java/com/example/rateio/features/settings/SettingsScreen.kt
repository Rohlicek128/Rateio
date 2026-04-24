package com.example.rateio.features.settings

import androidx.compose.foundation.layout.Column
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
import com.example.rateio.ui.theme.Typography


@Composable
fun SettingsScreen(contentPadding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(contentPadding)
    ) {
        Text("Settings", style = Typography.headlineLarge)

        LazyColumn {
            items(50) { id ->
                Card(
                    shape = RoundedCornerShape(size = 16.dp),
                    modifier = Modifier.fillMaxWidth().padding(8.dp)
                ) {
                    Text("Setting #$id", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}