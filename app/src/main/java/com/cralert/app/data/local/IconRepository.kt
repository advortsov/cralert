package com.cralert.app.data.local

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class IconRepository(
    private val context: Context,
    private val prefs: SharedPreferences,
    private val settings: SettingsRepository
) {

    suspend fun loadIcon(assetId: String, symbol: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            val file = getCachedFile(assetId)
            if (file != null && file.exists()) {
                return@withContext BitmapFactory.decodeFile(file.absolutePath)
            }

            val downloaded = downloadIcon(assetId, symbol)
            if (downloaded != null) {
                saveToCache(assetId, downloaded)
                return@withContext BitmapFactory.decodeFile(downloaded.absolutePath)
            }

            null
        }
    }

    private fun downloadIcon(assetId: String, symbol: String): File? {
        val baseUrl = settings.getIconBaseUrl().trimEnd('/')
        val url = URL("$baseUrl/${symbol.lowercase()}@2x.png")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = settings.getConnectTimeoutMs()
            readTimeout = settings.getReadTimeoutMs()
        }

        return try {
            val file = File(context.cacheDir, "icon_${assetId}.png")
            connection.inputStream.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (ex: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun saveToCache(assetId: String, file: File) {
        val index = getIndex().apply { put(assetId, file.name) }
        val timestamps = getTimestampIndex().apply { put(assetId, System.currentTimeMillis()) }
        persistIndex(index, timestamps)
    }

    private fun getCachedFile(assetId: String): File? {
        val index = getIndex()
        val timestamps = getTimestampIndex()
        val name = index[assetId] ?: return null
        val file = File(context.cacheDir, name)
        val timestamp = timestamps[assetId] ?: 0L
        if (!file.exists() || isExpired(timestamp)) {
            if (file.exists()) {
                file.delete()
            }
            index.remove(assetId)
            timestamps.remove(assetId)
            persistIndex(index, timestamps)
            return null
        }
        return file
    }

    private fun getIndex(): MutableMap<String, String> {
        val raw = prefs.getString(KEY_INDEX, null) ?: return mutableMapOf()
        val json = JSONObject(raw)
        val map = mutableMapOf<String, String>()
        json.keys().forEach { key ->
            map[key] = json.getString(key)
        }
        return map
    }

    private fun getTimestampIndex(): MutableMap<String, Long> {
        val raw = prefs.getString(KEY_INDEX_TS, null) ?: return mutableMapOf()
        val json = JSONObject(raw)
        val map = mutableMapOf<String, Long>()
        json.keys().forEach { key ->
            map[key] = json.optLong(key, 0L)
        }
        return map
    }

    private fun persistIndex(index: Map<String, String>, timestamps: Map<String, Long>) {
        val indexJson = JSONObject()
        index.forEach { (key, value) -> indexJson.put(key, value) }
        val tsJson = JSONObject()
        timestamps.forEach { (key, value) -> tsJson.put(key, value) }
        prefs.edit()
            .putString(KEY_INDEX, indexJson.toString())
            .putString(KEY_INDEX_TS, tsJson.toString())
            .apply()
    }

    private fun isExpired(timestamp: Long): Boolean {
        val ttlMs = settings.getIconCacheTtlMs()
        if (ttlMs <= 0L) return true
        if (timestamp <= 0L) return true
        return System.currentTimeMillis() - timestamp > ttlMs
    }

    private companion object {
        private const val KEY_INDEX = "icons_cache_index"
        private const val KEY_INDEX_TS = "icons_cache_timestamp"
    }
}
