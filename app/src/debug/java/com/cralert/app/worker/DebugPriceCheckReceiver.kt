package com.cralert.app.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class DebugPriceCheckReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.i(TAG, "Debug receiver fired action=${intent.action}")
        val serviceIntent = Intent(context, PriceCheckService::class.java)
            .setAction(PriceCheckService.ACTION_DEBUG)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    companion object {
        private const val TAG = "DebugPriceCheck"
    }
}
