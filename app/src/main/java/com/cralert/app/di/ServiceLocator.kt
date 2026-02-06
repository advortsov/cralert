package com.cralert.app.di

import android.content.Context
import android.content.SharedPreferences
import com.cralert.app.data.AlertRepository
import com.cralert.app.data.MarketRepository
import com.cralert.app.data.local.IconRepository
import com.cralert.app.data.local.MarketCache
import com.cralert.app.data.local.SettingsRepository
import com.cralert.app.data.local.SharedPreferencesAlertRepository
import com.cralert.app.data.remote.CoinCapApi
import com.cralert.app.data.remote.MarketApi

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

    val marketApi: MarketApi by lazy { CoinCapApi(settingsRepository) }

    val marketRepository: MarketRepository by lazy {
        MarketRepository(marketApi, MarketCache(prefs), settingsRepository)
    }

    val iconRepository: IconRepository by lazy {
        IconRepository(appContext, prefs, settingsRepository)
    }

    fun init(context: Context) {
        appContext = context.applicationContext
    }
}
