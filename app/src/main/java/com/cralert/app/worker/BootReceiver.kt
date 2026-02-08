package com.cralert.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Log.i(TAG, "Boot receiver fired action=${intent?.action}")
        AlarmScheduler.scheduleRepeating(context)
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
