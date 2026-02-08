package com.cralert.app

import com.cralert.app.data.Alert
import com.cralert.app.data.Condition
import com.cralert.app.data.local.AlertJsonAdapter
import org.junit.Assert.assertEquals
import org.junit.Test

class AlertJsonAdapterTest {

    @Test
    fun serializesAndParsesAlert() {
        val alert = Alert(
            id = "1",
            assetId = "bitcoin",
            symbol = "BTC",
            name = "Bitcoin",
            quoteAssetId = "tether",
            quoteSymbol = "USDT",
            quoteName = "Tether",
            targetPrice = 123.45,
            condition = Condition.ABOVE,
            enabled = true,
            lastTriggeredAt = 1000L,
            lastPrice = 120.0
        )

        val json = AlertJsonAdapter.toJson(alert)
        val parsed = AlertJsonAdapter.fromJson(json)

        assertEquals(alert, parsed)
    }
}
