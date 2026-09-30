package com.bookchaowalit.popularpages

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PageStatsTest {
    @Test
    fun normalizesPaths() {
        assertEquals("/blog/post", PageStats.normalizePath("https://example.com//Blog/Post/?utm=1#top"))
        assertEquals("/", PageStats.normalizePath("https://example.com"))
        assertEquals("/", PageStats.normalizePath("/index.html"))
        assertEquals("/docs", PageStats.normalizePath("/docs/index.html"))
        assertEquals("/", PageStats.normalizePath(""))
    }

    @Test
    fun ranksTopPagesInWindow() {
        val views = listOf(
            PageView("/a", "v1", 10), PageView("/a/", "v1", 11), PageView("/A?x=1", "v2", 12),
            PageView("/b", "v1", 13), PageView("/b", "v2", 14), PageView("/b", "v3", 15),
            PageView("/c", "v1", 16),
            PageView("/c", "v1", 500), // outside window
        )
        val top = PageStats.topPages(views, 0, 100)
        assertEquals(listOf(PageRank("/b", 3, 3), PageRank("/a", 3, 2), PageRank("/c", 1, 1)), top)
        assertEquals(1, PageStats.topPages(views, 0, 100, limit = 1).size)
    }

    @Test
    fun trendingComparesWindows() {
        val views = buildList {
            repeat(2) { add(PageView("/old", "v", 5)) } // previous window [0,10)
            repeat(5) { add(PageView("/old", "v", 15)) } // current window [10,20)
            repeat(3) { add(PageView("/new", "v", 15)) }
            repeat(4) { add(PageView("/fading", "v", 5)) }
            add(PageView("/fading", "v", 15))
        }
        val t = PageStats.trending(views, nowMs = 20, windowMs = 10)
        assertEquals(listOf("/new", "/old", "/fading"), t.map { it.path })
        assertEquals(150.0, t[1].growthPercent)
        assertNull(t[0].growthPercent)
        assertEquals(-75.0, t[2].growthPercent)
    }

    @Test
    fun compactNumbers() {
        assertEquals("999", PageStats.compact(999))
        assertEquals("1K", PageStats.compact(1_000))
        assertEquals("1.3K", PageStats.compact(1_250))
        assertEquals("2M", PageStats.compact(2_000_000))
        assertEquals("999.9K", PageStats.compact(999_949))
        assertEquals("1M", PageStats.compact(999_950))
        assertEquals("12.3M", PageStats.compact(12_345_678))
    }
}
