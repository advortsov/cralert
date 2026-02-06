package com.cralert.app

import com.cralert.app.data.Alert
import com.cralert.app.data.AlertEvaluator
import com.cralert.app.data.Condition
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertEvaluatorTest {

    @Test
    fun aboveConditionTriggersWhenPriceIsHigher() {
        val alert = Alert(
            id = "1",
            assetId = "bitcoin",
            symbol = "BTC",
            name = "Bitcoin",
            quoteAssetId = "tether",
            quoteSymbol = "USDT",
            quoteName = "Tether",
            targetPrice = 100.0,
            condition = Condition.ABOVE,
            enabled = true,
            lastTriggeredAt = null
        )

        val shouldTrigger = AlertEvaluator.shouldTrigger(alert, 120.0, System.currentTimeMillis(), 15 * 60 * 1000L)

        assertTrue("Expected trigger when price is above target", shouldTrigger)
    }

    @Test
    fun belowConditionTriggersWhenPriceIsLower() {
        val alert = Alert(
            id = "2",
            assetId = "bitcoin",
            symbol = "BTC",
            name = "Bitcoin",
            quoteAssetId = "tether",
            quoteSymbol = "USDT",
            quoteName = "Tether",
            targetPrice = 100.0,
            condition = Condition.BELOW,
            enabled = true,
            lastTriggeredAt = null
        )

        val shouldTrigger = AlertEvaluator.shouldTrigger(alert, 80.0, System.currentTimeMillis(), 15 * 60 * 1000L)

        assertTrue("Expected trigger when price is below target", shouldTrigger)
    }

    @Test
    fun doesNotTriggerTooFrequently() {
        val now = System.currentTimeMillis()
        val alert = Alert(
            id = "3",
            assetId = "bitcoin",
            symbol = "BTC",
            name = "Bitcoin",
            quoteAssetId = "tether",
            quoteSymbol = "USDT",
            quoteName = "Tether",
            targetPrice = 100.0,
            condition = Condition.ABOVE,
            enabled = true,
            lastTriggeredAt = now - 5 * 60 * 1000L
        )

        val shouldTrigger = AlertEvaluator.shouldTrigger(alert, 200.0, now, 15 * 60 * 1000L)

        assertFalse("Expected no trigger when last alert was too recent", shouldTrigger)
    }
}
