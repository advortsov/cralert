package com.cralert.app.data

import android.content.Context
import android.util.Log
import com.cralert.app.worker.NotificationHelper

data class AlertCheckStats(
    val triggered: Int,
    val skippedNoPrice: Int,
    val skippedDisabled: Int
)

class AlertCheckEngine(
    private val alertRepository: AlertRepository
) {
    fun evaluate(
        context: Context,
        alerts: List<Alert>,
        prices: Map<String, Double>,
        pricesUpdatedAt: Long
    ): AlertCheckStats {
        val enabledAlerts = alerts.filter { it.enabled }
        var triggeredCount = 0
        var skippedNoPrice = 0
        val skippedDisabled = alerts.size - enabledAlerts.size
        val now = System.currentTimeMillis()

        enabledAlerts.forEach { alert ->
            val price = PriceCalculator.currentPrice(alert, prices)
            if (price == null) {
                skippedNoPrice += 1
                Log.w(TAG, "Skip no price ${alert.symbol}/${alert.quoteSymbol} id=${alert.id}")
                return@forEach
            }
            val shouldTrigger = AlertEvaluator.shouldTrigger(alert, price)
            Log.i(
                TAG,
                "Eval ${alert.symbol}/${alert.quoteSymbol} price=$price target=${alert.targetPrice} cond=${alert.condition} last=${alert.lastPrice} trigger=$shouldTrigger"
            )
            if (shouldTrigger) {
                NotificationHelper.sendAlert(
                    context,
                    alert,
                    price,
                    pricesUpdatedAt
                )
                val updated = alert.copy(lastTriggeredAt = now, lastPrice = price)
                alertRepository.updateAlert(updated)
                triggeredCount += 1
                Log.i(TAG, "Triggered ${alert.symbol}/${alert.quoteSymbol} id=${alert.id}")
            } else {
                val updated = alert.copy(lastPrice = price)
                alertRepository.updateAlert(updated)
            }
        }

        return AlertCheckStats(
            triggered = triggeredCount,
            skippedNoPrice = skippedNoPrice,
            skippedDisabled = skippedDisabled
        )
    }

    companion object {
        private const val TAG = "AlertCheckEngine"
    }
}
