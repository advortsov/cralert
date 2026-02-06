package com.cralert.app.data

object PriceCalculator {
    fun currentPrice(alert: Alert, pricesUsd: Map<String, Double>): Double? {
        val baseUsd = pricesUsd[alert.assetId] ?: return null
        val quoteUsd = quotePriceUsd(alert, pricesUsd) ?: return null
        if (quoteUsd == 0.0) return null
        return baseUsd / quoteUsd
    }

    private fun quotePriceUsd(alert: Alert, pricesUsd: Map<String, Double>): Double? {
        if (alert.quoteSymbol.equals("USD", true) || alert.quoteAssetId.equals("usd", true)) {
            return 1.0
        }
        return pricesUsd[alert.quoteAssetId]
    }
}
