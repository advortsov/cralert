package com.cralert.app.data

object HistorySeriesCalculator {
    fun combineToPair(
        base: HistorySeries,
        quote: HistorySeries,
        pairId: String,
        interval: String,
        startMs: Long,
        endMs: Long
    ): HistorySeries {
        if (base.points.isEmpty() || quote.points.isEmpty()) {
            return HistorySeries(
                assetId = pairId,
                interval = interval,
                startMs = startMs,
                endMs = endMs,
                points = emptyList(),
                source = HistorySource.MIXED,
                fetchedAtMs = maxOf(base.fetchedAtMs, quote.fetchedAtMs)
            )
        }
        val quoteMap = quote.points.associateBy { it.timeMs }
        val merged = base.points.mapNotNull { basePoint ->
            val quotePoint = quoteMap[basePoint.timeMs] ?: return@mapNotNull null
            if (quotePoint.price == 0.0) return@mapNotNull null
            HistoricalPoint(basePoint.timeMs, basePoint.price / quotePoint.price)
        }.sortedBy { it.timeMs }

        val source = if (base.source == quote.source) base.source else HistorySource.MIXED
        return HistorySeries(
            assetId = pairId,
            interval = interval,
            startMs = startMs,
            endMs = endMs,
            points = merged,
            source = source,
            fetchedAtMs = maxOf(base.fetchedAtMs, quote.fetchedAtMs)
        )
    }
}
