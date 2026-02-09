package com.cralert.app.data

import android.content.Context
import com.cralert.app.data.local.HistoryCache
import com.cralert.app.data.local.SettingsRepository
import com.cralert.app.data.remote.CoinCapHistoryProvider
import com.cralert.app.data.remote.CryptoCompareHistoryProvider
import com.cralert.app.data.remote.HistoryProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HistoryRepository(
    context: Context,
    settings: SettingsRepository
) {
    data class HistoryLoadResult(
        val series: HistorySeries?,
        val fromCache: Boolean,
        val error: String? = null,
        val httpCode: Int? = null,
        val success: Boolean = true
    )

    private val cache = HistoryCache(context)
    private val settingsRepository = settings
    private val coinCapProvider: HistoryProvider = CoinCapHistoryProvider(settingsRepository)
    private val cryptoCompareProvider: HistoryProvider = CryptoCompareHistoryProvider(settingsRepository)

    suspend fun loadPairHistory(
        baseId: String,
        baseSymbol: String,
        quoteId: String,
        quoteSymbol: String,
        interval: String,
        startMs: Long,
        endMs: Long,
        forceRefresh: Boolean = false
    ): HistoryLoadResult {
        return withContext(Dispatchers.IO) {
            if (baseId == quoteId) {
                val single = loadAssetHistory(baseId, baseSymbol, interval, startMs, endMs, forceRefresh)
                return@withContext if (single.series != null) {
                    val ones = single.series.points.map { HistoricalPoint(it.timeMs, 1.0) }
                    HistoryLoadResult(
                        series = HistorySeries(
                            assetId = "$baseId/$quoteId",
                            interval = interval,
                            startMs = startMs,
                            endMs = endMs,
                            points = ones,
                            source = single.series.source,
                            fetchedAtMs = single.series.fetchedAtMs
                        ),
                        fromCache = single.fromCache,
                        error = single.error,
                        httpCode = single.httpCode,
                        success = single.success
                    )
                } else {
                    HistoryLoadResult(null, single.fromCache, single.error, single.httpCode, false)
                }
            }

            val baseResult = loadAssetHistory(baseId, baseSymbol, interval, startMs, endMs, forceRefresh)
            val quoteResult = loadAssetHistory(quoteId, quoteSymbol, interval, startMs, endMs, forceRefresh)
            val baseSeries = baseResult.series
            val quoteSeries = quoteResult.series
            if (baseSeries == null || quoteSeries == null) {
                val error = baseResult.error ?: quoteResult.error
                return@withContext HistoryLoadResult(null, baseResult.fromCache && quoteResult.fromCache, error, null, false)
            }
            val combined = HistorySeriesCalculator.combineToPair(
                base = baseSeries,
                quote = quoteSeries,
                pairId = "$baseId/$quoteId",
                interval = interval,
                startMs = startMs,
                endMs = endMs
            )
            val fromCache = baseResult.fromCache && quoteResult.fromCache
            val success = baseResult.success && quoteResult.success
            val error = baseResult.error ?: quoteResult.error
            HistoryLoadResult(combined, fromCache, error, null, success)
        }
    }

    private suspend fun loadAssetHistory(
        assetId: String,
        assetSymbol: String,
        interval: String,
        startMs: Long,
        endMs: Long,
        forceRefresh: Boolean
    ): HistoryLoadResult {
        val cached = cache.read(assetId, interval)
        val cacheFresh = cached?.let { isCacheFresh(it.fetchedAtMs) } ?: false
        if (!forceRefresh && cacheFresh) {
            val filtered = cached.copy(
                points = cached.points.filter { it.timeMs in startMs..endMs }
            )
            return HistoryLoadResult(filtered, true, null, null, true)
        }
        val coinCapResult = coinCapProvider.fetchHistory(assetId, assetSymbol, interval, startMs, endMs)
        if (coinCapResult.success && coinCapResult.data.points.isNotEmpty()) {
            cache.write(coinCapResult.data)
            return HistoryLoadResult(coinCapResult.data, false, null, coinCapResult.httpCode, true)
        }
        val fallbackNeeded = coinCapResult.httpCode == 429 || !coinCapResult.success
        if (fallbackNeeded) {
            val cryptoResult = cryptoCompareProvider.fetchHistory(assetId, assetSymbol, interval, startMs, endMs)
            if (cryptoResult.success && cryptoResult.data.points.isNotEmpty()) {
                cache.write(cryptoResult.data)
                return HistoryLoadResult(cryptoResult.data, false, null, cryptoResult.httpCode, true)
            }
        }
        if (cached != null) {
            return HistoryLoadResult(cached, true, coinCapResult.error, coinCapResult.httpCode, false)
        }
        return HistoryLoadResult(null, false, coinCapResult.error, coinCapResult.httpCode, false)
    }

    private fun isCacheFresh(fetchedAt: Long): Boolean {
        if (fetchedAt == 0L) return false
        val age = System.currentTimeMillis() - fetchedAt
        return age < settingsRepository.getHistoryCacheTtlMs()
    }
}
