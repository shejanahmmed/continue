package com.shejan.acontinue.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Utility helper for detecting map location links and opening locations in the device's default maps application.
 */
object LocationHelper {

    fun isMapLocationLink(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return false
        return trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true) ||
                trimmed.startsWith("geo:", ignoreCase = true) ||
                trimmed.contains("maps.google.com", ignoreCase = true) ||
                trimmed.contains("goo.gl/maps", ignoreCase = true) ||
                trimmed.contains("maps.app.goo.gl", ignoreCase = true) ||
                trimmed.contains("openstreetmap.org", ignoreCase = true) ||
                trimmed.contains("waze.com", ignoreCase = true)
    }

    fun openLocationInMapApp(context: Context, locationText: String) {
        val trimmed = locationText.trim()
        if (trimmed.isEmpty()) return

        val uri = when {
            trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("geo:", ignoreCase = true) -> {
                Uri.parse(trimmed)
            }
            trimmed.contains("maps.app.goo.gl", ignoreCase = true) ||
            trimmed.contains("goo.gl/maps", ignoreCase = true) ||
            trimmed.contains("maps.google.com", ignoreCase = true) ||
            trimmed.contains("openstreetmap.org", ignoreCase = true) ||
            trimmed.contains("waze.com", ignoreCase = true) -> {
                Uri.parse("https://$trimmed")
            }
            else -> {
                Uri.parse("geo:0,0?q=${Uri.encode(trimmed)}")
            }
        }

        // Attempt to launch directly in the official Google Maps application first.
        // This bypasses OEM/browser web redirection loading dialogs (such as Xiaomi/MIUI Link Resolver).
        val googleMapsIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            setPackage("com.google.android.apps.maps")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(googleMapsIntent)
            return
        } catch (_: Exception) {
            // Google Maps package is not installed or unavailable; fall through to generic intent.
        }

        val fallbackIntent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(fallbackIntent)
        } catch (_: Exception) {
            Toast.makeText(context, "No map application available", Toast.LENGTH_SHORT).show()
        }
    }
}
