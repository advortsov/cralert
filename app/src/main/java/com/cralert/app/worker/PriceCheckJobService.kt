package com.cralert.app.worker

import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import android.util.Log

class PriceCheckJobService : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        Log.i(TAG, "Job started id=${params?.jobId}")
        val intent = Intent(this, PriceCheckService::class.java)
            .setAction(PriceCheckService.ACTION_JOB)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.i(TAG, "Job stopped id=${params?.jobId}")
        return false
    }

    companion object {
        private const val TAG = "PriceCheckJobService"
    }
}
