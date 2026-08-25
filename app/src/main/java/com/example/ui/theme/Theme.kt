package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ElegantDarkColorScheme = darkColorScheme(
    primary = LavenderPrimary,
    onPrimary = DeepPurpleOnPrimary,
    primaryContainer = DeepPurpleContainer,
    onPrimaryContainer = LavenderOnContainer,
    secondary = RoseAccent,
    onSecondary = RoseDarkText,
    secondaryContainer = DeepPurpleContainer,
    onSecondaryContainer = LavenderOnContainer,
    tertiary = AmberEnergy,
    onTertiary = DeepPurpleOnPrimary,
    background = ElegantBackground,
    onBackground = TextWhite,
    surface = ElegantSurface,
    onSurface = TextWhite,
    surfaceVariant = ElegantCard,
    onSurfaceVariant = TextGrayLight,
    outline = ElegantCardBorder,
    error = CoralError
)

private val LightColorScheme = lightColorScheme(
    primary = DeepPurpleContainer,
    onPrimary = Color.White,
    primaryContainer = LavenderOnContainer,
    onPrimaryContainer = DeepPurpleContainer,
    secondary = DeepPurpleContainer,
    onSecondary = Color.White,
    secondaryContainer = RoseAccent,
    onSecondaryContainer = RoseDarkText,
    tertiary = AmberEnergy,
    onTertiary = TextDark,
    background = LightBackground,
    onBackground = TextDark,
    surface = LightSurface,
    onSurface = TextDark,
    surfaceVariant = LightCard,
    onSurfaceVariant = TextGrayMuted,
    outline = LightBorder,
    error = CoralError
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to Elegant Dark
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> ElegantDarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = ElegantBackground.toArgb()
                window.navigationBarColor = ElegantNav.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
