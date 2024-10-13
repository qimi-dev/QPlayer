package com.qimi.app.qplayer

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import com.qimi.app.qplayer.core.ui.AppState
import com.qimi.app.qplayer.core.ui.LocalAppState
import com.qimi.app.qplayer.core.ui.rememberAppState
import com.qimi.app.qplayer.navigation.QPlayerNavHost
import com.qimi.app.qplayer.ui.theme.QPlayerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlin.math.max

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
    var brushSize: Float by remember { mutableFloatStateOf(0f) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                brushSize = max(it.width, it.height).toFloat()
            }.background(
                brush = Brush.radialGradient(
                    0f to MaterialTheme.colorScheme.primary,
                    1f to MaterialTheme.colorScheme.background,
                    center = Offset(- brushSize / 3, - brushSize / 3),
                    radius = if (brushSize != 0f) brushSize else Float.POSITIVE_INFINITY
                )
            )
    ) {
        QPlayerNavHost()
    }
}




