package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.screens.AppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FinanceViewModel
import com.example.ui.viewmodel.FinanceViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val financeApp = application as FinanceApplication
        val factory = FinanceViewModelFactory(financeApp.repository)
        
        setContent {
            val viewModel: FinanceViewModel by viewModels { factory }
            val savedThemeSetting by viewModel.theme.collectAsState()
            
            val useDarkTheme = when (savedThemeSetting) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = useDarkTheme, dynamicColor = false) {
                AppNavigation(viewModel)
            }
        }
    }
}
