package com.cralert.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.cralert.app.BuildConfig

class SettingsRepository(private val prefs: SharedPreferences) {

    fun getApiBaseUrl(): String {
        return prefs.getString(KEY_API_BASE_URL, SettingsDefaults.API_BASE_URL)
            ?: SettingsDefaults.API_BASE_URL
    }

    fun setApiBaseUrl(value: String) {
        prefs.edit().putString(KEY_API_BASE_URL, value.trim()).apply()
    }

    fun getApiToken(): String {
        val stored = prefs.getString(KEY_API_TOKEN, "").orEmpty()
        return if (stored.isNotBlank()) stored else BuildConfig.COINCAP_TOKEN
    }

    fun setApiToken(value: String) {
        prefs.edit().putString(KEY_API_TOKEN, value.trim()).apply()
    }

    fun getIconBaseUrl(): String {
        return prefs.getString(KEY_ICON_BASE_URL, SettingsDefaults.ICON_BASE_URL)
            ?: SettingsDefaults.ICON_BASE_URL
    }

    fun setIconBaseUrl(value: String) {
        prefs.edit().putString(KEY_ICON_BASE_URL, value.trim()).apply()
    }

    fun getIconCacheTtlDays(): Long = prefs.getLong(KEY_ICON_CACHE_TTL_DAYS, SettingsDefaults.ICON_CACHE_TTL_DAYS)

    fun setIconCacheTtlDays(days: Long) {
        prefs.edit().putLong(KEY_ICON_CACHE_TTL_DAYS, days).apply()
    }

    fun getIconCacheTtlMs(): Long = getIconCacheTtlDays() * 24 * 60 * 60 * 1000L

    fun getCacheTtlHours(): Long = prefs.getLong(KEY_CACHE_TTL_HOURS, SettingsDefaults.CACHE_TTL_HOURS)

    fun setCacheTtlHours(hours: Long) {
        prefs.edit().putLong(KEY_CACHE_TTL_HOURS, hours).apply()
    }

    fun getCacheTtlMs(): Long = getCacheTtlHours() * 60 * 60 * 1000L

    fun getCheckIntervalMinutes(): Long = prefs.getLong(KEY_CHECK_INTERVAL_MIN, SettingsDefaults.CHECK_INTERVAL_MINUTES)

    fun setCheckIntervalMinutes(minutes: Long) {
        prefs.edit().putLong(KEY_CHECK_INTERVAL_MIN, minutes).apply()
    }

    fun getCheckIntervalMs(): Long = getCheckIntervalMinutes() * 60 * 1000L

    fun getAssetsLimit(): Int = prefs.getInt(KEY_ASSETS_LIMIT, SettingsDefaults.ASSETS_LIMIT)

    fun setAssetsLimit(limit: Int) {
        prefs.edit().putInt(KEY_ASSETS_LIMIT, limit).apply()
    }

    fun getConnectTimeoutMs(): Int = prefs.getInt(KEY_CONNECT_TIMEOUT_MS, SettingsDefaults.CONNECT_TIMEOUT_MS)

    fun setConnectTimeoutMs(value: Int) {
        prefs.edit().putInt(KEY_CONNECT_TIMEOUT_MS, value).apply()
    }

    fun getReadTimeoutMs(): Int = prefs.getInt(KEY_READ_TIMEOUT_MS, SettingsDefaults.READ_TIMEOUT_MS)

    fun setReadTimeoutMs(value: Int) {
        prefs.edit().putInt(KEY_READ_TIMEOUT_MS, value).apply()
    }

    fun isDarkModeEnabled(): Boolean = prefs.getBoolean(KEY_DARK_MODE, SettingsDefaults.DARK_MODE_ENABLED)

    fun setDarkModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun getRetryAttempts(): Int = prefs.getInt(KEY_RETRY_ATTEMPTS, SettingsDefaults.RETRY_ATTEMPTS)

    fun setRetryAttempts(value: Int) {
        prefs.edit().putInt(KEY_RETRY_ATTEMPTS, value).apply()
    }

    fun getRetryBaseDelayMs(): Long = prefs.getLong(KEY_RETRY_BASE_DELAY_MS, SettingsDefaults.RETRY_BASE_DELAY_MS)

    fun setRetryBaseDelayMs(value: Long) {
        prefs.edit().putLong(KEY_RETRY_BASE_DELAY_MS, value).apply()
    }

    fun getRetryMaxDelayMs(): Long = prefs.getLong(KEY_RETRY_MAX_DELAY_MS, SettingsDefaults.RETRY_MAX_DELAY_MS)

    fun setRetryMaxDelayMs(value: Long) {
        prefs.edit().putLong(KEY_RETRY_MAX_DELAY_MS, value).apply()
    }

    companion object {
        const val PREFS_NAME = "alerts_prefs"

        private const val KEY_API_BASE_URL = "settings_api_base_url"
        private const val KEY_API_TOKEN = "settings_api_token"
        private const val KEY_ICON_BASE_URL = "settings_icon_base_url"
        private const val KEY_ICON_CACHE_TTL_DAYS = "settings_icon_cache_ttl_days"
        private const val KEY_CACHE_TTL_HOURS = "settings_cache_ttl_hours"
        private const val KEY_CHECK_INTERVAL_MIN = "settings_check_interval_minutes"
        private const val KEY_ASSETS_LIMIT = "settings_assets_limit"
        private const val KEY_CONNECT_TIMEOUT_MS = "settings_connect_timeout_ms"
        private const val KEY_READ_TIMEOUT_MS = "settings_read_timeout_ms"
        private const val KEY_DARK_MODE = "settings_dark_mode"
        private const val KEY_RETRY_ATTEMPTS = "settings_retry_attempts"
        private const val KEY_RETRY_BASE_DELAY_MS = "settings_retry_base_delay_ms"
        private const val KEY_RETRY_MAX_DELAY_MS = "settings_retry_max_delay_ms"

        fun create(context: Context): SettingsRepository {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return SettingsRepository(prefs)
        }
    }
}
