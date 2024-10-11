package com.qimi.app.qplayer.core.ui

import android.graphics.Color
import android.util.Log
import androidx.activity.SystemBarStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb

val LocalAppState = compositionLocalOf {
    AppState(
        statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
    )
}

@Composable
fun rememberAppState(): AppState {
    val statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
    val navigationBarStyle = SystemBarStyle.light(
        MaterialTheme.colorScheme.background.toArgb(),
        MaterialTheme.colorScheme.background.toArgb()
    )
    return remember(statusBarStyle, navigationBarStyle) {
        AppState(statusBarStyle, navigationBarStyle)
    }
}

@Stable
class AppState(
    statusBarStyle: SystemBarStyle,
    navigationBarStyle: SystemBarStyle
) {

    var currentStatusBarStyle: SystemBarStyle by mutableStateOf(statusBarStyle)
        private set

    var currentNavigationBarStyle: SystemBarStyle by mutableStateOf(navigationBarStyle)
        private set

    var isBlack: Boolean by mutableStateOf(false)
        private set

    fun setSystemBarStyle(
        statusBarStyle: SystemBarStyle = currentStatusBarStyle,
        navigationBarStyle: SystemBarStyle = currentNavigationBarStyle,
    ) {
        isBlack = true
        currentStatusBarStyle = statusBarStyle
        currentNavigationBarStyle = navigationBarStyle
    }

}




















