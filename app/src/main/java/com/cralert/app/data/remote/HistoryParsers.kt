package com.cralert.app.data.remote

import com.cralert.app.data.HistoricalPoint
import com.cralert.app.data.HistorySeries
import com.cralert.app.data.HistorySource
import org.json.JSONArray
import org.json.JSONObject

object HistoryParsers {
    fun parseCoinCapHistory(
        assetId: String,
        interval: String,
        startMs: Long,
        endMs: Long,
        body: String,
        fetchedAtMs: Long
    ): HistorySeries {
        val root = JSONObject(body)
        val data = root.optJSONArray("data") ?: JSONArray()
        val points = mutableListOf<HistoricalPoint>()
        for (i in 0 until data.length()) {
            val item = data.getJSONObject(i)
            val price = item.optString("priceUsd").toDoubleOrNull() ?: continue
            val time = item.optLong("time")
            if (time == 0L) continue
            if (time < startMs || time > endMs) continue
            points.add(HistoricalPoint(time, price))
        }
        return HistorySeries(
            assetId = assetId,
            interval = interval,
            startMs = startMs,
            endMs = endMs,
            points = points.sortedBy { it.timeMs },
            source = HistorySource.COINCAP,
            fetchedAtMs = fetchedAtMs
        )
    }

    fun parseCryptoCompareHistory(
        assetId: String,
        interval: String,
        startMs: Long,
        endMs: Long,
        body: String,
        fetchedAtMs: Long
    ): HistorySeries {
        val root = JSONObject(body)
        val response = root.optString("Response")
        if (response.equals("Error", ignoreCase = true)) {
            val message = root.optString("Message")
            throw IllegalStateException(message.ifBlank { "CryptoCompare error" })
        }
        val data = root.optJSONObject("Data")?.optJSONArray("Data") ?: JSONArray()
        val points = mutableListOf<HistoricalPoint>()
        for (i in 0 until data.length()) {
            val item = data.getJSONObject(i)
            val timeSec = item.optLong("time")
            val close = item.optDouble("close", Double.NaN)
            if (timeSec == 0L || close.isNaN()) continue
            val timeMs = timeSec * 1000L
            if (timeMs < startMs || timeMs > endMs) continue
            points.add(HistoricalPoint(timeMs, close))
        }
        return HistorySeries(
            assetId = assetId,
            interval = interval,
            startMs = startMs,
            endMs = endMs,
            points = points.sortedBy { it.timeMs },
            source = HistorySource.CRYPTOCOMPARE,
            fetchedAtMs = fetchedAtMs
        )
    }
}
