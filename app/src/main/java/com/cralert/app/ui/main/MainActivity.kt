package com.cralert.app.ui.main

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.cralert.app.di.ServiceLocator
import com.cralert.app.ui.add.AddAlertActivity
import com.cralert.app.ui.settings.SettingsActivity
import com.cralert.app.ui.theme.CRAlertTheme
import com.cralert.app.worker.NotificationHelper

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = MainViewModelFactory(ServiceLocator.alertRepository, ServiceLocator.marketRepository)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        setContent {
            CRAlertTheme(darkTheme = ServiceLocator.settingsRepository.isDarkModeEnabled()) {
                MainScreen(
                    viewModel = viewModel,
                    onAddAlert = { startActivity(Intent(this, AddAlertActivity::class.java)) },
                    onOpenSettings = { startActivity(Intent(this, SettingsActivity::class.java)) },
                    onTestNotification = { NotificationHelper.sendTest(this) }
                )
            }
        }

        requestNotificationPermissionIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadAlerts()
        viewModel.refreshPrices()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
