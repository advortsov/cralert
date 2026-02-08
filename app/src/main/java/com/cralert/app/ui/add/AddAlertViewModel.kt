package com.cralert.app.ui.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cralert.app.data.Alert
import com.cralert.app.data.AlertRepository
import com.cralert.app.data.Asset
import com.cralert.app.data.Condition
import com.cralert.app.data.MarketRepository
import com.cralert.app.data.local.SettingsRepository
import kotlinx.coroutines.launch
import java.util.UUID

class AddAlertViewModel(
    private val alertRepository: AlertRepository,
    private val marketRepository: MarketRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _assets = MutableLiveData<List<Asset>>()
    val assets: LiveData<List<Asset>> = _assets

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private val _selectedBaseAsset = MutableLiveData<Asset?>(null)
    val selectedBaseAsset: LiveData<Asset?> = _selectedBaseAsset

    private val _selectedQuoteAsset = MutableLiveData<Asset?>(null)
    val selectedQuoteAsset: LiveData<Asset?> = _selectedQuoteAsset

    fun loadAssets(forceRefresh: Boolean) {
        viewModelScope.launch {
            _loading.value = true
            val result = marketRepository.loadAssets(
                limit = settingsRepository.getAssetsLimit(),
                forceRefresh = forceRefresh
            )
            _assets.value = result.assets
            _loading.value = false
            _error.value = when {
                !result.success && !result.error.isNullOrBlank() -> buildErrorMessage(result.httpCode, result.error)
                else -> null
            }
        }
    }

    fun selectBaseAsset(asset: Asset) {
        _selectedBaseAsset.value = asset
    }

    fun selectQuoteAsset(asset: Asset) {
        _selectedQuoteAsset.value = asset
    }

    fun saveAlert(targetPrice: Double, condition: Condition) {
        val base = _selectedBaseAsset.value ?: return
        val quote = _selectedQuoteAsset.value ?: return
        val alert = Alert(
            id = UUID.randomUUID().toString(),
            assetId = base.id,
            symbol = base.symbol,
            name = base.name,
            quoteAssetId = quote.id,
            quoteSymbol = quote.symbol,
            quoteName = quote.name,
            targetPrice = targetPrice,
            condition = condition,
            enabled = true,
            lastTriggeredAt = null,
            lastPrice = null
        )
        alertRepository.saveAlert(alert)
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
