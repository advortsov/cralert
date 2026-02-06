package com.cralert.app.ui.main

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cralert.app.databinding.ActivityMainBinding
import com.cralert.app.di.ServiceLocator
import com.cralert.app.ui.add.AddAlertActivity
import com.cralert.app.ui.settings.SettingsActivity
import com.cralert.app.worker.NotificationHelper
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private val scope = MainScope()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        val factory = MainViewModelFactory(ServiceLocator.alertRepository, ServiceLocator.marketRepository)
        viewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        val adapter = AlertAdapter(
            scope = scope,
            onToggle = { alert, enabled -> viewModel.toggleAlert(alert, enabled) },
            onDelete = { alert -> viewModel.deleteAlert(alert) }
        )
        binding.alertsList.layoutManager = LinearLayoutManager(this)
        binding.alertsList.adapter = adapter

        viewModel.alerts.observe(this) { list ->
            adapter.submit(list)
            binding.emptyText.visibility = if (list.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        }
        viewModel.cached.observe(this) { cached ->
            binding.statusText.visibility = if (cached) android.view.View.VISIBLE else android.view.View.GONE
            if (cached) {
                binding.statusText.text = getString(com.cralert.app.R.string.cached)
            }
        }
        viewModel.loading.observe(this) { loading ->
            binding.swipeRefresh.isRefreshing = loading
        }

        binding.fabAdd.setOnClickListener {
            startActivity(Intent(this, AddAlertActivity::class.java))
        }
        binding.testNotificationButton.setOnClickListener {
            NotificationHelper.sendTest(this)
        }
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshPrices()
        }

        requestNotificationPermissionIfNeeded()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadAlerts()
        viewModel.refreshPrices()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(com.cralert.app.R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            com.cralert.app.R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
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
