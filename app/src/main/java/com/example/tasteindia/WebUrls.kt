package com.example.tasteindia

import java.net.URI
import java.util.Locale

/** A pure validator so malformed and non-web schemes are testable offline. */
fun validWebUrl(value: String): String? {
    val trimmed = value.trim()
    val uri = runCatching { URI(trimmed) }.getOrNull() ?: return null
    return trimmed.takeIf {
        uri.scheme?.lowercase(Locale.ROOT) in setOf("http", "https") &&
            !uri.host.isNullOrBlank() && uri.userInfo == null
    }
}
