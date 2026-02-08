package com.cralert.app.data

object AlertEvaluator {
    fun shouldTrigger(alert: Alert, currentPrice: Double): Boolean {
        val last = alert.lastPrice
        if (last == null) {
            return when (alert.condition) {
                Condition.ABOVE -> currentPrice >= alert.targetPrice
                Condition.BELOW -> currentPrice <= alert.targetPrice
            }
        }
        return when (alert.condition) {
            Condition.ABOVE -> (last < alert.targetPrice && currentPrice >= alert.targetPrice) ||
                (last == alert.targetPrice && currentPrice > alert.targetPrice)
            Condition.BELOW -> (last > alert.targetPrice && currentPrice <= alert.targetPrice) ||
                (last == alert.targetPrice && currentPrice < alert.targetPrice)
        }
    }
}
