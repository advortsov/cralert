package com.cralert.app.ui.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.cralert.app.di.ServiceLocator
import com.cralert.app.ui.theme.CRAlertTheme

class AssetDetailsActivity : ComponentActivity() {

    private val viewModel: AssetDetailsViewModel by viewModels {
        AssetDetailsViewModelFactory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val baseId = intent.getStringExtra(EXTRA_BASE_ID).orEmpty()
        val baseSymbol = intent.getStringExtra(EXTRA_BASE_SYMBOL).orEmpty()
        val baseName = intent.getStringExtra(EXTRA_BASE_NAME).orEmpty()
        val quoteId = intent.getStringExtra(EXTRA_QUOTE_ID).orEmpty()
        val quoteSymbol = intent.getStringExtra(EXTRA_QUOTE_SYMBOL).orEmpty()
        val quoteName = intent.getStringExtra(EXTRA_QUOTE_NAME).orEmpty()

        viewModel.init(baseId, baseSymbol, baseName, quoteId, quoteSymbol, quoteName)

        setContent {
            CRAlertTheme(darkTheme = ServiceLocator.settingsRepository.isDarkModeEnabled()) {
                AssetDetailsScreen(
                    viewModel = viewModel,
                    onBack = { finish() }
                )
            }
        }
    }

    private class AssetDetailsViewModelFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AssetDetailsViewModel::class.java)) {
                val historyRepository = ServiceLocator.historyRepository
                val marketRepository = ServiceLocator.marketRepository
                @Suppress("UNCHECKED_CAST")
                return AssetDetailsViewModel(historyRepository, marketRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel")
        }
    }

    companion object {
        private const val EXTRA_BASE_ID = "extra_base_id"
        private const val EXTRA_BASE_SYMBOL = "extra_base_symbol"
        private const val EXTRA_BASE_NAME = "extra_base_name"
        private const val EXTRA_QUOTE_ID = "extra_quote_id"
        private const val EXTRA_QUOTE_SYMBOL = "extra_quote_symbol"
        private const val EXTRA_QUOTE_NAME = "extra_quote_name"

        fun createIntent(
            context: Context,
            baseId: String,
            baseSymbol: String,
            baseName: String,
            quoteId: String,
            quoteSymbol: String,
            quoteName: String
        ): Intent {
            return Intent(context, AssetDetailsActivity::class.java).apply {
                putExtra(EXTRA_BASE_ID, baseId)
                putExtra(EXTRA_BASE_SYMBOL, baseSymbol)
                putExtra(EXTRA_BASE_NAME, baseName)
                putExtra(EXTRA_QUOTE_ID, quoteId)
                putExtra(EXTRA_QUOTE_SYMBOL, quoteSymbol)
                putExtra(EXTRA_QUOTE_NAME, quoteName)
            }
        }
    }
}
