package com.cralert.app.data

data class Alert(
    val id: String,
    val assetId: String,
    val symbol: String,
    val name: String,
    val quoteAssetId: String,
    val quoteSymbol: String,
    val quoteName: String,
    val targetPrice: Double,
    val condition: Condition,
    val enabled: Boolean,
    val lastTriggeredAt: Long?
)

data class Asset(
    val id: String,
    val symbol: String,
    val name: String,
    val priceUsd: Double
)

data class PriceQuote(
    val assetId: String,
    val priceUsd: Double,
    val timestamp: Long
)

enum class Condition {
    ABOVE,
    BELOW
}
