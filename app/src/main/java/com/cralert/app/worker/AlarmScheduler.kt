package com.cralert.app.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.cralert.app.data.local.DiagnosticsRepository
import kotlin.math.max

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    fun scheduleRepeating(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, PriceCheckService::class.java)
            .setAction(PriceCheckService.ACTION_ALARM)
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getService(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val settings = com.cralert.app.data.local.SettingsRepository.create(context)
        val intervalMs = settings.getCheckIntervalMs()
        val now = System.currentTimeMillis()
        val triggerAt = now + intervalMs
        val exactAllowed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
        Log.i(TAG, "Schedule alarm intervalMs=$intervalMs triggerAt=$triggerAt exactAllowed=$exactAllowed")
        DiagnosticsRepository.create(context).update { state ->
            state.copy(
                lastAlarmScheduledAt = now,
                nextAlarmAt = triggerAt,
                lastExactAlarmAllowed = exactAllowed
            )
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && exactAllowed) {
                val showIntent = PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, com.cralert.app.ui.main.MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                alarmManager.setAlarmClock(
                    AlarmManager.AlarmClockInfo(triggerAt, showIntent),
                    pendingIntent
                )
                Log.i(TAG, "Alarm set as alarm clock")
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    pendingIntent
                )
                Log.i(TAG, "Alarm set exact and allow while idle")
            }
        } catch (ex: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAt,
                pendingIntent
            )
            Log.w(TAG, "Alarm fallback to inexact: ${ex.message}")
        }
        scheduleJob(context, intervalMs)
    }

    private fun scheduleJob(context: Context, intervalMs: Long) {
        val jobScheduler = context.getSystemService(JobScheduler::class.java)
        val scheduledAt = System.currentTimeMillis()
        if (jobScheduler == null) {
            DiagnosticsRepository.create(context).update { state ->
                state.copy(
                    lastJobScheduledAt = scheduledAt,
                    lastJobScheduleResult = RESULT_JOB_SCHEDULER_NULL
                )
            }
            return
        }
        val component = ComponentName(context, PriceCheckJobService::class.java)
        val minInterval = JobInfo.getMinPeriodMillis()
        val period = max(intervalMs, minInterval)
        val minFlex = JobInfo.getMinFlexMillis()
        val flex = max(minFlex, period / 5)
        val (finalResult, errorText) = try {
            val persistedJob = JobInfo.Builder(JOB_ID, component)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(period, flex)
                .build()
            val result = jobScheduler.schedule(persistedJob)
            if (result == JobScheduler.RESULT_SUCCESS) {
                Log.i(TAG, "Job scheduled ok period=$period flex=$flex")
                result to null
            } else {
                val fallback = JobInfo.Builder(JOB_ID, component)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPersisted(false)
                    .setPeriodic(period, flex)
                    .build()
                val fallbackResult = jobScheduler.schedule(fallback)
                Log.w(TAG, "Job schedule fallback result=$fallbackResult period=$period flex=$flex")
                fallbackResult to null
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Job schedule failed: ${ex.javaClass.simpleName} ${ex.message}")
            RESULT_JOB_EXCEPTION to "${ex.javaClass.simpleName}: ${ex.message ?: "unknown"}"
        }
        DiagnosticsRepository.create(context).update { state ->
            state.copy(
                lastJobScheduledAt = scheduledAt,
                lastJobScheduleResult = finalResult,
                lastJobScheduleError = errorText
            )
        }
    }

    private const val JOB_ID = 22001
    private const val RESULT_JOB_SCHEDULER_NULL = -100
    private const val RESULT_JOB_EXCEPTION = -101
}
