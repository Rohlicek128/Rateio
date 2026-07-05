package com.example.rateio.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rateio.presentation.ScreenScaffold
import com.example.rateio.presentation.components.ModalEnumSelector
import com.example.rateio.ui.theme.AppTheme


@Composable
fun SettingsAppearanceScreen(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    onBackClick: () -> Unit,
) {
    var showThemeModal by remember { mutableStateOf(false) }

    ScreenScaffold(
        title = "Appearance",
        onBackClick = onBackClick,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = padding,
        ) {

            item { SettingsListHeader("Global Theme") }
            item {
                SettingListItem(
                    title = "Appearance",
                    description = "Switch between theme appearances",
                    icon = Icons.Default.Palette,
                    position = ListItemPosition.SINGLE,
                    supportingContent = {
                        SettingsSelectedEnum(
                            modifier = Modifier.padding(top = 8.dp),
                            name = currentTheme.displayName,
                        )
                    },
                    showNavigateIconOnClick = false,
                    onClick = { showThemeModal = true }
                )
                if (showThemeModal) {
                    ModalEnumSelector(
                        title = "Category",
                        selectedOption = currentTheme,
                        onOptionSelected = onThemeChange,
                        onDismiss = { showThemeModal = false },
                    )
                }
            }

        }
    }
}