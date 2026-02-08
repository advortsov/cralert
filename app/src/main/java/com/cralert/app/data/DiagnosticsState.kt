package com.cralert.app.data

data class DiagnosticsState(
    val lastAlarmScheduledAt: Long = 0L,
    val nextAlarmAt: Long = 0L,
    val lastReceiverFiredAt: Long = 0L,
    val lastExactAlarmAllowed: Boolean? = null,
    val lastJobScheduledAt: Long = 0L,
    val lastJobScheduleResult: Int? = null,
    val lastJobScheduleError: String? = null,
    val lastServiceStartedAt: Long = 0L,
    val lastServiceFinishedAt: Long = 0L,
    val lastFetchStartedAt: Long = 0L,
    val lastFetchSuccess: Boolean? = null,
    val lastFetchHttpCode: Int? = null,
    val lastFetchError: String? = null,
    val lastAssetsCount: Int = 0,
    val lastPricesCount: Int = 0,
    val lastTriggeredAlerts: Int = 0,
    val lastSkippedNoPrice: Int = 0,
    val lastSkippedDisabled: Int = 0,
    val lastNotificationSentAt: Long = 0L
)
