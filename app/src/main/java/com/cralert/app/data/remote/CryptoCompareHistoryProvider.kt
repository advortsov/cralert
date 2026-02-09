package com.cralert.app.data.remote

import com.cralert.app.data.HistorySeries
import com.cralert.app.data.local.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class CryptoCompareHistoryProvider(
    private val settings: SettingsRepository
) : HistoryProvider {

    companion object {
        private const val TAG = "CryptoCompareHistory"
        private const val MAX_LIMIT = 2000
    }

    override suspend fun fetchHistory(
        assetId: String,
        assetSymbol: String,
        interval: String,
        startMs: Long,
        endMs: Long
    ): ApiResult<HistorySeries> {
        return withContext(Dispatchers.IO) {
            val symbol = assetSymbol.trim().uppercase()
            if (symbol.isBlank()) {
                return@withContext ApiResult.failure(
                    emptySeries(assetId, interval, startMs, endMs),
                    null,
                    "Symbol is empty"
                )
            }
            try {
                val series = fetchPaged(symbol, assetId, interval, startMs, endMs)
                ApiResult.success(series, 200)
            } catch (ex: Exception) {
                android.util.Log.e(TAG, "Request failed: ${ex.message}", ex)
                ApiResult.failure(emptySeries(assetId, interval, startMs, endMs), null, ex.message)
            }
        }
    }

    private fun fetchPaged(
        symbol: String,
        assetId: String,
        interval: String,
        startMs: Long,
        endMs: Long
    ): HistorySeries {
        val baseUrl = settings.getCryptoCompareBaseUrl().trimEnd('/')
        val startSec = startMs / 1000L
        val endSec = endMs / 1000L
        val points = mutableListOf<com.cralert.app.data.HistoricalPoint>()
        var toTs = endSec
        var loops = 0

        while (true) {
            loops += 1
            val url = buildUrl(baseUrl, symbol, toTs)
            android.util.Log.d(TAG, "Request: $url")
            val response = getResponse(url)
            if (response.httpCode !in 200..299) {
                throw IllegalStateException("HTTP ${response.httpCode} ${response.body.take(120)}")
            }
            val series = HistoryParsers.parseCryptoCompareHistory(
                assetId = assetId,
                interval = interval,
                startMs = startMs,
                endMs = endMs,
                body = response.body,
                fetchedAtMs = System.currentTimeMillis()
            )
            points.addAll(series.points)
            val earliest = series.points.minByOrNull { it.timeMs }
            if (earliest == null) break
            val earliestSec = earliest.timeMs / 1000L
            if (earliestSec <= startSec) break
            if (series.points.size < MAX_LIMIT) break
            toTs = earliestSec - 1
            if (loops >= 5) break
        }

        val filtered = points
            .filter { it.timeMs in startMs..endMs }
            .distinctBy { it.timeMs }
            .sortedBy { it.timeMs }

        return HistorySeries(
            assetId = assetId,
            interval = interval,
            startMs = startMs,
            endMs = endMs,
            points = filtered,
            source = com.cralert.app.data.HistorySource.CRYPTOCOMPARE,
            fetchedAtMs = System.currentTimeMillis()
        )
    }

    private fun buildUrl(baseUrl: String, symbol: String, toTs: Long): String {
        val encoded = URLEncoder.encode(symbol, "UTF-8")
        val params = mutableListOf(
            "fsym=$encoded",
            "tsym=USD",
            "limit=$MAX_LIMIT",
            "toTs=$toTs"
        )
        return "$baseUrl/histoday?${params.joinToString("&")}".also {
            // noop
        }
    }

    private fun getResponse(urlString: String): ApiResponse {
        val url = URL(urlString)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = settings.getConnectTimeoutMs()
            readTimeout = settings.getReadTimeoutMs()
            setRequestProperty("accept", "application/json")
            val token = settings.getCryptoCompareApiKey()
            if (token.isNotBlank()) {
                setRequestProperty("authorization", "Apikey $token")
            }
        }

        return try {
            val code = connection.responseCode
            val stream = if (code in 200..299) {
                BufferedInputStream(connection.inputStream)
            } else {
                BufferedInputStream(connection.errorStream)
            }
            val reader = BufferedReader(InputStreamReader(stream))
            val response = reader.readText()
            reader.close()
            ApiResponse(response, code)
        } finally {
            connection.disconnect()
        }
    }

    private data class ApiResponse(
        val body: String,
        val httpCode: Int
    )

    private fun emptySeries(
        assetId: String,
        interval: String,
        startMs: Long,
        endMs: Long
    ): HistorySeries {
        return HistorySeries(
            assetId = assetId,
            interval = interval,
            startMs = startMs,
            endMs = endMs,
            points = emptyList(),
            source = com.cralert.app.data.HistorySource.CRYPTOCOMPARE,
            fetchedAtMs = System.currentTimeMillis()
        )
    }
}
