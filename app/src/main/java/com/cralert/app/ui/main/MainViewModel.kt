package com.cralert.app.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cralert.app.data.Alert
import com.cralert.app.data.AlertRepository
import com.cralert.app.data.MarketRepository
import com.cralert.app.data.PriceCalculator
import kotlinx.coroutines.launch

class MainViewModel(
    private val alertRepository: AlertRepository,
    private val marketRepository: MarketRepository
) : ViewModel() {

    private val _alerts = MutableLiveData<List<AlertUiModel>>()
    val alerts: LiveData<List<AlertUiModel>> = _alerts

    private val _cached = MutableLiveData(false)
    val cached: LiveData<Boolean> = _cached

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _lastUpdateAt = MutableLiveData(0L)
    val lastUpdateAt: LiveData<Long> = _lastUpdateAt

    fun loadAlerts() {
        val alerts = alertRepository.getAlerts()
        val prices = marketRepository.getCachedPrices()
        _alerts.value = alerts.map { alert ->
            AlertUiModel(alert, PriceCalculator.currentPrice(alert, prices))
        }
        _cached.value = !marketRepository.isPriceCacheFresh()
        _lastUpdateAt.value = marketRepository.getPricesTimestamp()
    }

    fun refreshPrices() {
        viewModelScope.launch {
            _loading.value = true
            val alerts = alertRepository.getAlerts()
            val ids = alerts.flatMap { listOf(it.assetId, it.quoteAssetId) }.distinct()
            val fetchResult = if (ids.isNotEmpty()) {
                marketRepository.fetchAssetsByIds(ids)
            } else {
                val limit = com.cralert.app.di.ServiceLocator.settingsRepository.getAssetsLimit()
                val loadResult = marketRepository.loadAssets(limit = limit, forceRefresh = true)
                com.cralert.app.data.remote.ApiResult(
                    data = loadResult.assets,
                    httpCode = loadResult.httpCode,
                    error = loadResult.error,
                    success = loadResult.success
                )
            }
            val assets = fetchResult.data
            val prices = marketRepository.getCachedPrices()
            if (fetchResult.success) {
                com.cralert.app.di.ServiceLocator.alertCheckEngine.evaluate(
                    com.cralert.app.di.ServiceLocator.appContext,
                    alerts,
                    prices,
                    marketRepository.getPricesTimestamp()
                )
            }
            _alerts.value = alerts.map { alert ->
                AlertUiModel(alert, PriceCalculator.currentPrice(alert, prices))
            }
            _cached.value = !marketRepository.isPriceCacheFresh() || assets.isEmpty()
            _lastUpdateAt.value = marketRepository.getPricesTimestamp()
            _error.value = if (!fetchResult.success && !fetchResult.error.isNullOrBlank()) {
                buildErrorMessage(fetchResult.httpCode, fetchResult.error)
            } else {
                null
            }
            _loading.value = false
        }
    }

    fun deleteAlert(alert: Alert) {
        alertRepository.deleteAlert(alert.id)
        loadAlerts()
    }

    fun toggleAlert(alert: Alert, enabled: Boolean) {
        alertRepository.setAlertEnabled(alert.id, enabled)
        loadAlerts()
    }

    private fun buildErrorMessage(httpCode: Int?, error: String?): String {
        val codePart = httpCode?.let { "HTTP $it" } ?: "HTTP error"
        val messagePart = error?.take(120).orEmpty()
        return if (messagePart.isNotBlank()) {
            "$codePart: $messagePart"
        } else {
            codePart
        }
    }
}
