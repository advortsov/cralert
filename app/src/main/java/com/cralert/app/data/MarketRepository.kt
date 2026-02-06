package com.cralert.app.data

import com.cralert.app.data.local.MarketCache
import com.cralert.app.data.local.SettingsRepository
import com.cralert.app.data.remote.MarketApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MarketRepository(
    private val api: MarketApi,
    private val cache: MarketCache,
    private val settings: SettingsRepository
) {
    data class AssetResult(
        val assets: List<Asset>,
        val fromCache: Boolean
    )

    suspend fun loadAssets(limit: Int, forceRefresh: Boolean = false): AssetResult {
        return withContext(Dispatchers.IO) {
            val cacheFresh = isCacheFresh(cache.getAssetsTimestamp())
            if (!forceRefresh && cacheFresh) {
                val cached = cache.getCachedAssets()
                return@withContext AssetResult(cached, true)
            }
            try {
                val assets = api.fetchAssets(limit)
                cache.putAssets(assets)
                cache.putPrices(assets.associate { it.id to it.priceUsd })
                AssetResult(assets, false)
            } catch (ex: Exception) {
                android.util.Log.e("MarketRepository", "Failed to load assets: ${ex.message}", ex)
                val cached = cache.getCachedAssets()
                AssetResult(cached, true)
            }
        }
    }

    suspend fun fetchAssetsByIds(ids: List<String>): List<Asset> {
        return withContext(Dispatchers.IO) {
            if (ids.isEmpty()) return@withContext emptyList()
            try {
                val assets = api.fetchAssets(limit = ids.size, ids = ids)
                if (assets.isNotEmpty()) {
                    cache.putPrices(assets.associate { it.id to it.priceUsd })
                }
                assets
            } catch (ex: Exception) {
                android.util.Log.e("MarketRepository", "Failed to load assets by ids: ${ex.message}", ex)
                val cached = cache.getCachedAssets()
                cached.filter { ids.contains(it.id) }
            }
        }
    }

    fun getCachedPrices(): Map<String, Double> = cache.getCachedPrices()

    fun isPriceCacheFresh(): Boolean = isCacheFresh(cache.getPricesTimestamp())

    private fun isCacheFresh(timestamp: Long): Boolean {
        if (timestamp == 0L) return false
        val age = System.currentTimeMillis() - timestamp
        return age < settings.getCacheTtlMs()
    }
}
