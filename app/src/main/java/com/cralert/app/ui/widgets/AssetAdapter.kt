package com.cralert.app.ui.widgets

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cralert.app.data.Asset
import com.cralert.app.databinding.ItemAssetBinding
import com.cralert.app.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AssetAdapter(
    private val scope: CoroutineScope,
    private val onClick: (Asset) -> Unit
) : RecyclerView.Adapter<AssetAdapter.AssetViewHolder>() {

    private val items = mutableListOf<Asset>()
    private val allItems = mutableListOf<Asset>()

    fun submit(list: List<Asset>) {
        items.clear()
        allItems.clear()
        items.addAll(list)
        allItems.addAll(list)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        val q = query.trim().lowercase()
        items.clear()
        if (q.isEmpty()) {
            items.addAll(allItems)
        } else {
            items.addAll(allItems.filter {
                it.symbol.lowercase().contains(q) || it.name.lowercase().contains(q)
            })
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssetViewHolder {
        val binding = ItemAssetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AssetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AssetViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    override fun onViewRecycled(holder: AssetViewHolder) {
        holder.clear()
        super.onViewRecycled(holder)
    }

    inner class AssetViewHolder(private val binding: ItemAssetBinding) : RecyclerView.ViewHolder(binding.root) {
        private var iconJob: Job? = null

        fun bind(asset: Asset) {
            binding.title.text = "${asset.symbol} · ${asset.name}"
            binding.subtitle.text = "${formatPrice(asset.priceUsd)}"

            binding.root.setOnClickListener { onClick(asset) }

            iconJob?.cancel()
            binding.icon.setImageBitmap(null)
            iconJob = scope.launch {
                val bitmap = loadIcon(asset)
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

        private suspend fun loadIcon(asset: Asset): Bitmap? {
            return ServiceLocator.iconRepository.loadIcon(asset.id, asset.symbol)
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
