package com.qimi.app.qplayer.feature.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun MainRoute(
    modifier: Modifier = Modifier
) {
    MainScreen(
        modifier = modifier
    )
}

@Composable
internal fun MainScreen(
    modifier: Modifier = Modifier
) {
    Button(onClick = {}) {
        Text(text = "click")
    }
}
