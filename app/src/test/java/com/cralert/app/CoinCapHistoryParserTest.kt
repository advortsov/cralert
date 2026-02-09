package com.cralert.app

import com.cralert.app.data.HistorySource
import com.cralert.app.data.remote.HistoryParsers
import org.junit.Assert.assertEquals
import org.junit.Test

class CoinCapHistoryParserTest {
    @Test
    fun `parse coinCap history`() {
        val json = """
            {"data":[
              {"priceUsd":"100.5","time":1700000000000},
              {"priceUsd":"101.5","time":1700086400000}
            ],"timestamp":1700086400000}
        """.trimIndent()

        val series = HistoryParsers.parseCoinCapHistory(
            assetId = "bitcoin",
            interval = "d1",
            startMs = 1699990000000,
            endMs = 1700090000000,
            body = json,
            fetchedAtMs = 1700090000000
        )

        assertEquals("bitcoin", series.assetId)
        assertEquals(HistorySource.COINCAP, series.source)
        assertEquals(2, series.points.size)
        assertEquals(100.5, series.points[0].price, 0.0001)
    }
}
