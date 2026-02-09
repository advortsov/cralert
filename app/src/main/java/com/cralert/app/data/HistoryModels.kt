package com.cralert.app.data

data class HistoricalPoint(
    val timeMs: Long,
    val price: Double
)

enum class HistorySource {
    COINCAP,
    CRYPTOCOMPARE,
    MIXED
}

data class HistorySeries(
    val assetId: String,
    val interval: String,
    val startMs: Long,
    val endMs: Long,
    val points: List<HistoricalPoint>,
    val source: HistorySource,
    val fetchedAtMs: Long
)

data class HistoryCacheMeta(
    val assetId: String,
    val interval: String,
    val startMs: Long,
    val endMs: Long,
    val fetchedAtMs: Long,
    val source: HistorySource
)
