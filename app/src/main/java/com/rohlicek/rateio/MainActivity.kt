package com.rohlicek.rateio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.rohlicek.rateio.data.preferences.SyncPreferences
import com.rohlicek.rateio.data.remote.imdb.ImdbSyncScheduler
import com.rohlicek.rateio.navigation.AppNavigation
import com.rohlicek.rateio.ui.theme.AppTheme
import com.rohlicek.rateio.ui.theme.RateioTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val preferences = SyncPreferences(applicationContext)
        lifecycleScope.launch {
            if (preferences.isFirstLaunch.first()) {
                ImdbSyncScheduler.triggerImmediateFirstLaunchSync(applicationContext)
                preferences.setFirstLaunchCompleted()
            }
        }

        setContent {
            var currentTheme by remember { mutableStateOf(AppTheme.SYSTEM) }

            RateioTheme(currentTheme) {
                AppNavigation(
                    currentTheme = currentTheme,
                    onThemeChange = { newTheme -> currentTheme = newTheme },
                )
            }
        }
    }
}