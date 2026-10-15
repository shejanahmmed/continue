package com.shejan.acontinue.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
        modifier = Modifier.fillMaxSize(),
        containerColor = UtilitarianPureBlack,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
            modifier = Modifier.fillMaxSize()
        ) {
            content(innerPadding)
        }
    }
}

