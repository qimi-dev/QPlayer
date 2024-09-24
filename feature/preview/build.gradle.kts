plugins {
    alias(libs.plugins.qplayer.android.feature)
    alias(libs.plugins.qplayer.android.library.compose)
}

android {
    namespace = "com.qimi.app.qplayer.feature.preview"
}

dependencies {
    implementation(libs.androidx.material3)
    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(projects.core.data)

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.ui)
    implementation(libs.media3.exoplayer.hls)
}