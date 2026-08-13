package com.rohlicek.rateio.presentation.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.ButtonGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rohlicek.rateio.data.preferences.SyncPreferences
import com.rohlicek.rateio.presentation.ScreenScaffold
import com.rohlicek.rateio.presentation.components.OpenButton
import com.rohlicek.rateio.presentation.components.SaveButton
import kotlinx.coroutines.launch


@Composable
fun SettingsCategoriesScreen(
    onBackClick: () -> Unit,
) {
    val context = LocalContext.current
    val preferences = remember { SyncPreferences(context) }
    val scope = rememberCoroutineScope()

    val token by preferences.tmdbApiToken.collectAsStateWithLifecycle(initialValue = "")
    var inputToken by remember(token) { mutableStateOf(token) }


    val interactionSources = remember(2) {
        List(2) { MutableInteractionSource() }
    }

    ScreenScaffold(
        title = "Categories",
        onBackClick = onBackClick,
    ) { padding, listState ->
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = padding,
            state = listState,
        ) {
            item { SettingsListHeader("TMDB") }
            item {
                AnimatedVisibility(
                    visible = !inputToken.isBlank() && inputToken.length < 50,
                    enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                    exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                ) {
                    WarningCard(
                        title = "Too short for a token",
                        description = "This is probably not the TMDB API Read Access Token. Please check again.",
                    )
                }
            }
            item {
                InfoCard(
                    title = "Free TMDB API Token",
                    description = "You can get a TMDB API Read Access Token for free by registering on the TMDB site."
                )
            }
            item {
                SettingListItem(
                    title = "TMDB API Read Access Token",
                    description = "Go to /settings/api on TMDB, copy your API Read Access Token (the long key, +200 characters), and paste it here.",
                    icon = Icons.Default.Key,
                    position = ListItemPosition.SINGLE,
                    supportingContent = {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            SettingsTextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = inputToken,
                                onValueChange = {
                                    inputToken = it
                                },
                                placeholder = { SettingsPlaceholderText("eg. 9b9HKUT4u5hi3...") },
                            )

                            ButtonGroup(
                                modifier = Modifier.fillMaxWidth(),
                                expandedRatio = 0.2f,
                                overflowIndicator = {},
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                customItem(
                                    buttonGroupContent = {
                                        OpenButton(
                                            modifier = Modifier.weight(1f).animateWidth(interactionSource = interactionSources[0]),
                                            label = "TMDB API",
                                            onClickUrl = "https://www.themoviedb.org/settings/api",
                                            interactionSource = interactionSources[0]
                                        )
                                    },
                                    menuContent = {}
                                )
                                customItem(
                                    buttonGroupContent = {
                                        SaveButton(
                                            modifier = Modifier.weight(1f).animateWidth(interactionSource = interactionSources[1]),
                                            onClick = {
                                                scope.launch {
                                                    preferences.saveTmdbApiToken(inputToken.trim())
                                                    Toast.makeText(context, "TMDB Token has been saved", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            interactionSource = interactionSources[1]
                                        )
                                    },
                                    menuContent = {}
                                )
                            }
                        }
                    },
                )
            }

        }
    }
}