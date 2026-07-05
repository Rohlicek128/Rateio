package com.example.rateio.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.rateio.presentation.components.MajorSectionHeader

@Composable
fun ModalSettings(
    modifier: Modifier = Modifier,
    title: String,
    onDismiss: () -> Unit,
    skipPartiallyExpanded: Boolean = true,
    content: LazyListScope.() -> Unit = {},
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = skipPartiallyExpanded),
        containerColor = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = modifier
                .padding(horizontal = 20.dp)
                .clip(MaterialTheme.shapes.extraLarge.copy(
                    topStart = CornerSize(4.dp),
                    topEnd = CornerSize(4.dp))
                ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item { MajorSectionHeader(title) }

            content()

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}