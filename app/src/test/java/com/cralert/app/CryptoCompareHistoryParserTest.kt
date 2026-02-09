package com.cralert.app

import com.cralert.app.data.HistorySource
import com.cralert.app.data.remote.HistoryParsers
import org.junit.Assert.assertEquals
import org.junit.Test

class CryptoCompareHistoryParserTest {
    @Test
    fun `parse cryptoCompare history`() {
        val json = """
            {"Response":"Success","Data":{"Data":[
              {"time":1700000000,"close":10.0},
              {"time":1700086400,"close":12.5}
            ]}}
        """.trimIndent()

        val series = HistoryParsers.parseCryptoCompareHistory(
            assetId = "bitcoin",
            interval = "d1",
            startMs = 1699990000000,
            endMs = 1700090000000,
            body = json,
            fetchedAtMs = 1700090000000
        )

        assertEquals("bitcoin", series.assetId)
        assertEquals(HistorySource.CRYPTOCOMPARE, series.source)
        assertEquals(2, series.points.size)
        assertEquals(10.0, series.points[0].price, 0.0001)
    }
}
