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
    const val HISTORY_CACHE_TTL_HOURS = 24L
    const val CRYPTOCOMPARE_BASE_URL = "https://min-api.cryptocompare.com/data/v2"
    const val CRYPTOCOMPARE_API_KEY = ""
    const val RESEND_BASE_URL = "https://api.resend.com"
    const val RESEND_API_KEY = ""
    const val EMAIL_ENABLED = false
    const val EMAIL_FROM = ""
    const val EMAIL_TO = ""
}
