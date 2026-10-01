package com.bookchaowalit.popularpages

/** One page view. [visitorId] should be an anonymous, rotating id — never PII. */
data class PageView(val path: String, val visitorId: String, val timestampMs: Long)

data class PageRank(val path: String, val views: Int, val uniqueVisitors: Int)

data class Trend(val path: String, val current: Int, val previous: Int) {
    /** Growth in percent; null when there were no views in the previous window (new page). */
    val growthPercent: Double? get() = if (previous == 0) null else (current - previous) * 100.0 / previous
}

object PageStats {
    /**
     * Normalises a path or URL for grouping: drops scheme/host, query and
     * fragment, collapses duplicate slashes, lower-cases, strips a trailing
     * slash and `index.html`. Root becomes "/".
     */
    fun normalizePath(raw: String): String {
        var s = raw.trim()
        // Host part: after "scheme://" or a protocol-relative "//", up to the first '/', '?' or '#'.
        // Only a real "scheme://" prefix counts: a URL inside the query or path
        // ("/login?next=https://x.com/admin") must not be mistaken for the host.
        val scheme = SCHEME.find(s)?.value?.length ?: -1
        val hostStart = when {
            scheme >= 0 -> scheme
            s.startsWith("//") -> 2
            else -> -1
        }
        if (hostStart >= 0) {
            val end = s.indexOfAny(charArrayOf('/', '?', '#'), hostStart)
            s = if (end < 0) "/" else s.substring(end)
        }
        s = s.substringBefore('#').substringBefore('?').lowercase()
        s = "/" + s.split('/').filter { it.isNotEmpty() }.joinToString("/")
        s = s.removeSuffix("/index.html").ifEmpty { "/" }
        return s
    }

    /** Top [limit] pages in [fromMs, toMs) by views, ties broken by unique visitors then path. */
    fun topPages(views: List<PageView>, fromMs: Long, toMs: Long, limit: Int = 10): List<PageRank> {
        require(limit > 0) { "limit must be positive" }
        return views.asSequence()
            .filter { it.timestampMs in fromMs until toMs }
            .groupBy { normalizePath(it.path) }
            .map { (path, vs) -> PageRank(path, vs.size, vs.map { it.visitorId }.toSet().size) }
            .sortedWith(compareByDescending<PageRank> { it.views }.thenByDescending { it.uniqueVisitors }.thenBy { it.path })
            .take(limit)
    }

    /**
     * Compares the window [nowMs - windowMs, nowMs) with the one before it.
     * Pages are ordered by absolute gain in views; pages with no current views are dropped.
     */
    fun trending(views: List<PageView>, nowMs: Long, windowMs: Long, limit: Int = 10): List<Trend> {
        require(windowMs > 0 && limit > 0)
        val cur = HashMap<String, Int>()
        val prev = HashMap<String, Int>()
        for (v in views) {
            val p = normalizePath(v.path)
            when (v.timestampMs) {
                in (nowMs - windowMs) until nowMs -> cur[p] = (cur[p] ?: 0) + 1
                in (nowMs - 2 * windowMs) until (nowMs - windowMs) -> prev[p] = (prev[p] ?: 0) + 1
            }
        }
        return cur.map { (p, c) -> Trend(p, c, prev[p] ?: 0) }
            .sortedWith(compareByDescending<Trend> { it.current - it.previous }.thenBy { it.path })
            .take(limit)
    }

    private val SCHEME = Regex("^[A-Za-z][A-Za-z0-9+.-]*://")

    /** Compact counter label: 999 -> "999", 1_250 -> "1.3K", 2_000_000 -> "2M", 1_500_000_000 -> "1.5B". */
    fun compact(n: Int): String {
        require(n >= 0)
        fun one(value: Double, suffix: String): String {
            val rounded = kotlin.math.floor(value * 10 + 0.5) / 10 // half-up
            val text = if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
            return text + suffix
        }
        return when {
            n < 1_000 -> n.toString()
            n < 999_950 -> one(n / 1_000.0, "K")
            n < 999_950_000 -> one(n / 1_000_000.0, "M")
            else -> one(n / 1_000_000_000.0, "B")
        }
    }
}
