package com.shejan.acontinue.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val UtilitarianColorScheme = darkColorScheme(
    primary = UtilitarianTextPrimary,
    onPrimary = UtilitarianPureBlack,
    primaryContainer = UtilitarianCardBackground,
    onPrimaryContainer = UtilitarianTextPrimary,
    secondary = UtilitarianAccentRed,
    onSecondary = UtilitarianTextPrimary,
    background = UtilitarianPureBlack,
    onBackground = UtilitarianTextPrimary,
    surface = UtilitarianSurfaceBlack,
    onSurface = UtilitarianTextPrimary,
    surfaceVariant = UtilitarianCardBackground,
    onSurfaceVariant = UtilitarianTextSecondary,
    outline = UtilitarianBorderStroke
)

@Composable
fun ContinueTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            window?.let {
                it.statusBarColor = UtilitarianPureBlack.toArgb()
                it.navigationBarColor = UtilitarianPureBlack.toArgb()
                WindowCompat.getInsetsController(it, view).apply {
                    isAppearanceLightStatusBars = false
                    isAppearanceLightNavigationBars = false
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = UtilitarianColorScheme,
        typography = Typography,
        content = content
    )
}