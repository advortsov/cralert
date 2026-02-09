package com.cralert.app.data.local

import android.content.Context
import com.cralert.app.data.HistoricalPoint
import com.cralert.app.data.HistoryCacheMeta
import com.cralert.app.data.HistorySeries
import com.cralert.app.data.HistorySource
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class HistoryCache(private val context: Context) {
    private val historyDir: File by lazy {
        File(context.cacheDir, "history").apply { mkdirs() }
    }

    fun read(assetId: String, interval: String): HistorySeries? {
        val file = File(historyDir, fileName(assetId, interval))
        if (!file.exists()) return null
        val content = file.readText()
        if (content.isBlank()) return null
        val root = JSONObject(content)
        val metaJson = root.optJSONObject("meta") ?: return null
        val pointsJson = root.optJSONArray("points") ?: JSONArray()
        val meta = HistoryCacheMeta(
            assetId = metaJson.optString("assetId"),
            interval = metaJson.optString("interval"),
            startMs = metaJson.optLong("startMs"),
            endMs = metaJson.optLong("endMs"),
            fetchedAtMs = metaJson.optLong("fetchedAtMs"),
            source = HistorySource.valueOf(metaJson.optString("source", "COINCAP"))
        )
        val points = mutableListOf<HistoricalPoint>()
        for (i in 0 until pointsJson.length()) {
            val item = pointsJson.getJSONObject(i)
            val timeMs = item.optLong("timeMs")
            val price = item.optDouble("price")
            if (timeMs == 0L) continue
            points.add(HistoricalPoint(timeMs, price))
        }
        return HistorySeries(
            assetId = meta.assetId,
            interval = meta.interval,
            startMs = meta.startMs,
            endMs = meta.endMs,
            points = points.sortedBy { it.timeMs },
            source = meta.source,
            fetchedAtMs = meta.fetchedAtMs
        )
    }

    fun write(series: HistorySeries) {
        val file = File(historyDir, fileName(series.assetId, series.interval))
        val root = JSONObject()
        val meta = JSONObject()
        meta.put("assetId", series.assetId)
        meta.put("interval", series.interval)
        meta.put("startMs", series.startMs)
        meta.put("endMs", series.endMs)
        meta.put("fetchedAtMs", series.fetchedAtMs)
        meta.put("source", series.source.name)
        root.put("meta", meta)
        val pointsJson = JSONArray()
        series.points.forEach { point ->
            val obj = JSONObject()
            obj.put("timeMs", point.timeMs)
            obj.put("price", point.price)
            pointsJson.put(obj)
        }
        root.put("points", pointsJson)
        file.writeText(root.toString())
    }

    private fun fileName(assetId: String, interval: String): String {
        return "${assetId}_${interval}.json"
    }
}
