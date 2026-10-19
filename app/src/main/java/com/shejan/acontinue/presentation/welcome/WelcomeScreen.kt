package com.shejan.acontinue.presentation.welcome

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.R
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors

/**
 * Utilitarian starter screen introducing first-time users to the application.
 * Designed with a hardware-centric, high-contrast aesthetic and subtle linear motion.
 */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalUtilitarianColors.current
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    // Staggered motion transitions
    val logoAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "logoAlpha"
    )
    val logoScale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.90f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "logoScale"
    )

    val titleAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 350, delayMillis = 100, easing = FastOutSlowInEasing),
        label = "titleAlpha"
    )
    val titleOffsetY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 14f,
        animationSpec = tween(durationMillis = 350, delayMillis = 100, easing = FastOutSlowInEasing),
        label = "titleOffsetY"
    )

    val card1Alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 300, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "card1Alpha"
    )
    val card1OffsetY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 12f,
        animationSpec = tween(durationMillis = 300, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "card1OffsetY"
    )

    val card2Alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 300, delayMillis = 270, easing = FastOutSlowInEasing),
        label = "card2Alpha"
    )
    val card2OffsetY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 12f,
        animationSpec = tween(durationMillis = 300, delayMillis = 270, easing = FastOutSlowInEasing),
        label = "card2OffsetY"
    )

    val card3Alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 300, delayMillis = 340, easing = FastOutSlowInEasing),
        label = "card3Alpha"
    )
    val card3OffsetY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 12f,
        animationSpec = tween(durationMillis = 300, delayMillis = 340, easing = FastOutSlowInEasing),
        label = "card3OffsetY"
    )

    val ctaAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 350, delayMillis = 420, easing = FastOutSlowInEasing),
        label = "ctaAlpha"
    )
    val ctaOffsetY by animateFloatAsState(
        targetValue = if (startAnimation) 0f else 12f,
        animationSpec = tween(durationMillis = 350, delayMillis = 420, easing = FastOutSlowInEasing),
        label = "ctaOffsetY"
    )

    val logoRes = if (colors.isDark) R.drawable.app_logo_light else R.drawable.app_logo_dark

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Section 1: Hero Logo & Identity
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(108.dp)
                        .scale(logoScale)
                        .alpha(logoAlpha)
                        .border(
                            width = 1.dp,
                            color = colors.borderStroke.copy(alpha = 0.5f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = "Continue Logo",
                        modifier = Modifier.size(92.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .alpha(titleAlpha)
                        .graphicsLayer { translationY = titleOffsetY }
                ) {
                    Text(
                        text = "CONTINUE",
                        color = colors.textPrimary,
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        letterSpacing = 5.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "// TEMPORAL FOCUS WORKSPACE",
                        color = colors.textSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section 2: Technical Architecture Readout (Concise, minimal, hardware-focused)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                UtilitarianFeatureCard(
                    icon = Icons.Outlined.AccessTime,
                    code = "01",
                    title = "TIMETABLE",
                    subtitle = "Dynamic schedule tracking with real-time telemetry",
                    modifier = Modifier
                        .alpha(card1Alpha)
                        .graphicsLayer { translationY = card1OffsetY }
                )

                UtilitarianFeatureCard(
                    icon = Icons.Outlined.Notifications,
                    code = "02",
                    title = "PRIORITY ALERTS",
                    subtitle = "Structured reminders with multi-stage audio alerts",
                    modifier = Modifier
                        .alpha(card2Alpha)
                        .graphicsLayer { translationY = card2OffsetY }
                )

                UtilitarianFeatureCard(
                    icon = Icons.Outlined.Shield,
                    code = "03",
                    title = "LOCAL PERSISTENCE",
                    subtitle = "100% offline, zero trackers, private Room database",
                    modifier = Modifier
                        .alpha(card3Alpha)
                        .graphicsLayer { translationY = card3OffsetY }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Section 3: High-Contrast CTA Action
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(ctaAlpha)
                    .graphicsLayer { translationY = ctaOffsetY }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clickable(onClick = onGetStarted),
                    shape = RoundedCornerShape(8.dp),
                    color = colors.textPrimary
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GET STARTED",
                            color = colors.background,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = colors.background,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "v1.0.0 // READY",
                    color = colors.textDisabled,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

/**
 * Clean 1dp bordered specification card.
 */
@Composable
private fun UtilitarianFeatureCard(
    icon: ImageVector,
    code: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val colors = LocalUtilitarianColors.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = colors.cardBackground,
        border = BorderStroke(1.dp, colors.borderStroke)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.accent,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$code //",
                        color = colors.accent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = title,
                        color = colors.textPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = subtitle,
                    color = colors.textSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
