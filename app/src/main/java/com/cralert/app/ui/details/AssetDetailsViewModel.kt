package com.cralert.app.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cralert.app.data.HistoricalPoint
import com.cralert.app.data.HistoryRepository
import com.cralert.app.data.PriceCalculator
import com.cralert.app.data.Alert
import com.cralert.app.data.MarketRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset

class AssetDetailsViewModel(
    private val historyRepository: HistoryRepository,
    private val marketRepository: MarketRepository
) : ViewModel() {

    private val _state = MutableLiveData(AssetDetailsState())
    val state: LiveData<AssetDetailsState> = _state

    private var baseId: String = ""
    private var baseSymbol: String = ""
    private var baseName: String = ""
    private var quoteId: String = ""
    private var quoteSymbol: String = ""
    private var quoteName: String = ""

    fun init(baseId: String, baseSymbol: String, baseName: String, quoteId: String, quoteSymbol: String, quoteName: String) {
        this.baseId = baseId
        this.baseSymbol = baseSymbol
        this.baseName = baseName
        this.quoteId = quoteId
        this.quoteSymbol = quoteSymbol
        this.quoteName = quoteName
        loadHistory()
    }

    fun retry() {
        loadHistory(forceRefresh = true)
    }

    private fun loadHistory(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val current = _state.value ?: AssetDetailsState()
            _state.value = current.copy(loading = true, error = null)

            val end = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            val start = LocalDate.now(ZoneOffset.UTC).minusYears(5).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            val result = historyRepository.loadPairHistory(
                baseId = baseId,
                baseSymbol = baseSymbol,
                quoteId = quoteId,
                quoteSymbol = quoteSymbol,
                interval = "d1",
                startMs = start,
                endMs = end,
                forceRefresh = forceRefresh
            )

            val points = result.series?.points ?: emptyList()
            val min = points.minByOrNull { it.price }?.price
            val max = points.maxByOrNull { it.price }?.price
            val change = if (points.size >= 2) {
                val first = points.first().price
                val last = points.last().price
                if (first != 0.0) ((last - first) / first) * 100.0 else null
            } else {
                null
            }

            val currentPrice = PriceCalculator.currentPrice(
                Alert(
                    id = "",
                    assetId = baseId,
                    symbol = baseSymbol,
                    name = baseName,
                    quoteAssetId = quoteId,
                    quoteSymbol = quoteSymbol,
                    quoteName = quoteName,
                    targetPrice = 0.0,
                    condition = com.cralert.app.data.Condition.ABOVE,
                    enabled = true,
                    lastTriggeredAt = null,
                    lastPrice = null
                ),
                marketRepository.getCachedPrices()
            )

            _state.value = AssetDetailsState(
                baseId = baseId,
                baseSymbol = baseSymbol,
                baseName = baseName,
                quoteId = quoteId,
                quoteSymbol = quoteSymbol,
                quoteName = quoteName,
                loading = false,
                points = points,
                fromCache = result.fromCache,
                error = result.error,
                hasError = !result.success,
                min = min,
                max = max,
                changePercent = change,
                updatedAt = result.series?.fetchedAtMs ?: 0L,
                currentPrice = currentPrice
            )
        }
    }
}

data class AssetDetailsState(
    val baseId: String = "",
    val baseSymbol: String = "",
    val baseName: String = "",
    val quoteId: String = "",
    val quoteSymbol: String = "",
    val quoteName: String = "",
    val loading: Boolean = false,
    val points: List<HistoricalPoint> = emptyList(),
    val fromCache: Boolean = false,
    val error: String? = null,
    val hasError: Boolean = false,
    val min: Double? = null,
    val max: Double? = null,
    val changePercent: Double? = null,
    val updatedAt: Long = 0L,
    val currentPrice: Double? = null
)
