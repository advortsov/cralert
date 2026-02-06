package com.cralert.app

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.cralert.app.di.ServiceLocator
import com.cralert.app.worker.AlarmScheduler
import com.cralert.app.worker.NotificationHelper

class CRAlertApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
        val darkMode = ServiceLocator.settingsRepository.isDarkModeEnabled()
        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        NotificationHelper.createChannels(this)
        AlarmScheduler.scheduleRepeating(this)
    }
}
