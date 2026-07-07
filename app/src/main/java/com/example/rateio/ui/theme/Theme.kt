package com.example.rateio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.example.rateio.model.HasDisplayName

enum class AppTheme(override val displayName: String) : HasDisplayName {
    DARK("Dark"),
    OLED("OLED"),
    LIGHT("Light"),
    SYSTEM("System")
}

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

private fun androidx.compose.material3.ColorScheme.convertToOled(): androidx.compose.material3.ColorScheme {
    return this.copy(
        background = Color.Black,
        surface = Color.Black,
        surfaceDim = Color.Black,
        surfaceBright = Color.Black,
        surfaceContainerLowest = Color.Black,
        surfaceContainerLow = Color.Black,
        surfaceContainer = Color.Black,
        surfaceContainerHigh = Color.Black,
        surfaceContainerHighest = Color.Black,
        // Optional: you can leave surfaceVariant alone if you want subtle separation for cards,
        // or set it to Black/very dark gray if you want total blackout.
        surfaceVariant = Color(0xFF121212)
    )
}

@Composable
fun RateioTheme(
    appTheme: AppTheme = AppTheme.SYSTEM,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            when (appTheme) {
                AppTheme.DARK -> dynamicDarkColorScheme(context)
                AppTheme.OLED -> dynamicDarkColorScheme(context).convertToOled()
                AppTheme.LIGHT -> dynamicLightColorScheme(context)
                AppTheme.SYSTEM -> if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
        }
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        motionScheme = MotionScheme.expressive(),
        typography = Typography,
        content = content
    )
}