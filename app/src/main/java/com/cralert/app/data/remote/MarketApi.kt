package com.cralert.app.data.remote

import com.cralert.app.data.Asset

interface MarketApi {
    suspend fun fetchAssets(limit: Int, ids: List<String>? = null): List<Asset>
}
