package com.qimi.app.qplayer.core.ui

import kotlinx.coroutines.delay
import kotlin.math.pow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

suspend fun retry(
    count: Int = 3,
    rule: (Int) -> Duration = { 2.0.pow(it).seconds },
    action: suspend () -> Boolean
) {
    repeat(count) { index ->
        if (action()) {
            return@retry
        }
        delay(rule(index))
    }
}

