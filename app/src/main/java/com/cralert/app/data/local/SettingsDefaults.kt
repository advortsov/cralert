package com.cralert.app.data.local

object SettingsDefaults {
    const val API_BASE_URL = "https://rest.coincap.io/v3"
    const val ICON_BASE_URL = "https://assets.coincap.io/assets/icons"

    const val CACHE_TTL_HOURS = 24L
    const val ICON_CACHE_TTL_DAYS = 7L
    const val CHECK_INTERVAL_MINUTES = 15L
    const val ASSETS_LIMIT = 200

    const val CONNECT_TIMEOUT_MS = 8000
    const val READ_TIMEOUT_MS = 15000

    const val DARK_MODE_ENABLED = false
    const val RETRY_ATTEMPTS = 3
    const val RETRY_BASE_DELAY_MS = 500L
    const val RETRY_MAX_DELAY_MS = 1500L
}
