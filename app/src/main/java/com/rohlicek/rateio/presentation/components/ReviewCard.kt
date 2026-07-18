package com.rohlicek.rateio.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import kotlinx.coroutines.launch
import androidx.graphics.shapes.toPath



class MorphPolygonShape(
    private val morph: Morph,
    private val percentage: Float
) : Shape {
    private val matrix = Matrix()
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        // Below assumes that you haven't changed the default radius of 1f, nor the centerX and centerY of 0f
        // By default this stretches the path to the size of the container, if you don't want stretching, use the same size.width for both x and y.
        matrix.scale(size.width / 2f, size.height / 2f)
        matrix.translate(1f, 1f)

        val path = morph.toPath(progress = percentage).asComposePath()
        path.transform(matrix)
        return Outline.Generic(path)
    }
}


@Composable
fun ReviewCard(
    name: String,
    avatarPath: String?,
    supportingText: String?,
    rating: Float?,
    content: String,
    modifier: Modifier = Modifier,
    placeholderContent: String? = null,
    onEdit: ((String) -> Unit)? = null,
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var showSheet by remember { mutableStateOf(false) }

    val morphProgress = remember { Animatable(0f) }
    val morph = remember {
        Morph(
            start = MaterialShapes.Cookie9Sided,
            end = MaterialShapes.Circle
        )
    }

    Card(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.ToggleOn)
                showSheet = true
            }),
        colors = CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.width(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AdaptiveAsyncImage(
                        model = avatarPath,
                        contentDescription = name,
                        modifier = Modifier
                            .size(IconButtonDefaults.extraLargeIconSize)
                            .clip(
                                if (morphProgress.value < 0.9f) MaterialShapes.Cookie9Sided.toShape()
                                else MaterialShapes.Circle.toShape()
                            ),
                        placeholderRatio = 1f,
                        contentScale = ContentScale.Crop,
                        onSuccess = {
                            scope.launch {
                                morphProgress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = 100)
                                )
                            }
                        }
                    )
                    Column {
                        Text(
                            name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (supportingText != null) {
                            Text(
                                supportingText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                RateBox(rating = rating)
            }
            if (placeholderContent != null && content.isBlank()) {
                Text(
                    placeholderContent,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            else {
                Text(
                    content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }

    if (showSheet) {
        FullReviewModal(
            name = name,
            avatarPath = avatarPath,
            supportingText = supportingText,
            rating = rating,
            content = content,
            onDismiss = { showSheet = false },
            onEdit = onEdit,
        )
    }
}

@Composable
private fun FullReviewModal(
    name: String,
    avatarPath: String?,
    supportingText: String?,
    rating: Float?,
    content: String,
    onDismiss: () -> Unit,
    onEdit: ((String) -> Unit)? = null,
) {
    var editedContent by remember { mutableStateOf(content) }
    var imageLoaded by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.width(250.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AdaptiveAsyncImage(
                        model = avatarPath,
                        contentDescription = name,
                        modifier = Modifier
                            .size(70.dp)
                            .clip(
                                if (!imageLoaded) MaterialShapes.Cookie9Sided.toShape()
                                else MaterialShapes.Circle.toShape()
                            ),
                        placeholderRatio = 1f,
                        contentScale = ContentScale.Crop,
                        onSuccess = {
                            imageLoaded = true
                        }
                    )
                    Column {
                        Text(
                            name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (supportingText != null) {
                            Text(
                                supportingText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                RateBox(rating = rating,)
            }

            LazyColumn(
                modifier = Modifier.clip(MaterialTheme.shapes.extraLarge.copy(
                    topStart = CornerSize(4.dp),
                    topEnd = CornerSize(4.dp),
                )),
            ) {
                item {
                    if (onEdit != null) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            value = editedContent,
                            colors = OutlinedTextFieldDefaults.colors().copy(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                                focusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            ),
                            onValueChange = {
                                editedContent = it
                                onEdit(it)
                            },
                            placeholder = {
                                Text("Your thoughts")
                            },
                            minLines = 4,
                        )
                    }
                    else {
                        Text(
                            editedContent,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(150.dp)) }
            }
        }
    }
}