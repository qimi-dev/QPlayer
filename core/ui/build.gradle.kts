plugins {
    alias(libs.plugins.qplayer.android.library)
    alias(libs.plugins.qplayer.android.library.compose)
}

android {
    namespace = "com.qimi.app.qplayer.core.ui"
}

dependencies {
    implementation(libs.androidx.material3)

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.ui)
    implementation(libs.media3.exoplayer.hls)
    api(libs.lottie.compose)
}