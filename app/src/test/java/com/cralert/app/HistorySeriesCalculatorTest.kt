package com.cralert.app

import com.cralert.app.data.HistoricalPoint
import com.cralert.app.data.HistorySeries
import com.cralert.app.data.HistorySeriesCalculator
import com.cralert.app.data.HistorySource
import org.junit.Assert.assertEquals
import org.junit.Test

class HistorySeriesCalculatorTest {
    @Test
    fun `combine base and quote history`() {
        val base = HistorySeries(
            assetId = "base",
            interval = "d1",
            startMs = 0,
            endMs = 2,
            points = listOf(
                HistoricalPoint(1000, 10.0),
                HistoricalPoint(2000, 20.0)
            ),
            source = HistorySource.COINCAP,
            fetchedAtMs = 1
        )
        val quote = HistorySeries(
            assetId = "quote",
            interval = "d1",
            startMs = 0,
            endMs = 2,
            points = listOf(
                HistoricalPoint(1000, 2.0),
                HistoricalPoint(2000, 4.0)
            ),
            source = HistorySource.COINCAP,
            fetchedAtMs = 1
        )

        val combined = HistorySeriesCalculator.combineToPair(
            base = base,
            quote = quote,
            pairId = "base/quote",
            interval = "d1",
            startMs = 0,
            endMs = 2
        )

        assertEquals(2, combined.points.size)
        assertEquals(5.0, combined.points[0].price, 0.0001)
        assertEquals(5.0, combined.points[1].price, 0.0001)
    }
}
