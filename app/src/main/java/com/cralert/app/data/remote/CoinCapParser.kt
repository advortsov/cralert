package com.cralert.app.data.remote

import com.cralert.app.data.Asset
import org.json.JSONArray
import org.json.JSONObject

object CoinCapParser {
    fun parseAssets(json: String): List<Asset> {
        val root = JSONObject(json)
        val data = root.optJSONArray("data") ?: JSONArray()
        val assets = mutableListOf<Asset>()
        for (i in 0 until data.length()) {
            val item = data.getJSONObject(i)
            assets.add(
                Asset(
                    id = item.getString("id"),
                    symbol = item.getString("symbol"),
                    name = item.getString("name"),
                    priceUsd = item.optString("priceUsd", "0").toDoubleOrNull() ?: 0.0
                )
            )
        }
        return assets
    }
}
