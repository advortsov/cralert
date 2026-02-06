package com.cralert.app.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cralert.app.data.AlertRepository
import com.cralert.app.data.MarketRepository
import com.cralert.app.data.local.SettingsRepository

class AddAlertViewModelFactory(
    private val alertRepository: AlertRepository,
    private val marketRepository: MarketRepository,
    private val settingsRepository: SettingsRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddAlertViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddAlertViewModel(alertRepository, marketRepository, settingsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
