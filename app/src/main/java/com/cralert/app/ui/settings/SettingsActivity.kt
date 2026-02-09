package com.cralert.app.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.cralert.app.di.ServiceLocator
import com.cralert.app.ui.theme.CRAlertTheme

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            CRAlertTheme(darkTheme = ServiceLocator.settingsRepository.isDarkModeEnabled()) {
                SettingsScreen(
                    settingsRepository = ServiceLocator.settingsRepository,
                    onBack = { finish() },
                    onOpenBatterySettings = { SettingsIntents.requestIgnoreBatteryOptimizations(this) },
                    onOpenNotificationSettings = { SettingsIntents.openNotificationSettings(this) },
                    onSaved = { darkMode ->
                        AppCompatDelegate.setDefaultNightMode(
                            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                        )
                        Toast.makeText(this, getString(com.cralert.app.R.string.action_saved), Toast.LENGTH_SHORT)
                            .show()
                        recreate()
                    }
                )
            }
        }
    }
}
