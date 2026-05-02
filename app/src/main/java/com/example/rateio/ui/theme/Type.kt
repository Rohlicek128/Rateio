package com.example.rateio.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

val GoogleSansTypography = Typography(
    displayLarge   = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal),
    displayMedium  = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal),
    displaySmall   = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal),
    headlineLarge  = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal),
    headlineMedium = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal),
    headlineSmall  = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal),
    titleLarge     = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Medium),
    titleMedium    = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Medium),
    titleSmall     = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Medium),
    bodyLarge      = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal),
    bodyMedium     = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal),
    bodySmall      = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal),
    labelLarge     = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Medium),
    labelMedium    = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal),
    labelSmall     = TextStyle(fontFamily = GoogleSans, fontWeight = FontWeight.Normal),
)