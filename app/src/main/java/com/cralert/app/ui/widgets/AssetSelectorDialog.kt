package com.cralert.app.ui.widgets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.cralert.app.databinding.DialogAssetSelectorBinding
import com.cralert.app.di.ServiceLocator
import com.cralert.app.ui.add.AddAlertViewModel
import com.cralert.app.ui.add.AddAlertViewModelFactory
import com.cralert.app.ui.add.AssetSelectorMode
import kotlinx.coroutines.MainScope

class AssetSelectorDialog : DialogFragment() {

    private var _binding: DialogAssetSelectorBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddAlertViewModel
    private val scope = MainScope()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAssetSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val factory = AddAlertViewModelFactory(
            ServiceLocator.alertRepository,
            ServiceLocator.marketRepository,
            ServiceLocator.settingsRepository
        )
        viewModel = ViewModelProvider(requireActivity(), factory)[AddAlertViewModel::class.java]

        val mode = arguments?.getString(ARG_MODE)?.let { AssetSelectorMode.valueOf(it) }
            ?: AssetSelectorMode.BASE

        val adapter = AssetAdapter(scope) { asset ->
            if (mode == AssetSelectorMode.BASE) {
                viewModel.selectBaseAsset(asset)
            } else {
                viewModel.selectQuoteAsset(asset)
            }
            dismiss()
        }

        binding.assetsList.layoutManager = LinearLayoutManager(requireContext())
        binding.assetsList.adapter = adapter

        viewModel.assets.observe(viewLifecycleOwner) { list ->
            adapter.submit(list)
        }

        binding.searchView.setIconifiedByDefault(false)
        binding.searchView.isIconified = false

        binding.refreshButton.setOnClickListener {
            viewModel.loadAssets(forceRefresh = true)
        }

        binding.searchView.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filter(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText.orEmpty())
                return true
            }
        })

        if (viewModel.assets.value.isNullOrEmpty()) {
            viewModel.loadAssets(forceRefresh = false)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_MODE = "mode"

        fun newInstance(mode: AssetSelectorMode): AssetSelectorDialog {
            val dialog = AssetSelectorDialog()
            dialog.arguments = Bundle().apply {
                putString(ARG_MODE, mode.name)
            }
            return dialog
        }
    }
}
