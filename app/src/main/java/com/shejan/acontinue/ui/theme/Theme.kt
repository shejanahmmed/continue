package com.shejan.acontinue.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.shejan.acontinue.domain.model.AccentPalette
import com.shejan.acontinue.domain.model.ThemeMode
import com.shejan.acontinue.domain.model.UserSettings

data class UtilitarianColors(
    val background: Color,
    val surface: Color,
    val cardBackground: Color,
    val borderStroke: Color,
    val borderActive: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textDisabled: Color,
    val accent: Color,
    val isDark: Boolean
)

val LocalUtilitarianColors = staticCompositionLocalOf {
    UtilitarianColors(
        background = UtilitarianPureBlack,
        surface = UtilitarianSurfaceBlack,
        cardBackground = UtilitarianCardBackground,
        borderStroke = UtilitarianBorderStroke,
        borderActive = UtilitarianBorderActive,
        textPrimary = UtilitarianTextPrimary,
        textSecondary = UtilitarianTextSecondary,
        textDisabled = UtilitarianTextDisabled,
        accent = AccentMonochromeRed,
        isDark = true
    )
}

@Composable
fun ContinueTheme(
    settings: UserSettings = UserSettings(),
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (settings.themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val rawAccentColor = Color(settings.accentPalette.colorHex)

    val colors = if (isDark) {
        UtilitarianColors(
            background = UtilitarianPureBlack,
            surface = UtilitarianSurfaceBlack,
            cardBackground = UtilitarianCardBackground,
            borderStroke = UtilitarianBorderStroke,
            borderActive = UtilitarianBorderActive,
            textPrimary = UtilitarianTextPrimary,
            textSecondary = UtilitarianTextSecondary,
            textDisabled = UtilitarianTextDisabled,
            accent = rawAccentColor,
            isDark = true
        )
    } else {
        UtilitarianColors(
            background = UtilitarianLightBackground,
            surface = UtilitarianLightSurface,
            cardBackground = UtilitarianLightCard,
            borderStroke = UtilitarianLightBorderStroke,
            borderActive = UtilitarianLightBorderActive,
            textPrimary = UtilitarianLightTextPrimary,
            textSecondary = UtilitarianLightTextSecondary,
            textDisabled = UtilitarianLightTextDisabled,
            accent = if (settings.accentPalette == AccentPalette.TACTILE_WHITE) UtilitarianLightTextPrimary else rawAccentColor,
            isDark = false
        )
    }

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = colors.textPrimary,
            onPrimary = colors.background,
            primaryContainer = colors.cardBackground,
            onPrimaryContainer = colors.textPrimary,
            secondary = colors.accent,
            onSecondary = colors.textPrimary,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.cardBackground,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.borderStroke
        )
    } else {
        lightColorScheme(
            primary = colors.textPrimary,
            onPrimary = colors.background,
            primaryContainer = colors.cardBackground,
            onPrimaryContainer = colors.textPrimary,
            secondary = colors.accent,
            onSecondary = colors.background,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.cardBackground,
            onSurfaceVariant = colors.textSecondary,
            outline = colors.borderStroke
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = colors.background.toArgb()
                it.navigationBarColor = colors.background.toArgb()
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = !isDark
                    isAppearanceLightNavigationBars = !isDark
                }
            }
        }
    }

    CompositionLocalProvider(LocalUtilitarianColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}