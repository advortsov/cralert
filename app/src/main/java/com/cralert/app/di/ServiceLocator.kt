package com.cralert.app.di

import android.content.Context
import android.content.SharedPreferences
import com.cralert.app.data.AlertCheckEngine
import com.cralert.app.data.AlertRepository
import com.cralert.app.data.HistoryRepository
import com.cralert.app.data.MarketRepository
import com.cralert.app.data.local.DiagnosticsRepository
import com.cralert.app.data.local.IconRepository
import com.cralert.app.data.local.MarketCache
import com.cralert.app.data.local.SettingsRepository
import com.cralert.app.data.local.SharedPreferencesAlertRepository
import com.cralert.app.data.remote.CoinCapRestProvider
import com.cralert.app.data.remote.QuotesRestProvider
import com.cralert.app.worker.EmailNotifier

object ServiceLocator {
    lateinit var appContext: Context
        private set

    private val prefs: SharedPreferences by lazy {
        appContext.getSharedPreferences(SettingsRepository.PREFS_NAME, Context.MODE_PRIVATE)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(prefs)
    }

    val alertRepository: AlertRepository by lazy {
        SharedPreferencesAlertRepository(prefs)
    }

    val quotesRestProvider: QuotesRestProvider by lazy { CoinCapRestProvider(settingsRepository) }

    val marketRepository: MarketRepository by lazy {
        MarketRepository(quotesRestProvider, MarketCache(prefs), settingsRepository)
    }

    val iconRepository: IconRepository by lazy {
        IconRepository(appContext, prefs, settingsRepository)
    }

    val diagnosticsRepository: DiagnosticsRepository by lazy {
        DiagnosticsRepository(prefs)
    }

    val alertCheckEngine: AlertCheckEngine by lazy {
        AlertCheckEngine(alertRepository, emailNotifier)
    }

    val historyRepository: HistoryRepository by lazy {
        HistoryRepository(appContext, settingsRepository)
    }

    val emailNotifier: EmailNotifier by lazy {
        EmailNotifier(settingsRepository)
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }
}
