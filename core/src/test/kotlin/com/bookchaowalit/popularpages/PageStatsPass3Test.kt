package com.bookchaowalit.popularpages

import kotlin.test.Test
import kotlin.test.assertEquals

class PageStatsPass3Test {

    @Test
    fun urlInsideTheQueryOfARelativePathIsNotTreatedAsTheHost() {
        assertEquals("/login", PageStats.normalizePath("/login?next=https://example.com/admin"))
        assertEquals("/r", PageStats.normalizePath("/r#https://x.com/a"))
        assertEquals("/go/https:/x.com", PageStats.normalizePath("/go/https://x.com"))
        assertEquals("/blog", PageStats.normalizePath("HTTPS://Example.com/Blog/"))
        assertEquals("/blog", PageStats.normalizePath("android-app://com.x/blog"))
    }

    @Test
    fun compactRoundsHalfUp() {
        assertEquals("1.2K", PageStats.compact(1_150))
        assertEquals("2.4K", PageStats.compact(2_350))
        assertEquals("1.2M", PageStats.compact(1_150_000))
        assertEquals("999.9K", PageStats.compact(999_949))
        assertEquals("1M", PageStats.compact(999_950))
    }

    @Test
    fun billionsUseABSuffix() {
        assertEquals("999.9M", PageStats.compact(999_949_999))
        assertEquals("1B", PageStats.compact(999_950_000))
        assertEquals("2.1B", PageStats.compact(Int.MAX_VALUE))
    }
}
