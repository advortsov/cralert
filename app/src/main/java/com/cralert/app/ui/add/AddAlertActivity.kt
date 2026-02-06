package com.cralert.app.ui.add

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cralert.app.data.Condition
import com.cralert.app.databinding.ActivityAddAlertBinding
import com.cralert.app.di.ServiceLocator
import com.cralert.app.ui.widgets.AssetSelectorDialog

class AddAlertActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAlertBinding
    private lateinit var viewModel: AddAlertViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAlertBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val factory = AddAlertViewModelFactory(
            ServiceLocator.alertRepository,
            ServiceLocator.marketRepository,
            ServiceLocator.settingsRepository
        )
        viewModel = ViewModelProvider(this, factory)[AddAlertViewModel::class.java]

        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material)

        binding.baseAssetInput.setOnClickListener { openSelector(AssetSelectorMode.BASE) }
        binding.baseAssetInputLayout.setEndIconOnClickListener { openSelector(AssetSelectorMode.BASE) }

        binding.quoteAssetInput.setOnClickListener { openSelector(AssetSelectorMode.QUOTE) }
        binding.quoteAssetInputLayout.setEndIconOnClickListener { openSelector(AssetSelectorMode.QUOTE) }

        binding.conditionAbove.isChecked = true

        binding.saveButton.setOnClickListener {
            val priceText = binding.priceInput.text?.toString()?.trim().orEmpty()
            val price = priceText.toDoubleOrNull()
            val base = viewModel.selectedBaseAsset.value
            val quote = viewModel.selectedQuoteAsset.value
            if (base == null || quote == null || price == null) {
                Toast.makeText(this, "Select base, quote and price", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val condition = if (binding.conditionAbove.isChecked) Condition.ABOVE else Condition.BELOW
            viewModel.saveAlert(price, condition)
            finish()
        }

        viewModel.assets.observe(this) { assets ->
            if (assets.isNotEmpty()) {
                if (viewModel.selectedBaseAsset.value == null) {
                    viewModel.selectBaseAsset(assets.first())
                }
                if (viewModel.selectedQuoteAsset.value == null) {
                    val usdt = assets.firstOrNull { it.symbol.equals("USDT", true) }
                    if (usdt != null && viewModel.selectedBaseAsset.value?.id != usdt.id) {
                        viewModel.selectQuoteAsset(usdt)
                    } else if (assets.size > 1) {
                        viewModel.selectQuoteAsset(assets[1])
                    }
                }
            }
        }

        viewModel.selectedBaseAsset.observe(this) { asset ->
            if (asset != null) {
                binding.baseAssetInput.setText("${asset.symbol} · ${asset.name}")
            }
        }

        viewModel.selectedQuoteAsset.observe(this) { asset ->
            if (asset != null) {
                binding.quoteAssetInput.setText("${asset.symbol} · ${asset.name}")
            }
        }

        viewModel.loadAssets(forceRefresh = false)
    }

    private fun openSelector(mode: AssetSelectorMode) {
        val dialog = AssetSelectorDialog.newInstance(mode)
        dialog.show(supportFragmentManager, "asset_selector")
    }
}
