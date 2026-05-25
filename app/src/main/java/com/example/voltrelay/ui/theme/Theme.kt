package com.example.voltrelay.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    onTertiaryContainer = OnTertiaryContainerDark,
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = OnPrimaryContainerLight,
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    onSecondaryContainer = OnSecondaryContainerLight,
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    onTertiaryContainer = OnTertiaryContainerLight,
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight
)

@Composable
fun VoltRelinkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    seedColor: Color? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // 1. Priority: Dynamic Color (Monet)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // 2. Priority: Custom Seed Color
        seedColor != null -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = seedColor,
                    onPrimary = Color.Black,
                    primaryContainer = seedColor.copy(alpha = 0.4f),
                    onPrimaryContainer = Color.White,
                    secondary = seedColor,
                    onSecondary = Color.Black,
                    secondaryContainer = seedColor.copy(alpha = 0.3f),
                    onSecondaryContainer = Color.White,
                    tertiary = seedColor,
                    onTertiary = Color.Black,
                    tertiaryContainer = seedColor.copy(alpha = 0.25f),
                    onTertiaryContainer = Color.White,
                    surface = Color(0xFF1C1B1F),
                    onSurface = Color(0xFFE6E1E5),
                    background = Color(0xFF1C1B1F),
                    onBackground = Color(0xFFE6E1E5),
                    surfaceVariant = Color(0xFF49454F),
                    onSurfaceVariant = Color(0xFFCAC4D0)
                )
            } else {
                lightColorScheme(
                    primary = seedColor,
                    onPrimary = Color.White,
                    primaryContainer = seedColor.copy(alpha = 0.2f),
                    onPrimaryContainer = seedColor,
                    secondary = seedColor,
                    onSecondary = Color.White,
                    secondaryContainer = seedColor.copy(alpha = 0.15f),
                    onSecondaryContainer = seedColor,
                    tertiary = seedColor,
                    onTertiary = Color.White,
                    tertiaryContainer = seedColor.copy(alpha = 0.12f),
                    onTertiaryContainer = seedColor,
                    surface = Color(0xFFFFFBFE),
                    onSurface = Color(0xFF1C1B1F),
                    background = Color(0xFFFFFBFE),
                    onBackground = Color(0xFF1C1B1F),
                    surfaceVariant = Color(0xFFE7E0EC),
                    onSurfaceVariant = Color(0xFF49454F)
                )
            }
        }
        // 3. Fallback
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
