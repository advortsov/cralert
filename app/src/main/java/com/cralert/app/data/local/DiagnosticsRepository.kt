package com.cralert.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.cralert.app.data.DiagnosticsState
import org.json.JSONObject

class DiagnosticsRepository(private val prefs: SharedPreferences) {

    fun getState(): DiagnosticsState {
        val raw = prefs.getString(KEY_STATE, null) ?: return DiagnosticsState()
        val json = JSONObject(raw)
        return DiagnosticsState(
            lastAlarmScheduledAt = json.optLong(KEY_LAST_ALARM_SCHEDULED, 0L),
            nextAlarmAt = json.optLong(KEY_NEXT_ALARM_AT, 0L),
            lastReceiverFiredAt = json.optLong(KEY_LAST_RECEIVER_FIRED, 0L),
            lastExactAlarmAllowed = if (json.has(KEY_LAST_EXACT_ALLOWED)) json.getBoolean(KEY_LAST_EXACT_ALLOWED) else null,
            lastJobScheduledAt = json.optLong(KEY_LAST_JOB_SCHEDULED_AT, 0L),
            lastJobScheduleResult = if (json.has(KEY_LAST_JOB_RESULT)) json.getInt(KEY_LAST_JOB_RESULT) else null,
            lastJobScheduleError = if (json.has(KEY_LAST_JOB_ERROR)) json.getString(KEY_LAST_JOB_ERROR) else null,
            lastServiceStartedAt = json.optLong(KEY_LAST_SERVICE_STARTED, 0L),
            lastServiceFinishedAt = json.optLong(KEY_LAST_SERVICE_FINISHED, 0L),
            lastFetchStartedAt = json.optLong(KEY_LAST_FETCH_STARTED, 0L),
            lastFetchSuccess = if (json.has(KEY_LAST_FETCH_SUCCESS)) json.getBoolean(KEY_LAST_FETCH_SUCCESS) else null,
            lastFetchHttpCode = if (json.has(KEY_LAST_FETCH_HTTP)) json.getInt(KEY_LAST_FETCH_HTTP) else null,
            lastFetchError = if (json.has(KEY_LAST_FETCH_ERROR)) json.getString(KEY_LAST_FETCH_ERROR) else null,
            lastAssetsCount = json.optInt(KEY_LAST_ASSETS_COUNT, 0),
            lastPricesCount = json.optInt(KEY_LAST_PRICES_COUNT, 0),
            lastTriggeredAlerts = json.optInt(KEY_LAST_TRIGGERED, 0),
            lastSkippedNoPrice = json.optInt(KEY_LAST_SKIPPED_NO_PRICE, 0),
            lastSkippedDisabled = json.optInt(KEY_LAST_SKIPPED_DISABLED, 0),
            lastNotificationSentAt = json.optLong(KEY_LAST_NOTIFICATION_SENT, 0L)
        )
    }

    fun setState(state: DiagnosticsState) {
        val json = JSONObject()
        json.put(KEY_LAST_ALARM_SCHEDULED, state.lastAlarmScheduledAt)
        json.put(KEY_NEXT_ALARM_AT, state.nextAlarmAt)
        json.put(KEY_LAST_RECEIVER_FIRED, state.lastReceiverFiredAt)
        putNullableBoolean(json, KEY_LAST_EXACT_ALLOWED, state.lastExactAlarmAllowed)
        json.put(KEY_LAST_JOB_SCHEDULED_AT, state.lastJobScheduledAt)
        putNullableInt(json, KEY_LAST_JOB_RESULT, state.lastJobScheduleResult)
        putNullableString(json, KEY_LAST_JOB_ERROR, state.lastJobScheduleError)
        json.put(KEY_LAST_SERVICE_STARTED, state.lastServiceStartedAt)
        json.put(KEY_LAST_SERVICE_FINISHED, state.lastServiceFinishedAt)
        json.put(KEY_LAST_FETCH_STARTED, state.lastFetchStartedAt)
        putNullableBoolean(json, KEY_LAST_FETCH_SUCCESS, state.lastFetchSuccess)
        putNullableInt(json, KEY_LAST_FETCH_HTTP, state.lastFetchHttpCode)
        putNullableString(json, KEY_LAST_FETCH_ERROR, state.lastFetchError)
        json.put(KEY_LAST_ASSETS_COUNT, state.lastAssetsCount)
        json.put(KEY_LAST_PRICES_COUNT, state.lastPricesCount)
        json.put(KEY_LAST_TRIGGERED, state.lastTriggeredAlerts)
        json.put(KEY_LAST_SKIPPED_NO_PRICE, state.lastSkippedNoPrice)
        json.put(KEY_LAST_SKIPPED_DISABLED, state.lastSkippedDisabled)
        json.put(KEY_LAST_NOTIFICATION_SENT, state.lastNotificationSentAt)
        prefs.edit().putString(KEY_STATE, json.toString()).apply()
    }

    fun update(block: (DiagnosticsState) -> DiagnosticsState) {
        val current = getState()
        setState(block(current))
    }

    private fun putNullableBoolean(json: JSONObject, key: String, value: Boolean?) {
        if (value == null) {
            json.remove(key)
        } else {
            json.put(key, value)
        }
    }

    private fun putNullableInt(json: JSONObject, key: String, value: Int?) {
        if (value == null) {
            json.remove(key)
        } else {
            json.put(key, value)
        }
    }

    private fun putNullableString(json: JSONObject, key: String, value: String?) {
        if (value.isNullOrBlank()) {
            json.remove(key)
        } else {
            json.put(key, value)
        }
    }

    companion object {
        private const val KEY_STATE = "diagnostics_state"
        private const val KEY_LAST_ALARM_SCHEDULED = "last_alarm_scheduled_at"
        private const val KEY_NEXT_ALARM_AT = "next_alarm_at"
        private const val KEY_LAST_RECEIVER_FIRED = "last_receiver_fired_at"
        private const val KEY_LAST_EXACT_ALLOWED = "last_exact_alarm_allowed"
        private const val KEY_LAST_JOB_SCHEDULED_AT = "last_job_scheduled_at"
        private const val KEY_LAST_JOB_RESULT = "last_job_schedule_result"
        private const val KEY_LAST_JOB_ERROR = "last_job_schedule_error"
        private const val KEY_LAST_SERVICE_STARTED = "last_service_started_at"
        private const val KEY_LAST_SERVICE_FINISHED = "last_service_finished_at"
        private const val KEY_LAST_FETCH_STARTED = "last_fetch_started_at"
        private const val KEY_LAST_FETCH_SUCCESS = "last_fetch_success"
        private const val KEY_LAST_FETCH_HTTP = "last_fetch_http_code"
        private const val KEY_LAST_FETCH_ERROR = "last_fetch_error"
        private const val KEY_LAST_ASSETS_COUNT = "last_assets_count"
        private const val KEY_LAST_PRICES_COUNT = "last_prices_count"
        private const val KEY_LAST_TRIGGERED = "last_triggered_alerts"
        private const val KEY_LAST_SKIPPED_NO_PRICE = "last_skipped_no_price"
        private const val KEY_LAST_SKIPPED_DISABLED = "last_skipped_disabled"
        private const val KEY_LAST_NOTIFICATION_SENT = "last_notification_sent_at"

        fun create(context: Context): DiagnosticsRepository {
            val prefs = context.getSharedPreferences(SettingsRepository.PREFS_NAME, Context.MODE_PRIVATE)
            return DiagnosticsRepository(prefs)
        }
    }
}
