package com.cralert.app.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cralert.app.data.AlertRepository
import com.cralert.app.data.MarketRepository

class MainViewModelFactory(
    private val alertRepository: AlertRepository,
    private val marketRepository: MarketRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(alertRepository, marketRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel")
    }
}
