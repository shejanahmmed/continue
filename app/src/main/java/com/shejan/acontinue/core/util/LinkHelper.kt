package com.shejan.acontinue.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.TextUnit

object LinkHelper {
    private val URL_REGEX = Regex(
        """(https?://[^\s<>"{}|\\^`]+|www\.[^\s<>"{}|\\^`]+)""",
        RegexOption.IGNORE_CASE
    )

    fun extractUrls(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        return URL_REGEX.findAll(text).map { match ->
            match.value.trimEnd('.', ',', ')', ']', ';', ':', '>', '"', '\'')
        }.filter { it.length > 4 }.distinct().toList()
    }

    fun formatUrl(url: String): String {
        return if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            "https://$url"
        } else {
            url
        }
    }

    fun openUrl(context: Context, url: String) {
        try {
            val formatted = formatUrl(url)
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(formatted)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // Silently ignore if no browser is available
        }
    }

    fun buildAnnotatedStringWithLinks(
        text: String,
        linkColor: Color = Color(0xFF3897F0)
    ): AnnotatedString {
        return buildAnnotatedString {
            append(text)
            val matches = URL_REGEX.findAll(text)
            for (match in matches) {
                val rawUrl = match.value.trimEnd('.', ',', ')', ']', ';', ':', '>', '"', '\'')
                if (rawUrl.length <= 4) continue
                val startIndex = match.range.first
                val endIndex = startIndex + rawUrl.length
                val fullUrl = formatUrl(rawUrl)

                addLink(
                    url = LinkAnnotation.Url(
                        url = fullUrl,
                        styles = TextLinkStyles(
                            style = SpanStyle(
                                color = linkColor,
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    ),
                    start = startIndex,
                    end = endIndex
                )
            }
        }
    }
}

@Composable
fun ClickableLinkText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    linkColor: Color = Color(0xFF3897F0),
    style: TextStyle = TextStyle.Default,
    lineHeight: TextUnit = TextUnit.Unspecified
) {
    val annotatedString = remember(text, linkColor) {
        LinkHelper.buildAnnotatedStringWithLinks(text, linkColor)
    }

    Text(
        text = annotatedString,
        modifier = modifier,
        color = color,
        style = style,
        lineHeight = lineHeight
    )
}
