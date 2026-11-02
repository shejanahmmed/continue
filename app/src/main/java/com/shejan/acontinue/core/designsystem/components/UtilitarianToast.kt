package com.shejan.acontinue.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors
import kotlinx.coroutines.delay

@Stable
class UtilitarianToastState {
    var message by mutableStateOf<String?>(null)
        private set

    fun show(msg: String) {
        message = msg
    }

    fun dismiss() {
        message = null
    }
}

val LocalToastState = staticCompositionLocalOf { UtilitarianToastState() }

@Composable
fun rememberUtilitarianToastState(): UtilitarianToastState {
    return remember { UtilitarianToastState() }
}

@Composable
fun UtilitarianToastHost(
    toastState: UtilitarianToastState,
    modifier: Modifier = Modifier
) {
    val colors = LocalUtilitarianColors.current
    val currentMsg = toastState.message

    LaunchedEffect(currentMsg) {
        if (currentMsg != null) {
            delay(2500L)
            toastState.dismiss()
        }
    }

    AnimatedVisibility(
        visible = currentMsg != null,
        enter = fadeIn(tween(160)) + slideInVertically(
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                dampingRatio = Spring.DampingRatioLowBouncy
            )
        ) { it / 2 },
        exit = fadeOut(tween(140)) + slideOutVertically { it / 2 },
        modifier = modifier
    ) {
        if (currentMsg != null) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colors.cardBackground,
                border = BorderStroke(1.dp, colors.borderStroke),
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(colors.accent, CircleShape)
                    )
                    Text(
                        text = currentMsg,
                        color = colors.textPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.3.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

