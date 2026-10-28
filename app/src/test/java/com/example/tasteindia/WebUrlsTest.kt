package com.example.tasteindia

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WebUrlsTest {
    @Test fun `accepts trimmed web URLs only`() {
        assertEquals("https://example.com/recipe", validWebUrl(" https://example.com/recipe "))
        listOf("", "javascript:alert(1)", "file:///tmp/file", "https://", "not a url",
            "https://user:password@example.com").forEach { assertNull(validWebUrl(it)) }
    }
}
