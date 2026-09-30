package com.bookchaowalit.popularpages

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PageStatsEdgeCaseTest {

    @Test
    fun querySlashesDoNotBecomeThePath() {
        assertEquals("/", PageStats.normalizePath("https://example.com?next=/admin/users"))
        assertEquals("/", PageStats.normalizePath("https://example.com#/section/2"))
        assertEquals("/a", PageStats.normalizePath("https://example.com/a?next=/b"))
    }

    @Test
    fun protocolRelativeUrlsDropTheHost() {
        assertEquals("/blog", PageStats.normalizePath("//cdn.example.com/blog/"))
    }

    @Test
    fun pathNormalisationCorners() {
        assertEquals("/", PageStats.normalizePath(""))
        assertEquals("/", PageStats.normalizePath("   "))
        assertEquals("/", PageStats.normalizePath("///"))
        assertEquals("/blog", PageStats.normalizePath("/Blog/Index.HTML"))
        assertEquals("/a/b", PageStats.normalizePath("a//b/"))
        assertEquals("/index.html.bak", PageStats.normalizePath("/index.html.bak"))
    }

    @Test
    fun windowsAreHalfOpen() {
        val views = listOf(PageView("/a", "v1", 100), PageView("/b", "v1", 200))
        assertEquals(listOf("/a"), PageStats.topPages(views, 100, 200).map { it.path })
        assertTrue(PageStats.topPages(views, 300, 400).isEmpty())
        assertFailsWith<IllegalArgumentException> { PageStats.topPages(views, 0, 1, limit = 0) }
    }

    @Test
    fun trendingNewPagesHaveNoGrowthPercent() {
        val views = listOf(
            PageView("/new", "v1", 950),
            PageView("/old", "v1", 850), PageView("/old", "v2", 860), PageView("/old", "v3", 960),
            PageView("/gone", "v1", 800),
        )
        val t = PageStats.trending(views, nowMs = 1000, windowMs = 100)
        assertEquals(listOf("/new", "/old"), t.map { it.path })
        assertNull(t[0].growthPercent)
        assertEquals(-50.0, t[1].growthPercent)
        assertFailsWith<IllegalArgumentException> { PageStats.trending(views, 1000, 0) }
    }

    @Test
    fun compactBoundaries() {
        assertEquals("0", PageStats.compact(0))
        assertEquals("999", PageStats.compact(999))
        assertEquals("1K", PageStats.compact(1_000))
        assertEquals("1K", PageStats.compact(1_049))
        assertEquals("1.1K", PageStats.compact(1_050))
        assertEquals("999.9K", PageStats.compact(999_949))
        assertEquals("1M", PageStats.compact(999_950))
        assertEquals("2.1B", PageStats.compact(Int.MAX_VALUE))
        assertFailsWith<IllegalArgumentException> { PageStats.compact(-1) }
    }
}
