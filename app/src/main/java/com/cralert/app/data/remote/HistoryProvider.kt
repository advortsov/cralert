package com.cralert.app.data.remote

import com.cralert.app.data.HistorySeries

interface HistoryProvider {
    suspend fun fetchHistory(
        assetId: String,
        assetSymbol: String,
        interval: String,
        startMs: Long,
        endMs: Long
    ): ApiResult<HistorySeries>
}
