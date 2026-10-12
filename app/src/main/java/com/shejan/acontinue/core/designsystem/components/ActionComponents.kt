package com.shejan.acontinue.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shejan.acontinue.ui.theme.LocalUtilitarianColors

@Composable
fun HighContrastButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isAccent: Boolean = false,
    isOutlined: Boolean = false
) {
    val colors = LocalUtilitarianColors.current
    val accentColor = colors.accent
    val onAccentColor = if (accentColor == Color.White) Color.Black else Color.White

    if (isOutlined) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, if (isAccent) accentColor else colors.borderStroke),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (isAccent) accentColor else colors.textPrimary
            ),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isAccent) accentColor else colors.textPrimary,
                contentColor = if (isAccent) onAccentColor else colors.background
            ),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = text,
                fontFamily = FontFamily.SansSerif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun UtilitarianBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LocalUtilitarianColors.current.borderStroke,
    textColor: Color = LocalUtilitarianColors.current.textPrimary
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            color = textColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}
