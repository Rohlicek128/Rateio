package com.rohlicek.rateio.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.rohlicek.rateio.presentation.settings.ErrorCard


@Composable
fun ScreenError(error: String?) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        ErrorCard(
            modifier = Modifier.padding(20.dp),
            description = error ?: "N/A"
        )
    }
}

@Composable
fun ScreenLoading(glowColor: Color = Color.Transparent) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        //CircularWavyProgressIndicator()
        /*Box(
            modifier = Modifier
                .matchParentSize()
                //.offset(y = offset)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            glowColor.copy(alpha = 0.6f),
                            glowColor,
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY,
                    )
                )
        )*/
        LoadingIndicator(
            modifier = Modifier.size(90.dp),
            color = MaterialTheme.colorScheme.surfaceBright,
        )
    }
}