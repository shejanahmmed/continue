package com.shejan.acontinue.core.designsystem.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shejan.acontinue.domain.model.EntryType
import com.shejan.acontinue.ui.theme.UtilitarianPureBlack

@Composable
fun UtilitarianScaffold(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onCreateClick: () -> Unit,
    onQuickCreate: (EntryType) -> Unit = {},
    showBottomBar: Boolean = true,
    content: @Composable (PaddingValues) -> Unit
) {
    var isSpeedDialOpen by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        isSpeedDialOpen = false
    }

    BackHandler(enabled = isSpeedDialOpen) {
        isSpeedDialOpen = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UtilitarianPureBlack)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = UtilitarianPureBlack,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize()) {
                content(innerPadding)
            }
        }

        // Layer 2: Dim Scrim
        AnimatedVisibility(
            visible = isSpeedDialOpen,
            enter = fadeIn(tween(160)),
            exit = fadeOut(tween(120))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isSpeedDialOpen = false
                    }
            )
        }

        // Layer 3: Speed Dial Pills (centered directly above the '+' button)
        AnimatedVisibility(
            visible = isSpeedDialOpen,
            enter = fadeIn(tween(160)) + slideInVertically(
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy)
            ) { it / 3 },
            exit = fadeOut(tween(120)) + slideOutVertically(
                animationSpec = tween(120, easing = FastOutSlowInEasing)
            ) { it / 3 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 78.dp)
        ) {
            EasyAccessSpeedDialPills(
                onSelect = { entryType ->
                    isSpeedDialOpen = false
                    onQuickCreate(entryType)
                }
            )
        }

        // Layer 4: Floating Nav Bar (always in foreground)
        if (showBottomBar) {
            FloatingUtilitarianNavBar(
                currentRoute = currentRoute,
                onNavigate = {
                    isSpeedDialOpen = false
                    onNavigate(it)
                },
                onCreateClick = {
                    if (isSpeedDialOpen) {
                        isSpeedDialOpen = false
                    } else {
                        onCreateClick()
                    }
                },
                onOpenSpeedDial = {
                    isSpeedDialOpen = true
                },
                isSpeedDialOpen = isSpeedDialOpen,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}


