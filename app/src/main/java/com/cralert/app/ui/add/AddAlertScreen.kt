package com.cralert.app.ui.add

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cralert.app.R
import com.cralert.app.data.Condition

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAlertScreen(
    viewModel: AddAlertViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val assets by viewModel.assets.observeAsState(emptyList())
    val base by viewModel.selectedBaseAsset.observeAsState()
    val quote by viewModel.selectedQuoteAsset.observeAsState()
    val loading by viewModel.loading.observeAsState(false)
    val error by viewModel.error.observeAsState()

    var priceText by rememberSaveable { mutableStateOf("") }
    var condition by rememberSaveable { mutableStateOf(Condition.ABOVE) }
    var selectorMode by remember { mutableStateOf<AssetSelectorMode?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadAssets(forceRefresh = false)
    }

    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(assets) {
        if (assets.isNotEmpty()) {
            if (base == null) {
                viewModel.selectBaseAsset(assets.first())
            }
            if (quote == null) {
                val usdt = assets.firstOrNull { it.symbol.equals("USDT", true) }
                if (usdt != null && base?.id != usdt.id) {
                    viewModel.selectQuoteAsset(usdt)
                } else if (assets.size > 1) {
                    viewModel.selectQuoteAsset(assets[1])
                }
            }
        }
    }

    if (selectorMode != null) {
        AssetSelectorDialog(
            mode = selectorMode!!,
            assets = assets,
            loading = loading,
            onRefresh = { viewModel.loadAssets(forceRefresh = true) },
            onDismiss = { selectorMode = null },
            onAssetSelected = { asset ->
                if (selectorMode == AssetSelectorMode.BASE) {
                    viewModel.selectBaseAsset(asset)
                } else {
                    viewModel.selectQuoteAsset(asset)
                }
                selectorMode = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.title_add_alert)) },
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
                    value = base?.let { "${it.symbol} · ${it.name}" } ?: "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.label_base_asset)) },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { selectorMode = AssetSelectorMode.BASE }) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = stringResource(R.string.action_select_base)
                            )
                        }
                    }
                )
            }
            item {
                OutlinedTextField(
                    value = quote?.let { "${it.symbol} · ${it.name}" } ?: "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.label_quote_asset)) },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { selectorMode = AssetSelectorMode.QUOTE }) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = stringResource(R.string.action_select_quote)
                            )
                        }
                    }
                )
            }
            item {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(text = stringResource(R.string.label_price)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            item {
                Text(
                    text = stringResource(R.string.label_condition),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = condition == Condition.ABOVE,
                        onClick = { condition = Condition.ABOVE }
                    )
                    Text(text = stringResource(R.string.condition_above))
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(
                        selected = condition == Condition.BELOW,
                        onClick = { condition = Condition.BELOW }
                    )
                    Text(text = stringResource(R.string.condition_below))
                }
            }
            if (!error.isNullOrBlank()) {
                item {
                    Text(
                        text = error.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            item {
                Button(
                    onClick = {
                        val price = priceText.trim().toDoubleOrNull()
                        if (base == null || quote == null || price == null) {
                            Toast.makeText(
                                context,
                                context.getString(R.string.error_select_assets_price),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@Button
                        }
                        viewModel.saveAlert(price, condition)
                        onSaved()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.action_save))
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}
