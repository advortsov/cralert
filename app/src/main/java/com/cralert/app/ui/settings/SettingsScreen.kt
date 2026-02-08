package com.cralert.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cralert.app.R
import com.cralert.app.data.local.SettingsDefaults
import com.cralert.app.data.local.SettingsRepository
import com.cralert.app.di.ServiceLocator
import com.cralert.app.worker.PriceCheckService
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    onBack: () -> Unit,
    onSaved: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val diagnosticsRepository = ServiceLocator.diagnosticsRepository
    var apiBaseUrl by rememberSaveable { mutableStateOf(settingsRepository.getApiBaseUrl()) }
    var apiToken by rememberSaveable { mutableStateOf(settingsRepository.getApiToken()) }
    var iconBaseUrl by rememberSaveable { mutableStateOf(settingsRepository.getIconBaseUrl()) }
    var iconCacheTtlDays by rememberSaveable { mutableStateOf(settingsRepository.getIconCacheTtlDays().toString()) }
    var assetsLimit by rememberSaveable { mutableStateOf(settingsRepository.getAssetsLimit().toString()) }
    var cacheTtlHours by rememberSaveable { mutableStateOf(settingsRepository.getCacheTtlHours().toString()) }
    var checkIntervalMinutes by rememberSaveable { mutableStateOf(settingsRepository.getCheckIntervalMinutes().toString()) }
    var connectTimeoutMs by rememberSaveable { mutableStateOf(settingsRepository.getConnectTimeoutMs().toString()) }
    var readTimeoutMs by rememberSaveable { mutableStateOf(settingsRepository.getReadTimeoutMs().toString()) }
    var darkMode by rememberSaveable { mutableStateOf(settingsRepository.isDarkModeEnabled()) }
    var retryAttempts by rememberSaveable { mutableStateOf(settingsRepository.getRetryAttempts().toString()) }
    var retryBaseDelayMs by rememberSaveable { mutableStateOf(settingsRepository.getRetryBaseDelayMs().toString()) }
    var retryMaxDelayMs by rememberSaveable { mutableStateOf(settingsRepository.getRetryMaxDelayMs().toString()) }
    var diagnosticsState by remember { mutableStateOf(diagnosticsRepository.getState()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.title_settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                OutlinedTextField(
                    value = apiBaseUrl,
                    onValueChange = { apiBaseUrl = it },
                    label = { Text(text = stringResource(R.string.label_api_base_url)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = apiToken,
                    onValueChange = { apiToken = it },
                    label = { Text(text = stringResource(R.string.label_api_token)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = iconBaseUrl,
                    onValueChange = { iconBaseUrl = it },
                    label = { Text(text = stringResource(R.string.label_icon_base_url)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                OutlinedTextField(
                    value = iconCacheTtlDays,
                    onValueChange = { iconCacheTtlDays = it },
                    label = { Text(text = stringResource(R.string.label_icon_cache_ttl_days)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item {
                OutlinedTextField(
                    value = assetsLimit,
                    onValueChange = { assetsLimit = it },
                    label = { Text(text = stringResource(R.string.label_assets_limit)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item {
                OutlinedTextField(
                    value = cacheTtlHours,
                    onValueChange = { cacheTtlHours = it },
                    label = { Text(text = stringResource(R.string.label_cache_ttl_hours)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item {
                OutlinedTextField(
                    value = checkIntervalMinutes,
                    onValueChange = { checkIntervalMinutes = it },
                    label = { Text(text = stringResource(R.string.label_check_interval_minutes)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item {
                OutlinedTextField(
                    value = connectTimeoutMs,
                    onValueChange = { connectTimeoutMs = it },
                    label = { Text(text = stringResource(R.string.label_connect_timeout)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item {
                OutlinedTextField(
                    value = readTimeoutMs,
                    onValueChange = { readTimeoutMs = it },
                    label = { Text(text = stringResource(R.string.label_read_timeout)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item {
                OutlinedTextField(
                    value = retryAttempts,
                    onValueChange = { retryAttempts = it },
                    label = { Text(text = stringResource(R.string.label_retry_attempts)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item {
                OutlinedTextField(
                    value = retryBaseDelayMs,
                    onValueChange = { retryBaseDelayMs = it },
                    label = { Text(text = stringResource(R.string.label_retry_base_delay)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item {
                OutlinedTextField(
                    value = retryMaxDelayMs,
                    onValueChange = { retryMaxDelayMs = it },
                    label = { Text(text = stringResource(R.string.label_retry_max_delay)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
            item {
                Column {
                    Text(text = stringResource(R.string.label_theme), style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(6.dp))
                    RowSwitch(
                        title = stringResource(R.string.label_dark_mode),
                        checked = darkMode,
                        onCheckedChange = { darkMode = it }
                    )
                }
            }
            item {
                Button(
                    onClick = {
                        val assetsLimitValue = assetsLimit.toIntOrNull() ?: SettingsDefaults.ASSETS_LIMIT
                        val iconCacheTtlValue = iconCacheTtlDays.toLongOrNull()
                            ?: SettingsDefaults.ICON_CACHE_TTL_DAYS
                        val cacheTtlValue = cacheTtlHours.toLongOrNull() ?: SettingsDefaults.CACHE_TTL_HOURS
                        val checkIntervalValue = checkIntervalMinutes.toLongOrNull()
                            ?: SettingsDefaults.CHECK_INTERVAL_MINUTES
                        val connectTimeoutValue = connectTimeoutMs.toIntOrNull() ?: SettingsDefaults.CONNECT_TIMEOUT_MS
                        val readTimeoutValue = readTimeoutMs.toIntOrNull() ?: SettingsDefaults.READ_TIMEOUT_MS
                        val retryAttemptsValue = retryAttempts.toIntOrNull() ?: SettingsDefaults.RETRY_ATTEMPTS
                        val retryBaseDelayValue = retryBaseDelayMs.toLongOrNull() ?: SettingsDefaults.RETRY_BASE_DELAY_MS
                        val retryMaxDelayValue = retryMaxDelayMs.toLongOrNull() ?: SettingsDefaults.RETRY_MAX_DELAY_MS

                        settingsRepository.setApiBaseUrl(
                            apiBaseUrl.trim().ifBlank { SettingsDefaults.API_BASE_URL }
                        )
                        settingsRepository.setApiToken(apiToken.trim())
                        settingsRepository.setIconBaseUrl(
                            iconBaseUrl.trim().ifBlank { SettingsDefaults.ICON_BASE_URL }
                        )
                        settingsRepository.setIconCacheTtlDays(iconCacheTtlValue)
                        settingsRepository.setAssetsLimit(assetsLimitValue)
                        settingsRepository.setCacheTtlHours(cacheTtlValue)
                        settingsRepository.setCheckIntervalMinutes(checkIntervalValue)
                        settingsRepository.setConnectTimeoutMs(connectTimeoutValue)
                        settingsRepository.setReadTimeoutMs(readTimeoutValue)
                        settingsRepository.setDarkModeEnabled(darkMode)
                        settingsRepository.setRetryAttempts(retryAttemptsValue)
                        settingsRepository.setRetryBaseDelayMs(retryBaseDelayValue)
                        settingsRepository.setRetryMaxDelayMs(retryMaxDelayValue)

                        onSaved(darkMode)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.action_save))
                }
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            item {
                Text(text = stringResource(R.string.label_diagnostics), style = MaterialTheme.typography.titleMedium)
            }
            item {
                DiagnosticsRow(
                    stringResource(R.string.diag_last_alarm_scheduled),
                    formatTimestamp(diagnosticsState.lastAlarmScheduledAt)
                )
                DiagnosticsRow(stringResource(R.string.diag_next_alarm), formatTimestamp(diagnosticsState.nextAlarmAt))
                DiagnosticsRow(
                    stringResource(R.string.diag_receiver_fired),
                    formatTimestamp(diagnosticsState.lastReceiverFiredAt)
                )
                DiagnosticsRow(
                    stringResource(R.string.diag_exact_allowed),
                    diagnosticsState.lastExactAlarmAllowed?.toString() ?: "--"
                )
                DiagnosticsRow(
                    stringResource(R.string.diag_job_scheduled),
                    formatTimestamp(diagnosticsState.lastJobScheduledAt)
                )
                DiagnosticsRow(
                    stringResource(R.string.diag_job_result),
                    diagnosticsState.lastJobScheduleResult?.toString() ?: "--"
                )
                DiagnosticsRow(
                    stringResource(R.string.diag_job_error),
                    diagnosticsState.lastJobScheduleError ?: "--"
                )
                DiagnosticsRow(
                    stringResource(R.string.diag_service_started),
                    formatTimestamp(diagnosticsState.lastServiceStartedAt)
                )
                DiagnosticsRow(
                    stringResource(R.string.diag_service_finished),
                    formatTimestamp(diagnosticsState.lastServiceFinishedAt)
                )
                DiagnosticsRow(
                    stringResource(R.string.diag_fetch_started),
                    formatTimestamp(diagnosticsState.lastFetchStartedAt)
                )
                DiagnosticsRow(
                    stringResource(R.string.diag_fetch_success),
                    diagnosticsState.lastFetchSuccess?.toString() ?: "--"
                )
                DiagnosticsRow(
                    stringResource(R.string.diag_fetch_http),
                    diagnosticsState.lastFetchHttpCode?.toString() ?: "--"
                )
                DiagnosticsRow(
                    stringResource(R.string.diag_fetch_error),
                    diagnosticsState.lastFetchError ?: "--"
                )
                DiagnosticsRow(stringResource(R.string.diag_assets_count), diagnosticsState.lastAssetsCount.toString())
                DiagnosticsRow(stringResource(R.string.diag_prices_count), diagnosticsState.lastPricesCount.toString())
                DiagnosticsRow(
                    stringResource(R.string.diag_triggered_alerts),
                    diagnosticsState.lastTriggeredAlerts.toString()
                )
                DiagnosticsRow(
                    stringResource(R.string.diag_skipped_no_price),
                    diagnosticsState.lastSkippedNoPrice.toString()
                )
                DiagnosticsRow(
                    stringResource(R.string.diag_skipped_disabled),
                    diagnosticsState.lastSkippedDisabled.toString()
                )
                DiagnosticsRow(
                    stringResource(R.string.diag_last_notification),
                    formatTimestamp(diagnosticsState.lastNotificationSentAt)
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            startPriceCheck(context)
                            diagnosticsState = diagnosticsRepository.getState()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.action_run_check))
                    }
                    Button(
                        onClick = { diagnosticsState = diagnosticsRepository.getState() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = stringResource(R.string.action_refresh_diagnostics))
                    }
                }
            }
        }
    }
}

@Composable
private fun RowSwitch(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun DiagnosticsRow(label: String, value: String) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(text = value, style = MaterialTheme.typography.bodySmall)
    }
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0L) return "--"
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}

private fun startPriceCheck(context: android.content.Context) {
    val intent = android.content.Intent(context, PriceCheckService::class.java)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        context.startForegroundService(intent)
    } else {
        context.startService(intent)
    }
}
