package com.cralert.app.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberDismissState
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Delete
import com.cralert.app.R
import com.cralert.app.data.Alert
import com.cralert.app.data.Condition
import com.cralert.app.ui.widgets.AssetIcon
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onAddAlert: () -> Unit,
    onOpenSettings: () -> Unit,
    onTestNotification: () -> Unit
) {
    val alerts by viewModel.alerts.observeAsState(emptyList())
    val cached by viewModel.cached.observeAsState(false)
    val loading by viewModel.loading.observeAsState(false)
    val lastUpdateAt by viewModel.lastUpdateAt.observeAsState(0L)
    val error by viewModel.error.observeAsState(null)
    val context = LocalContext.current

    val pullRefreshState = rememberPullRefreshState(
        refreshing = loading,
        onRefresh = { viewModel.refreshPrices() }
    )

    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.title_alerts)) },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.label_updated_at, formatTime(lastUpdateAt)),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        IconButton(onClick = onOpenSettings) {
                            Icon(
                                imageVector = Icons.Outlined.Settings,
                                contentDescription = stringResource(R.string.action_settings)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAlert) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = stringResource(R.string.action_add)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .pullRefresh(pullRefreshState)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (cached) {
                    item {
                        Text(
                            text = stringResource(R.string.cached),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                item {
                    TextButton(onClick = onTestNotification) {
                        Text(text = stringResource(R.string.action_test_notification))
                    }
                }
                if (alerts.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.empty_alerts),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(alerts, key = { it.alert.id }) { model ->
                        SwipeToDeleteRow(
                            model = model,
                            onToggle = { alert, enabled -> viewModel.toggleAlert(alert, enabled) },
                            onDelete = { alert -> viewModel.deleteAlert(alert) }
                        )
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline,
                            thickness = 0.6.dp
                        )
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = loading,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(1f)
            )
        }
    }
}

@Composable
private fun AlertRow(
    model: AlertUiModel,
    onToggle: (Alert, Boolean) -> Unit,
    onDelete: (Alert) -> Unit
) {
    val alert = model.alert
    val conditionText = if (alert.condition == Condition.ABOVE) {
        stringResource(R.string.condition_above)
    } else {
        stringResource(R.string.condition_below)
    }
    val conditionColor = if (alert.condition == Condition.ABOVE) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.error
    }
    val currentPrice = model.currentPrice?.let { formatAmount(it) } ?: "--"

    Surface(color = MaterialTheme.colorScheme.surface) {
        ListItem(
            modifier = Modifier.fillMaxWidth(),
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            leadingContent = {
                AssetIcon(
                    assetId = alert.assetId,
                    symbol = alert.symbol,
                    size = 48.dp
                )
            },
            headlineContent = {
                Text(
                    text = "${alert.symbol}/${alert.quoteSymbol}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            supportingContent = {
                Text(
                    text = "$conditionText ${formatAmount(alert.targetPrice)} ${alert.quoteSymbol}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = conditionColor
                )
            },
            trailingContent = {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$currentPrice ${alert.quoteSymbol}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = alert.enabled,
                            onCheckedChange = { onToggle(alert, it) }
                        )
                        IconButton(onClick = { onDelete(alert) }) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = stringResource(R.string.action_delete),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SwipeToDeleteRow(
    model: AlertUiModel,
    onToggle: (Alert, Boolean) -> Unit,
    onDelete: (Alert) -> Unit
) {
    val dismissState = rememberDismissState(
        confirmStateChange = { value ->
            if (value == DismissValue.DismissedToStart) {
                onDelete(model.alert)
                true
            } else {
                false
            }
        }
    )

    SwipeToDismiss(
        state = dismissState,
        directions = setOf(DismissDirection.EndToStart),
        background = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.CenterEnd,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        },
        dismissContent = {
            AlertRow(
                model = model,
                onToggle = onToggle,
                onDelete = onDelete
            )
        }
    )
}

private fun formatAmount(value: Double): String {
    return String.format(Locale.US, "%.2f", value)
}

private fun formatTime(timestamp: Long): String {
    if (timestamp <= 0L) return "--:--"
    val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(formatter)
}
