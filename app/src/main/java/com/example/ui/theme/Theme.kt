package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = NeonRed,
    onPrimary = TextWhite,
    primaryContainer = NeonRedDim,
    onPrimaryContainer = TextWhite,
    secondary = AquaCyan,
    onSecondary = ToolBackground,
    secondaryContainer = ToolSurfaceSecondary,
    onSecondaryContainer = TextWhite,
    background = ToolBackground,
    onBackground = TextWhite,
    surface = ToolSurfaceCard,
    onSurface = TextWhite,
    surfaceVariant = ToolSurfaceSecondary,
    onSurfaceVariant = TextGray,
    outline = GrayBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark mode to comply with the instructions
    content: @Composable () -> Unit,
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ToolBackground.toArgb()
            window.navigationBarColor = ToolBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

