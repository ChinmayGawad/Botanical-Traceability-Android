package com.florachain.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.florachain.designsystem.FloraChainTheme
import com.florachain.presentation.navigation.FloraChainNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isDarkTheme by rememberSaveable { mutableStateOf(true) }
            var currentLanguage by rememberSaveable { mutableStateOf("en") }

            FloraChainTheme(
                darkTheme = isDarkTheme,
                languageCode = currentLanguage,
                onToggleTheme = { isDarkTheme = !isDarkTheme },
                onLanguageChange = { lang ->
                    currentLanguage = lang
                    applyAppLocale(lang)
                }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = FloraChainTheme.colors.background
                ) {
                    FloraChainNavHost()
                }
            }
        }
    }

    private fun applyAppLocale(langTag: String) {
        val locale = java.util.Locale(langTag)
        java.util.Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            getSystemService(android.app.LocaleManager::class.java)?.applicationLocales =
                android.os.LocaleList.forLanguageTags(langTag)
        }
    }
}
