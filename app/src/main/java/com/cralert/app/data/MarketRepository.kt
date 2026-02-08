package com.cralert.app.data

import com.cralert.app.data.local.MarketCache
import com.cralert.app.data.local.SettingsRepository
import com.cralert.app.data.remote.ApiResult
import com.cralert.app.data.remote.QuotesRestProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MarketRepository(
    private val api: QuotesRestProvider,
    private val cache: MarketCache,
    private val settings: SettingsRepository
) {
    data class AssetResult(
        val assets: List<Asset>,
        val fromCache: Boolean,
        val error: String? = null,
        val httpCode: Int? = null,
        val success: Boolean = true
    )

    suspend fun loadAssets(limit: Int, forceRefresh: Boolean = false): AssetResult {
        return withContext(Dispatchers.IO) {
            val cacheFresh = isCacheFresh(cache.getAssetsTimestamp())
            if (!forceRefresh && cacheFresh) {
                val cached = cache.getCachedAssets()
                return@withContext AssetResult(cached, true)
            }
            try {
                val result = api.fetchAssets(limit)
                if (result.success && result.data.isNotEmpty()) {
                    cache.putAssets(result.data)
                    cache.putPrices(result.data.associate { it.id to it.priceUsd })
                    AssetResult(result.data, false, null, result.httpCode, true)
                } else {
                    val cached = cache.getCachedAssets()
                    AssetResult(
                        assets = cached,
                        fromCache = true,
                        error = result.error,
                        httpCode = result.httpCode,
                        success = false
                    )
                }
            } catch (ex: Exception) {
                android.util.Log.e("MarketRepository", "Failed to load assets: ${ex.message}", ex)
                val cached = cache.getCachedAssets()
                AssetResult(
                    assets = cached,
                    fromCache = true,
                    error = ex.message,
                    httpCode = null,
                    success = false
                )
            }
        }
    }

    suspend fun fetchAssetsByIds(ids: List<String>): ApiResult<List<Asset>> {
        return withContext(Dispatchers.IO) {
            if (ids.isEmpty()) {
                return@withContext ApiResult.failure(emptyList(), null, "No ids")
            }
            try {
                val result = api.fetchAssets(limit = ids.size, ids = ids)
                if (result.success && result.data.isNotEmpty()) {
                    cache.putPrices(result.data.associate { it.id to it.priceUsd })
                    return@withContext result
                }
                val cached = cache.getCachedAssets().filter { ids.contains(it.id) }
                if (cached.isNotEmpty()) {
                    cache.putPrices(cached.associate { it.id to it.priceUsd })
                }
                ApiResult.failure(cached, result.httpCode, result.error)
            } catch (ex: Exception) {
                android.util.Log.e("MarketRepository", "Failed to load assets by ids: ${ex.message}", ex)
                val cached = cache.getCachedAssets()
                ApiResult.failure(cached.filter { ids.contains(it.id) }, null, ex.message)
            }
        }
    }

    fun getCachedPrices(): Map<String, Double> = cache.getCachedPrices()

    fun getPricesTimestamp(): Long = cache.getPricesTimestamp()

    fun isPriceCacheFresh(): Boolean = isCacheFresh(cache.getPricesTimestamp())

    private fun isCacheFresh(timestamp: Long): Boolean {
        if (timestamp == 0L) return false
        val age = System.currentTimeMillis() - timestamp
        return age < settings.getCacheTtlMs()
    }
}
