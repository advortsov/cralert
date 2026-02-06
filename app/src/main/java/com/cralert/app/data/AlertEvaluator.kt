package com.cralert.app.data

object AlertEvaluator {
    fun shouldTrigger(
        alert: Alert,
        currentPrice: Double,
        now: Long,
        minIntervalMs: Long
    ): Boolean {
        val enoughTime = alert.lastTriggeredAt?.let { now - it > minIntervalMs } ?: true
        if (!enoughTime) return false
        return when (alert.condition) {
            Condition.ABOVE -> currentPrice >= alert.targetPrice
            Condition.BELOW -> currentPrice <= alert.targetPrice
        }
    }
}
