package com.cralert.app.data.local

import android.content.SharedPreferences
import com.cralert.app.data.Alert
import com.cralert.app.data.AlertRepository
import org.json.JSONArray

class SharedPreferencesAlertRepository(
    private val prefs: SharedPreferences
) : AlertRepository {

    override fun getAlerts(): List<Alert> {
        val raw = prefs.getString(KEY_ALERTS, null) ?: return emptyList()
        val json = JSONArray(raw)
        val alerts = mutableListOf<Alert>()
        for (i in 0 until json.length()) {
            alerts.add(AlertJsonAdapter.fromJson(json.getJSONObject(i)))
        }
        return alerts
    }

    override fun saveAlert(alert: Alert) {
        val alerts = getAlerts().toMutableList()
        alerts.add(alert)
        persistAlerts(alerts)
    }

    override fun updateAlert(alert: Alert) {
        val alerts = getAlerts().toMutableList()
        val index = alerts.indexOfFirst { it.id == alert.id }
        if (index >= 0) {
            alerts[index] = alert
            persistAlerts(alerts)
        }
    }

    override fun deleteAlert(alertId: String) {
        val alerts = getAlerts().filterNot { it.id == alertId }
        persistAlerts(alerts)
    }

    override fun setAlertEnabled(alertId: String, enabled: Boolean) {
        val alerts = getAlerts().toMutableList()
        val index = alerts.indexOfFirst { it.id == alertId }
        if (index >= 0) {
            val current = alerts[index]
            alerts[index] = current.copy(enabled = enabled)
            persistAlerts(alerts)
        }
    }

    private fun persistAlerts(alerts: List<Alert>) {
        val json = JSONArray()
        alerts.forEach { json.put(AlertJsonAdapter.toJson(it)) }
        prefs.edit().putString(KEY_ALERTS, json.toString()).apply()
    }

    private companion object {
        private const val KEY_ALERTS = "alerts_json"
    }
}
