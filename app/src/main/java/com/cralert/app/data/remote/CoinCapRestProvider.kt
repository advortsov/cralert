package com.cralert.app.data.remote

import com.cralert.app.data.Asset
import com.cralert.app.data.local.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class CoinCapRestProvider(
    private val settings: SettingsRepository
) : QuotesRestProvider {

    companion object {
        private const val TAG = "CoinCapRestProvider"
    }

    override suspend fun fetchAssets(limit: Int, ids: List<String>?): ApiResult<List<Asset>> {
        return withContext(Dispatchers.IO) {
            val url = buildUrl(limit, ids)
            android.util.Log.d(TAG, "Request: $url")
            try {
                val response = getResponseWithRetry(url)
                android.util.Log.d(TAG, "HTTP ${response.httpCode}")
                android.util.Log.d(TAG, "Response size: ${response.body.length}")
                if (response.httpCode in 200..299) {
                    val assets = CoinCapParser.parseAssets(response.body)
                    ApiResult.success(assets, response.httpCode)
                } else {
                    val error = response.body.take(300)
                    ApiResult.failure(emptyList(), response.httpCode, error)
                }
            } catch (ex: Exception) {
                android.util.Log.e(TAG, "Request failed: ${ex.message}", ex)
                ApiResult.failure(emptyList(), null, ex.message)
            }
        }
    }

    private fun getResponseWithRetry(urlString: String): ApiResponse {
        val retryAttempts = settings.getRetryAttempts()
        val baseDelay = settings.getRetryBaseDelayMs()
        val maxDelay = settings.getRetryMaxDelayMs()
        var lastResponse: ApiResponse? = null
        var lastError: Exception? = null
        for (attempt in 0..retryAttempts) {
            if (attempt > 0) {
                val backoff = baseDelay * (1 shl (attempt - 1))
                val delayMs = kotlin.math.min(backoff, maxDelay)
                Thread.sleep(delayMs)
                android.util.Log.w(TAG, "Retry attempt ${attempt + 1} after ${delayMs}ms")
            }
            try {
                val response = getResponse(urlString)
                if (response.httpCode in 200..299) {
                    return response
                }
                lastResponse = response
                if (response.httpCode >= 500 || response.httpCode == 429) {
                    continue
                }
                return response
            } catch (ex: Exception) {
                lastError = ex
                android.util.Log.w(TAG, "Request failed on attempt ${attempt + 1}: ${ex.message}")
            }
        }
        lastResponse?.let { return it }
        throw lastError ?: IllegalStateException("Request failed without response")
    }

    private fun buildUrl(limit: Int, ids: List<String>?): String {
        val baseUrl = settings.getApiBaseUrl().trimEnd('/')
        val base = StringBuilder("$baseUrl/assets")
        val params = mutableListOf<String>()
        if (!ids.isNullOrEmpty()) {
            val joined = ids.joinToString(",") { URLEncoder.encode(it, "UTF-8") }
            params.add("ids=$joined")
        } else {
            params.add("limit=$limit")
        }
        if (params.isNotEmpty()) {
            base.append("?").append(params.joinToString("&"))
        }
        return base.toString()
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
}
