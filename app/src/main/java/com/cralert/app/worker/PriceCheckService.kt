package com.cralert.app.worker

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.cralert.app.data.local.DiagnosticsRepository
import com.cralert.app.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PriceCheckService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: "none"
        Log.i(TAG, "Service start action=$action startId=$startId")
        try {
            startForeground(SERVICE_ID, NotificationHelper.buildServiceNotification(this))
        } catch (ex: SecurityException) {
            Log.e(TAG, "Foreground start failed: ${ex.message}")
            stopSelf()
            return START_NOT_STICKY
        }

        serviceScope.launch {
            val repository = ServiceLocator.alertRepository
            val marketRepository = ServiceLocator.marketRepository
            val diagnostics = DiagnosticsRepository.create(this@PriceCheckService)

            val startAt = System.currentTimeMillis()
            val startedFromScheduler = intent?.action == ACTION_ALARM || intent?.action == ACTION_JOB
            Log.i(TAG, "Run started at=$startAt fromScheduler=$startedFromScheduler")
            diagnostics.update { state ->
                state.copy(
                    lastServiceStartedAt = startAt,
                    lastReceiverFiredAt = if (startedFromScheduler) startAt else state.lastReceiverFiredAt,
                    lastTriggeredAlerts = 0,
                    lastSkippedNoPrice = 0,
                    lastSkippedDisabled = 0
                )
            }

            val alerts = repository.getAlerts()
            Log.i(TAG, "Loaded alerts count=${alerts.size}")
            if (alerts.isEmpty()) {
                diagnostics.update { state ->
                    state.copy(lastServiceFinishedAt = System.currentTimeMillis())
                }
                stopSelf()
                return@launch
            }

            val enabledAlerts = alerts.filter { it.enabled }
            Log.i(TAG, "Enabled alerts count=${enabledAlerts.size}")
            if (enabledAlerts.isEmpty()) {
                diagnostics.update { state ->
                    state.copy(
                        lastSkippedDisabled = alerts.size,
                        lastServiceFinishedAt = System.currentTimeMillis()
                    )
                }
                stopSelf()
                return@launch
            }

            val ids = enabledAlerts.flatMap { listOf(it.assetId, it.quoteAssetId) }.distinct()
            diagnostics.update { state ->
                state.copy(lastFetchStartedAt = System.currentTimeMillis())
            }
            val fetchResult = marketRepository.fetchAssetsByIds(ids)
            val prices = marketRepository.getCachedPrices()
            val pricesUpdatedAt = marketRepository.getPricesTimestamp()
            Log.i(
                TAG,
                "Fetch result success=${fetchResult.success} http=${fetchResult.httpCode} assets=${fetchResult.data.size} prices=${prices.size}"
            )
            diagnostics.update { state ->
                state.copy(
                    lastFetchSuccess = fetchResult.success,
                    lastFetchHttpCode = fetchResult.httpCode,
                    lastFetchError = fetchResult.error,
                    lastAssetsCount = fetchResult.data.size,
                    lastPricesCount = prices.size
                )
            }
            val stats = ServiceLocator.alertCheckEngine.evaluate(
                this@PriceCheckService,
                alerts,
                prices,
                pricesUpdatedAt
            )

            diagnostics.update { state ->
                state.copy(
                    lastServiceFinishedAt = System.currentTimeMillis(),
                    lastTriggeredAlerts = stats.triggered,
                    lastSkippedNoPrice = stats.skippedNoPrice,
                    lastSkippedDisabled = stats.skippedDisabled,
                    lastNotificationSentAt = if (stats.triggered > 0) System.currentTimeMillis() else state.lastNotificationSentAt
                )
            }

            AlarmScheduler.scheduleRepeating(this@PriceCheckService)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "Service destroyed")
        serviceJob.cancel()
    }

    companion object {
        private const val SERVICE_ID = 1
        private const val TAG = "PriceCheckService"
        const val ACTION_ALARM = "com.cralert.app.ACTION_ALARM_CHECK"
        const val ACTION_JOB = "com.cralert.app.ACTION_JOB_CHECK"
        const val ACTION_DEBUG = "com.cralert.app.ACTION_DEBUG_CHECK"
    }
}
