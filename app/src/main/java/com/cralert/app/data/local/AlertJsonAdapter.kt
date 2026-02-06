package com.cralert.app.data.local

import com.cralert.app.data.Alert
import com.cralert.app.data.Condition
import org.json.JSONObject

object AlertJsonAdapter {
    fun toJson(alert: Alert): JSONObject {
        val json = JSONObject()
        json.put("id", alert.id)
        json.put("assetId", alert.assetId)
        json.put("symbol", alert.symbol)
        json.put("name", alert.name)
        json.put("quoteAssetId", alert.quoteAssetId)
        json.put("quoteSymbol", alert.quoteSymbol)
        json.put("quoteName", alert.quoteName)
        json.put("targetPrice", alert.targetPrice)
        json.put("condition", alert.condition.name)
        json.put("enabled", alert.enabled)
        if (alert.lastTriggeredAt != null) {
            json.put("lastTriggeredAt", alert.lastTriggeredAt)
        }
        return json
    }

    fun fromJson(json: JSONObject): Alert {
        return Alert(
            id = json.getString("id"),
            assetId = json.getString("assetId"),
            symbol = json.getString("symbol"),
            name = json.getString("name"),
            quoteAssetId = json.optString("quoteAssetId", "tether"),
            quoteSymbol = json.optString("quoteSymbol", "USDT"),
            quoteName = json.optString("quoteName", "Tether"),
            targetPrice = json.getDouble("targetPrice"),
            condition = Condition.valueOf(json.getString("condition")),
            enabled = json.optBoolean("enabled", true),
            lastTriggeredAt = if (json.has("lastTriggeredAt")) json.getLong("lastTriggeredAt") else null
        )
    }
}
