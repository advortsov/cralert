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

class CoinCapApi(
    private val settings: SettingsRepository
) : MarketApi {

    companion object {
        private const val TAG = "CoinCapApi"
    }

    override suspend fun fetchAssets(limit: Int, ids: List<String>?): List<Asset> {
        return withContext(Dispatchers.IO) {
            val url = buildUrl(limit, ids)
            android.util.Log.d(TAG, "Request: $url")
            val json = getJson(url)
            android.util.Log.d(TAG, "Response size: ${json.length}")
            CoinCapParser.parseAssets(json)
        }
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

    private fun getJson(urlString: String): String {
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
            android.util.Log.d(TAG, "HTTP $code")
            val stream = if (code in 200..299) {
                BufferedInputStream(connection.inputStream)
            } else {
                BufferedInputStream(connection.errorStream)
            }
            val reader = BufferedReader(InputStreamReader(stream))
            val response = reader.readText()
            reader.close()
            response
        } finally {
            connection.disconnect()
        }
    }

}
