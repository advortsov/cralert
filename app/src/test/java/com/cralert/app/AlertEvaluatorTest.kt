package com.cralert.app

import com.cralert.app.data.Alert
import com.cralert.app.data.AlertEvaluator
import com.cralert.app.data.Condition
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertEvaluatorTest {

    @Test
    fun aboveConditionTriggersOnlyOnCrossingUp() {
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
            lastTriggeredAt = null,
            lastPrice = 90.0
        )

        val shouldTrigger = AlertEvaluator.shouldTrigger(alert, 120.0)

        assertTrue("Expected trigger when crossing above target", shouldTrigger)
    }

    @Test
    fun aboveConditionTriggersWhenCrossingToEqual() {
        val alert = Alert(
            id = "1a",
            assetId = "bitcoin",
            symbol = "BTC",
            name = "Bitcoin",
            quoteAssetId = "tether",
            quoteSymbol = "USDT",
            quoteName = "Tether",
            targetPrice = 100.0,
            condition = Condition.ABOVE,
            enabled = true,
            lastTriggeredAt = null,
            lastPrice = 90.0
        )

        val shouldTrigger = AlertEvaluator.shouldTrigger(alert, 100.0)

        assertTrue("Expected trigger when crossing to equal target", shouldTrigger)
    }

    @Test
    fun belowConditionTriggersOnlyOnCrossingDown() {
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
            lastTriggeredAt = null,
            lastPrice = 110.0
        )

        val shouldTrigger = AlertEvaluator.shouldTrigger(alert, 80.0)

        assertTrue("Expected trigger when crossing below target", shouldTrigger)
    }

    @Test
    fun belowConditionTriggersWhenCrossingToEqual() {
        val alert = Alert(
            id = "2a",
            assetId = "bitcoin",
            symbol = "BTC",
            name = "Bitcoin",
            quoteAssetId = "tether",
            quoteSymbol = "USDT",
            quoteName = "Tether",
            targetPrice = 100.0,
            condition = Condition.BELOW,
            enabled = true,
            lastTriggeredAt = null,
            lastPrice = 110.0
        )

        val shouldTrigger = AlertEvaluator.shouldTrigger(alert, 100.0)

        assertTrue("Expected trigger when crossing to equal target", shouldTrigger)
    }

    @Test
    fun doesNotTriggerWithoutCrossing() {
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
            lastTriggeredAt = null,
            lastPrice = 120.0
        )

        val shouldTrigger = AlertEvaluator.shouldTrigger(alert, 130.0)

        assertFalse("Expected no trigger when price stays above target", shouldTrigger)
    }

    @Test
    fun doesNotTriggerWhenStaysAtEqual() {
        val alert = Alert(
            id = "3a",
            assetId = "bitcoin",
            symbol = "BTC",
            name = "Bitcoin",
            quoteAssetId = "tether",
            quoteSymbol = "USDT",
            quoteName = "Tether",
            targetPrice = 100.0,
            condition = Condition.ABOVE,
            enabled = true,
            lastTriggeredAt = null,
            lastPrice = 100.0
        )

        val shouldTrigger = AlertEvaluator.shouldTrigger(alert, 100.0)

        assertFalse("Expected no trigger when price stays equal", shouldTrigger)
    }

    @Test
    fun triggersWhenNoLastPriceAndAlreadyBeyond() {
        val alert = Alert(
            id = "4",
            assetId = "bitcoin",
            symbol = "BTC",
            name = "Bitcoin",
            quoteAssetId = "tether",
            quoteSymbol = "USDT",
            quoteName = "Tether",
            targetPrice = 100.0,
            condition = Condition.ABOVE,
            enabled = true,
            lastTriggeredAt = null,
            lastPrice = null
        )

        val shouldTrigger = AlertEvaluator.shouldTrigger(alert, 120.0)

        assertTrue("Expected trigger when no last price and already above target", shouldTrigger)
    }

    @Test
    fun doesNotTriggerWhenNoLastPriceAndNotBeyond() {
        val alert = Alert(
            id = "5",
            assetId = "bitcoin",
            symbol = "BTC",
            name = "Bitcoin",
            quoteAssetId = "tether",
            quoteSymbol = "USDT",
            quoteName = "Tether",
            targetPrice = 100.0,
            condition = Condition.BELOW,
            enabled = true,
            lastTriggeredAt = null,
            lastPrice = null
        )

        val shouldTrigger = AlertEvaluator.shouldTrigger(alert, 120.0)

        assertFalse("Expected no trigger when no last price and not beyond target", shouldTrigger)
    }
}
