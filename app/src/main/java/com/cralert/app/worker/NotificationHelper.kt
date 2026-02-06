package com.cralert.app.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.cralert.app.R
import com.cralert.app.data.Alert
import com.cralert.app.ui.main.MainActivity

object NotificationHelper {
    const val CHANNEL_ALERTS = "price_alerts"
    const val CHANNEL_SERVICE = "price_service"
    private const val TEST_NOTIFICATION_ID = 9999

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS,
                context.getString(R.string.notification_channel),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
            }

            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE,
                context.getString(R.string.notification_service),
                NotificationManager.IMPORTANCE_LOW
            )

            manager.createNotificationChannel(alertsChannel)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    fun buildServiceNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, CHANNEL_SERVICE)
            .setContentTitle(context.getString(R.string.notification_service))
            .setContentText(context.getString(R.string.notification_service_desc))
            .setSmallIcon(android.R.drawable.ic_popup_sync)
            .setOngoing(true)
            .build()
    }

    fun sendAlert(context: Context, alert: Alert, currentPrice: Double) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            alert.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val conditionText = if (alert.condition.name == "ABOVE") "above" else "below"
        val pair = "${alert.symbol}/${alert.quoteSymbol}"
        val title = "$pair crossed $conditionText ${formatPrice(alert.targetPrice)} ${alert.quoteSymbol}"
        val text = "Current ${formatPrice(currentPrice)} ${alert.quoteSymbol}"

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(200, 200, 200))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(alert.id.hashCode(), notification)
    }

    fun sendTest(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            TEST_NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setContentTitle(context.getString(R.string.action_test_notification))
            .setContentText(context.getString(R.string.notification_trigger))
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(200, 200, 200))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(TEST_NOTIFICATION_ID, notification)
    }

    private fun formatPrice(value: Double): String {
        return if (value >= 1.0) {
            String.format("$%.2f", value)
        } else {
            String.format("$%.6f", value)
        }
    }
}
