package com.cralert.app.ui.main

import com.cralert.app.data.Alert

data class AlertUiModel(
    val alert: Alert,
    val currentPrice: Double?
)
