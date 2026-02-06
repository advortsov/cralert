package com.cralert.app.data.local

object SettingsDefaults {
    const val API_BASE_URL = "https://rest.coincap.io/v3"
    const val ICON_BASE_URL = "https://assets.coincap.io/assets/icons"

    const val CACHE_TTL_HOURS = 24L
    const val CHECK_INTERVAL_MINUTES = 15L
    const val MIN_TRIGGER_INTERVAL_MINUTES = 15L
    const val ASSETS_LIMIT = 200

    const val CONNECT_TIMEOUT_MS = 5000
    const val READ_TIMEOUT_MS = 8000

    const val DARK_MODE_ENABLED = false
}
