package com.cralert.app.data

interface AlertRepository {
    fun getAlerts(): List<Alert>
    fun saveAlert(alert: Alert)
    fun updateAlert(alert: Alert)
    fun deleteAlert(alertId: String)
    fun setAlertEnabled(alertId: String, enabled: Boolean)
}
