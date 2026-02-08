package com.cralert.app.data.remote

import com.cralert.app.data.Asset

interface QuotesRestProvider {
    suspend fun fetchAssets(limit: Int, ids: List<String>? = null): ApiResult<List<Asset>>
}
