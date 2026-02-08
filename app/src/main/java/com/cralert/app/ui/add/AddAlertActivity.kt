package com.cralert.app.ui.add

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.cralert.app.di.ServiceLocator
import com.cralert.app.ui.theme.CRAlertTheme

class AddAlertActivity : AppCompatActivity() {

    private lateinit var viewModel: AddAlertViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = AddAlertViewModelFactory(
            ServiceLocator.alertRepository,
            ServiceLocator.marketRepository,
            ServiceLocator.settingsRepository
        )
        viewModel = ViewModelProvider(this, factory)[AddAlertViewModel::class.java]

        setContent {
            CRAlertTheme(darkTheme = ServiceLocator.settingsRepository.isDarkModeEnabled()) {
                AddAlertScreen(
                    viewModel = viewModel,
                    onBack = { finish() },
                    onSaved = { finish() }
                )
            }
        }
    }
}
