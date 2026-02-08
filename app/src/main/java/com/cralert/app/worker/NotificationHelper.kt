package com.cralert.app.worker

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.cralert.app.R
import com.cralert.app.data.Alert
import com.cralert.app.data.Condition
import com.cralert.app.ui.main.MainActivity

object NotificationHelper {
    const val CHANNEL_ALERTS = "price_alerts"
    const val CHANNEL_SERVICE = "price_service"
    private const val TEST_NOTIFICATION_ID = 9999
    private const val TAG = "NotificationHelper"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            ensureAlertsChannel(manager, context)
            ensureServiceChannel(manager, context)
        }
    }

    private fun ensureAlertsChannel(manager: NotificationManager, context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val existing = manager.getNotificationChannel(CHANNEL_ALERTS)
        if (existing != null) {
            manager.deleteNotificationChannel(CHANNEL_ALERTS)
        }
        val alertsChannel = NotificationChannel(
            CHANNEL_ALERTS,
            context.getString(R.string.notification_channel),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), audioAttributes)
            if (manager.isNotificationPolicyAccessGranted) {
                setBypassDnd(true)
            }
        }
        manager.createNotificationChannel(alertsChannel)
    }

    private fun ensureServiceChannel(manager: NotificationManager, context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val existing = manager.getNotificationChannel(CHANNEL_SERVICE)
        if (existing == null) {
            val serviceChannel = NotificationChannel(
                CHANNEL_SERVICE,
                context.getString(R.string.notification_service),
                NotificationManager.IMPORTANCE_LOW
            )
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

    fun sendAlert(
        context: Context,
        alert: Alert,
        currentPrice: Double,
        pricesUpdatedAt: Long,
        sentAt: Long = System.currentTimeMillis()
    ) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            alert.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val conditionText = if (alert.condition == Condition.ABOVE) {
            context.getString(R.string.notification_condition_above)
        } else {
            context.getString(R.string.notification_condition_below)
        }
        val pair = "${alert.symbol}/${alert.quoteSymbol}"
        val title = context.getString(
            R.string.notification_title,
            pair,
            conditionText,
            formatPrice(alert.targetPrice),
            alert.quoteSymbol
        )
        val sentAtText = formatDateTime(sentAt)
        val pricesAtText = if (pricesUpdatedAt > 0L) {
            formatDateTime(pricesUpdatedAt)
        } else {
            context.getString(R.string.notification_prices_unknown)
        }
        val text = context.getString(R.string.notification_text, sentAtText, pricesAtText)
        val bigText = context.getString(
            R.string.notification_big_text,
            sentAtText,
            pricesAtText,
            formatPrice(currentPrice),
            alert.quoteSymbol
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(200, 200, 200))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(alert.id.hashCode(), notification)
        Log.i(TAG, "Alert notification sent id=${alert.id} pair=$pair")
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
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(200, 200, 200))
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(TEST_NOTIFICATION_ID, notification)
        Log.i(TAG, "Test notification sent")
    }

    private fun formatPrice(value: Double): String {
        return String.format(java.util.Locale.US, "%.2f", value)
    }

    private fun formatDateTime(value: Long): String {
        val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return format.format(java.util.Date(value))
    }
}
