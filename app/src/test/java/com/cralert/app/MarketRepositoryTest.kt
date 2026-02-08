package com.cralert.app

import android.content.SharedPreferences
import com.cralert.app.data.Asset
import com.cralert.app.data.MarketRepository
import com.cralert.app.data.local.MarketCache
import com.cralert.app.data.local.SettingsRepository
import com.cralert.app.data.remote.ApiResult
import com.cralert.app.data.remote.QuotesRestProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketRepositoryTest {

    @Test
    fun fetchAssetsByIdsUpdatesCache() = runBlocking {
        val prefs = FakeSharedPreferences()
        val settings = SettingsRepository(prefs)
        val cache = MarketCache(prefs)
        val api = FakeMarketApi()
        val repository = MarketRepository(api, cache, settings)

        val result = repository.fetchAssetsByIds(listOf("bitcoin"))

        assertTrue("Expected success result", result.success)
        val prices = repository.getCachedPrices()
        assertEquals(1, prices.size)
        val price = prices["bitcoin"] ?: 0.0
        assertEquals(123.0, price, 0.0001)
    }

    private class FakeMarketApi : QuotesRestProvider {
        override suspend fun fetchAssets(limit: Int, ids: List<String>?): ApiResult<List<Asset>> {
            val assets = listOf(
                Asset(
                    id = "bitcoin",
                    symbol = "BTC",
                    name = "Bitcoin",
                    priceUsd = 123.0
                )
            )
            return ApiResult.success(assets, 200)
        }
    }


    private class FakeSharedPreferences : SharedPreferences {
        private val data = mutableMapOf<String, Any?>()

        override fun getAll(): MutableMap<String, *> = data.toMutableMap()

        override fun getString(key: String, defValue: String?): String? {
            return data[key] as? String ?: defValue
        }

        override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
            @Suppress("UNCHECKED_CAST")
            return data[key] as? MutableSet<String> ?: defValues
        }

        override fun getInt(key: String, defValue: Int): Int {
            return data[key] as? Int ?: defValue
        }

        override fun getLong(key: String, defValue: Long): Long {
            return data[key] as? Long ?: defValue
        }

        override fun getFloat(key: String, defValue: Float): Float {
            return data[key] as? Float ?: defValue
        }

        override fun getBoolean(key: String, defValue: Boolean): Boolean {
            return data[key] as? Boolean ?: defValue
        }

        override fun contains(key: String): Boolean = data.containsKey(key)

        override fun edit(): SharedPreferences.Editor = Editor()

        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
            // no-op
        }

        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
            // no-op
        }

        private inner class Editor : SharedPreferences.Editor {
            private val pending = mutableMapOf<String, Any?>()
            private var clear = false

            override fun putString(key: String, value: String?): SharedPreferences.Editor = apply {
                pending[key] = value
            }

            override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor = apply {
                pending[key] = values
            }

            override fun putInt(key: String, value: Int): SharedPreferences.Editor = apply {
                pending[key] = value
            }

            override fun putLong(key: String, value: Long): SharedPreferences.Editor = apply {
                pending[key] = value
            }

            override fun putFloat(key: String, value: Float): SharedPreferences.Editor = apply {
                pending[key] = value
            }

            override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = apply {
                pending[key] = value
            }

            override fun remove(key: String): SharedPreferences.Editor = apply {
                pending[key] = null
            }

            override fun clear(): SharedPreferences.Editor = apply {
                clear = true
            }

            override fun commit(): Boolean {
                applyChanges()
                return true
            }

            override fun apply() {
                applyChanges()
            }

            private fun applyChanges() {
                if (clear) {
                    data.clear()
                }
                pending.forEach { (key, value) ->
                    if (value == null) {
                        data.remove(key)
                    } else {
                        data[key] = value
                    }
                }
                pending.clear()
            }
        }
    }
}
