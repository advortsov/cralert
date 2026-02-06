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
            connectTimeout = 5000
            readTimeout = 8000
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
        val map = getIndex().apply { put(assetId, file.name) }
        val json = JSONObject()
        map.forEach { (key, value) -> json.put(key, value) }
        prefs.edit().putString(KEY_INDEX, json.toString()).apply()
    }

    private fun getCachedFile(assetId: String): File? {
        val index = getIndex()
        val name = index[assetId] ?: return null
        return File(context.cacheDir, name)
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

    private companion object {
        private const val KEY_INDEX = "icons_cache_index"
    }
}
