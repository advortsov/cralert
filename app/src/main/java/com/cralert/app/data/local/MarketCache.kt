package com.cralert.app.data.local

import android.content.SharedPreferences
import com.cralert.app.data.Asset
import org.json.JSONArray
import org.json.JSONObject

class MarketCache(private val prefs: SharedPreferences) {

    fun getCachedAssets(): List<Asset> {
        val raw = prefs.getString(KEY_ASSETS, null) ?: return emptyList()
        val json = JSONArray(raw)
        val assets = mutableListOf<Asset>()
        for (i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            assets.add(
                Asset(
                    id = obj.getString("id"),
                    symbol = obj.getString("symbol"),
                    name = obj.getString("name"),
                    priceUsd = obj.optDouble("priceUsd", 0.0)
                )
            )
        }
        return assets
    }

    fun putAssets(assets: List<Asset>) {
        val json = JSONArray()
        assets.forEach { asset ->
            val obj = JSONObject()
            obj.put("id", asset.id)
            obj.put("symbol", asset.symbol)
            obj.put("name", asset.name)
            obj.put("priceUsd", asset.priceUsd)
            json.put(obj)
        }
        prefs.edit()
            .putString(KEY_ASSETS, json.toString())
            .putLong(KEY_ASSETS_TS, System.currentTimeMillis())
            .apply()
    }

    fun getAssetsTimestamp(): Long = prefs.getLong(KEY_ASSETS_TS, 0L)

    fun getCachedPrices(): Map<String, Double> {
        val raw = prefs.getString(KEY_PRICES, null) ?: return emptyMap()
        val json = JSONObject(raw)
        val map = mutableMapOf<String, Double>()
        json.keys().forEach { key ->
            map[key] = json.optDouble(key, 0.0)
        }
        return map
    }

    fun putPrices(prices: Map<String, Double>) {
        val json = JSONObject()
        prices.forEach { (key, value) -> json.put(key, value) }
        prefs.edit()
            .putString(KEY_PRICES, json.toString())
            .putLong(KEY_PRICES_TS, System.currentTimeMillis())
            .apply()
    }

    fun getPricesTimestamp(): Long = prefs.getLong(KEY_PRICES_TS, 0L)

    private companion object {
        private const val KEY_ASSETS = "assets_json"
        private const val KEY_ASSETS_TS = "assets_timestamp"
        private const val KEY_PRICES = "prices_json"
        private const val KEY_PRICES_TS = "prices_timestamp"
    }
}
