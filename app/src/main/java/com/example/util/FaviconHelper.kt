package com.example.util

import android.net.Uri

object FaviconHelper {

    fun getGoogleFaviconUrl(webUrl: String, size: Int = 128): String {
        return try {
            val uri = Uri.parse(webUrl)
            val host = uri.host ?: webUrl
            "https://www.google.com/s2/favicons?domain=$host&sz=$size"
        } catch (_: Exception) {
            "https://www.google.com/s2/favicons?domain=example.com&sz=$size"
        }
    }

    fun cleanUrl(input: String): String {
        var trimmed = input.trim()
        if (trimmed.isEmpty()) return "https://google.com"
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            // Check if user entered a search query or a domain
            if (trimmed.contains(".") && !trimmed.contains(" ")) {
                trimmed = "https://$trimmed"
            } else {
                trimmed = "https://www.google.com/search?q=" + Uri.encode(trimmed)
            }
        }
        return trimmed
    }

    fun extractDomainName(url: String): String {
        return try {
            val uri = Uri.parse(url)
            val host = uri.host ?: return "Web App"
            host.removePrefix("www.").removePrefix("m.")
        } catch (_: Exception) {
            "Web App"
        }
    }
}
