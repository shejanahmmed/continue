package com.shejan.acontinue.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.ui.theme.UtilitarianAccentRed
import com.shejan.acontinue.ui.theme.UtilitarianBorderStroke
import com.shejan.acontinue.ui.theme.UtilitarianCardBackground
import com.shejan.acontinue.ui.theme.UtilitarianTextPrimary
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors

@Composable
fun MonochromeCard(
    modifier: Modifier = Modifier,
    borderColor: Color = LocalUtilitarianColors.current.borderStroke,
    backgroundColor: Color = LocalUtilitarianColors.current.cardBackground,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardShape = RoundedCornerShape(12.dp)

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .clip(cardShape),
            shape = cardShape,
            color = backgroundColor,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    } else {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .clip(cardShape),
            shape = cardShape,
            color = backgroundColor,
            border = BorderStroke(1.dp, borderColor)
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}

@Composable
fun DotMatrixHeader(
    title: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    badgeText: String? = null,
    isAccentBadge: Boolean = false,
    badgeAtEnd: Boolean = false,
    actions: @Composable (RowScope.() -> Unit)? = null
) {
    val colors = LocalUtilitarianColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = colors.textPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Text(
            text = title.uppercase(),
            color = colors.textPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            letterSpacing = 1.2.sp
        )

        if (badgeText != null && !badgeAtEnd) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isAccentBadge) colors.accent else colors.borderStroke
            ) {
                Text(
                    text = badgeText,
                    color = if (isAccentBadge && colors.accent == Color.White) Color.Black else colors.textPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        if (badgeAtEnd || actions != null) {
            Spacer(modifier = Modifier.weight(1f))
            if (badgeText != null && badgeAtEnd) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (isAccentBadge) colors.accent else colors.borderStroke
                ) {
                    Text(
                        text = badgeText,
                        color = if (isAccentBadge && colors.accent == Color.White) Color.Black else colors.textPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            if (actions != null) {
                actions()
            }
        }
    }
}
