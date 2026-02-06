package com.cralert.app.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.cralert.app.data.local.SettingsDefaults
import com.cralert.app.databinding.ActivitySettingsBinding
import com.cralert.app.di.ServiceLocator

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        binding.toolbar.setNavigationOnClickListener { finish() }

        loadValues()

        binding.saveButton.setOnClickListener {
            saveValues()
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadValues() {
        val settings = ServiceLocator.settingsRepository
        binding.apiBaseUrlInput.setText(settings.getApiBaseUrl())
        binding.apiTokenInput.setText(settings.getApiToken())
        binding.iconBaseUrlInput.setText(settings.getIconBaseUrl())
        binding.assetsLimitInput.setText(settings.getAssetsLimit().toString())
        binding.cacheTtlInput.setText(settings.getCacheTtlHours().toString())
        binding.checkIntervalInput.setText(settings.getCheckIntervalMinutes().toString())
        binding.minTriggerInput.setText(settings.getMinTriggerIntervalMinutes().toString())
        binding.connectTimeoutInput.setText(settings.getConnectTimeoutMs().toString())
        binding.readTimeoutInput.setText(settings.getReadTimeoutMs().toString())
        binding.darkModeSwitch.isChecked = settings.isDarkModeEnabled()
    }

    private fun saveValues() {
        val settings = ServiceLocator.settingsRepository
        val apiBaseUrl = binding.apiBaseUrlInput.text?.toString()?.trim().orEmpty()
        val apiToken = binding.apiTokenInput.text?.toString()?.trim().orEmpty()
        val iconBaseUrl = binding.iconBaseUrlInput.text?.toString()?.trim().orEmpty()

        val assetsLimit = binding.assetsLimitInput.text?.toString()?.toIntOrNull() ?: SettingsDefaults.ASSETS_LIMIT
        val cacheTtlHours = binding.cacheTtlInput.text?.toString()?.toLongOrNull() ?: SettingsDefaults.CACHE_TTL_HOURS
        val checkIntervalMinutes = binding.checkIntervalInput.text?.toString()?.toLongOrNull()
            ?: SettingsDefaults.CHECK_INTERVAL_MINUTES
        val minTriggerMinutes = binding.minTriggerInput.text?.toString()?.toLongOrNull()
            ?: SettingsDefaults.MIN_TRIGGER_INTERVAL_MINUTES
        val connectTimeout = binding.connectTimeoutInput.text?.toString()?.toIntOrNull() ?: SettingsDefaults.CONNECT_TIMEOUT_MS
        val readTimeout = binding.readTimeoutInput.text?.toString()?.toIntOrNull() ?: SettingsDefaults.READ_TIMEOUT_MS
        val darkMode = binding.darkModeSwitch.isChecked

        if (apiBaseUrl.isNotBlank()) {
            settings.setApiBaseUrl(apiBaseUrl)
        } else {
            settings.setApiBaseUrl(SettingsDefaults.API_BASE_URL)
        }

        settings.setApiToken(apiToken)

        if (iconBaseUrl.isNotBlank()) {
            settings.setIconBaseUrl(iconBaseUrl)
        } else {
            settings.setIconBaseUrl(SettingsDefaults.ICON_BASE_URL)
        }

        settings.setAssetsLimit(assetsLimit)
        settings.setCacheTtlHours(cacheTtlHours)
        settings.setCheckIntervalMinutes(checkIntervalMinutes)
        settings.setMinTriggerIntervalMinutes(minTriggerMinutes)
        settings.setConnectTimeoutMs(connectTimeout)
        settings.setReadTimeoutMs(readTimeout)
        settings.setDarkModeEnabled(darkMode)

        AppCompatDelegate.setDefaultNightMode(
            if (darkMode) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        recreate()
    }
}
