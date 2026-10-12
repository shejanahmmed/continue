package com.shejan.acontinue.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shejan.acontinue.ui.theme.UtilitarianPureBlack

@Composable
fun UtilitarianScaffold(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onCreateClick: () -> Unit,
    showBottomBar: Boolean = true,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(UtilitarianPureBlack)
            .statusBarsPadding(),
        containerColor = UtilitarianPureBlack,
        bottomBar = {
            if (showBottomBar) {
                FloatingUtilitarianNavBar(
                    currentRoute = currentRoute,
                    onNavigate = onNavigate,
                    onCreateClick = onCreateClick
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(UtilitarianPureBlack)
        ) {
            content(innerPadding)
        }
    }
}
