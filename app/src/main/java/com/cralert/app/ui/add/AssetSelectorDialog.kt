package com.cralert.app.ui.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import java.util.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.res.stringResource
import com.cralert.app.R
import com.cralert.app.data.Asset
import com.cralert.app.ui.widgets.AssetIcon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssetSelectorDialog(
    mode: AssetSelectorMode,
    assets: List<Asset>,
    loading: Boolean,
    onRefresh: () -> Unit,
    onDismiss: () -> Unit,
    onAssetSelected: (Asset) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val filtered = remember(assets, query) {
        val q = query.trim().lowercase(Locale.US)
        if (q.isEmpty()) {
            assets
        } else {
            assets.filter {
                it.symbol.lowercase(Locale.US).contains(q) || it.name.lowercase(Locale.US).contains(q)
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Text(
                            text = if (mode == AssetSelectorMode.BASE) {
                                stringResource(R.string.title_select_base)
                            } else {
                                stringResource(R.string.title_select_quote)
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = stringResource(R.string.action_close)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = onRefresh) {
                            Icon(
                                imageVector = Icons.Outlined.Refresh,
                                contentDescription = stringResource(R.string.refresh)
                            )
                        }
                    }
                )
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = stringResource(R.string.search_assets)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = stringResource(R.string.search_assets)
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                    if (loading) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        )
                    }
                }
                HorizontalDivider()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
                ) {
                    items(filtered, key = { it.id }) { asset ->
                        AssetRow(asset = asset, onClick = { onAssetSelected(asset) })
                    }
                    if (filtered.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.empty_assets),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AssetRow(
    asset: Asset,
    onClick: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        ListItem(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            leadingContent = {
                AssetIcon(
                    assetId = asset.id,
                    symbol = asset.symbol,
                    size = 40.dp
                )
            },
            headlineContent = {
                Text(
                    text = "${asset.symbol} · ${asset.name}",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall
                )
            },
            supportingContent = null
        )
    }
}
