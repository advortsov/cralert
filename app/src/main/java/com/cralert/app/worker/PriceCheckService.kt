package com.cralert.app.worker

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.cralert.app.data.AlertEvaluator
import com.cralert.app.data.PriceCalculator
import com.cralert.app.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PriceCheckService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(SERVICE_ID, NotificationHelper.buildServiceNotification(this))

        serviceScope.launch {
            val repository = ServiceLocator.alertRepository
            val marketRepository = ServiceLocator.marketRepository
            val settings = ServiceLocator.settingsRepository

            val alerts = repository.getAlerts().filter { it.enabled }
            if (alerts.isEmpty()) {
                stopSelf()
                return@launch
            }

            val ids = alerts.flatMap { listOf(it.assetId, it.quoteAssetId) }.distinct()
            marketRepository.fetchAssetsByIds(ids)
            val prices = marketRepository.getCachedPrices()
            val minIntervalMs = settings.getMinTriggerIntervalMs()

            val now = System.currentTimeMillis()
            alerts.forEach { alert ->
                val price = PriceCalculator.currentPrice(alert, prices) ?: return@forEach
                if (AlertEvaluator.shouldTrigger(alert, price, now, minIntervalMs)) {
                    NotificationHelper.sendAlert(this@PriceCheckService, alert, price)
                    val updated = alert.copy(lastTriggeredAt = now)
                    repository.updateAlert(updated)
                }
            }

            AlarmScheduler.scheduleRepeating(this@PriceCheckService)
            stopSelf()
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    private companion object {
        private const val SERVICE_ID = 1
    }
}
