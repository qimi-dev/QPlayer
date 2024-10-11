package com.qimi.app.qplayer

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import com.qimi.app.qplayer.core.ui.AppState
import com.qimi.app.qplayer.core.ui.LocalAppState
import com.qimi.app.qplayer.core.ui.rememberAppState
import com.qimi.app.qplayer.navigation.QPlayerNavHost
import com.qimi.app.qplayer.ui.theme.QPlayerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val appState: AppState = rememberAppState()
            QPlayerTheme {
                LaunchedEffect(appState.currentStatusBarStyle, appState.currentNavigationBarStyle) {
                    enableEdgeToEdge(
                        statusBarStyle = appState.currentStatusBarStyle,
                        navigationBarStyle = appState.currentNavigationBarStyle
                    )
                }
                CompositionLocalProvider(LocalAppState provides appState) {
                    QPlayerApp(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

}

@Composable
internal fun QPlayerApp(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        QPlayerNavHost()
    }
}




