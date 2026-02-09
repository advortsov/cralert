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

class CoinCapHistoryProvider(
    private val settings: SettingsRepository
) : HistoryProvider {

    companion object {
        private const val TAG = "CoinCapHistoryProvider"
    }

    override suspend fun fetchHistory(
        assetId: String,
        assetSymbol: String,
        interval: String,
        startMs: Long,
        endMs: Long
    ): ApiResult<HistorySeries> {
        return withContext(Dispatchers.IO) {
            val baseUrl = settings.getApiBaseUrl().trimEnd('/')
            val url = "$baseUrl/assets/$assetId/history?interval=$interval&start=$startMs&end=$endMs"
            android.util.Log.d(TAG, "Request: $url")
            try {
                val response = getResponse(url)
                android.util.Log.d(TAG, "HTTP ${response.httpCode}")
                if (response.httpCode in 200..299) {
                    val series = HistoryParsers.parseCoinCapHistory(
                        assetId = assetId,
                        interval = interval,
                        startMs = startMs,
                        endMs = endMs,
                        body = response.body,
                        fetchedAtMs = System.currentTimeMillis()
                    )
                    ApiResult.success(series, response.httpCode)
                } else {
                    val error = response.body.take(300)
                    ApiResult.failure(emptySeries(assetId, interval, startMs, endMs), response.httpCode, error)
                }
            } catch (ex: Exception) {
                android.util.Log.e(TAG, "Request failed: ${ex.message}", ex)
                ApiResult.failure(emptySeries(assetId, interval, startMs, endMs), null, ex.message)
            }
        }
    }

    private fun getResponse(urlString: String): ApiResponse {
        val url = URL(urlString)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = settings.getConnectTimeoutMs()
            readTimeout = settings.getReadTimeoutMs()
            setRequestProperty("accept", "application/json")
            val token = settings.getApiToken()
            if (token.isNotBlank()) {
                setRequestProperty("Authorization", "Bearer $token")
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
            source = com.cralert.app.data.HistorySource.COINCAP,
            fetchedAtMs = System.currentTimeMillis()
        )
    }
}
