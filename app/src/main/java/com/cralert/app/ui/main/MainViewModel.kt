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

    fun loadAlerts() {
        val alerts = alertRepository.getAlerts()
        val prices = marketRepository.getCachedPrices()
        _alerts.value = alerts.map { alert ->
            AlertUiModel(alert, PriceCalculator.currentPrice(alert, prices))
        }
        _cached.value = !marketRepository.isPriceCacheFresh()
    }

    fun refreshPrices() {
        viewModelScope.launch {
            _loading.value = true
            val alerts = alertRepository.getAlerts()
            val ids = alerts.flatMap { listOf(it.assetId, it.quoteAssetId) }.distinct()
            val assets = marketRepository.fetchAssetsByIds(ids)
            val prices = marketRepository.getCachedPrices()
            _alerts.value = alerts.map { alert ->
                AlertUiModel(alert, PriceCalculator.currentPrice(alert, prices))
            }
            _cached.value = !marketRepository.isPriceCacheFresh() || assets.isEmpty()
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
}
