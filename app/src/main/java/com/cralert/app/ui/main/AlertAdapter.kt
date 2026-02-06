package com.cralert.app.ui.main

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cralert.app.data.Alert
import com.cralert.app.databinding.ItemAlertBinding
import com.cralert.app.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AlertAdapter(
    private val scope: CoroutineScope,
    private val onToggle: (Alert, Boolean) -> Unit,
    private val onDelete: (Alert) -> Unit
) : RecyclerView.Adapter<AlertAdapter.AlertViewHolder>() {

    private val items = mutableListOf<AlertUiModel>()

    fun submit(list: List<AlertUiModel>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlertViewHolder {
        val binding = ItemAlertBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AlertViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AlertViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    override fun onViewRecycled(holder: AlertViewHolder) {
        holder.clear()
        super.onViewRecycled(holder)
    }

    inner class AlertViewHolder(private val binding: ItemAlertBinding) : RecyclerView.ViewHolder(binding.root) {
        private var iconJob: Job? = null

        fun bind(model: AlertUiModel) {
            val alert = model.alert
            binding.title.text = "${alert.symbol}/${alert.quoteSymbol} · ${alert.name}"
            val conditionText = if (alert.condition.name == "ABOVE") {
                binding.root.context.getString(com.cralert.app.R.string.condition_above)
            } else {
                binding.root.context.getString(com.cralert.app.R.string.condition_below)
            }
            binding.subtitle.text = "$conditionText ${formatPrice(alert.targetPrice)} ${alert.quoteSymbol}"
            binding.price.text = model.currentPrice?.let { "Now ${formatPrice(it)} ${alert.quoteSymbol}" } ?: "No price"
            binding.toggle.setOnCheckedChangeListener(null)
            binding.toggle.isChecked = alert.enabled
            binding.toggle.setOnCheckedChangeListener { _, isChecked -> onToggle(alert, isChecked) }
            binding.delete.setOnClickListener { onDelete(alert) }

            iconJob?.cancel()
            binding.icon.setImageBitmap(null)
            iconJob = scope.launch {
                val bitmap = loadIcon(alert.assetId, alert.symbol)
                withContext(Dispatchers.Main) {
                    if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        if (bitmap != null) {
                            binding.icon.setImageBitmap(bitmap)
                        } else {
                            binding.icon.setImageResource(android.R.drawable.ic_menu_info_details)
                        }
                    }
                }
            }
        }

        fun clear() {
            iconJob?.cancel()
            iconJob = null
        }

        private suspend fun loadIcon(assetId: String, symbol: String): Bitmap? {
            return ServiceLocator.iconRepository.loadIcon(assetId, symbol)
        }

        private fun formatPrice(value: Double): String {
            return if (value >= 1.0) {
                String.format("$%.2f", value)
            } else {
                String.format("$%.6f", value)
            }
        }
    }
}
