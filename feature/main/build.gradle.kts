plugins {
    alias(libs.plugins.qplayer.android.feature)
    alias(libs.plugins.qplayer.android.library.compose)
}

android {
    namespace = "com.qimi.app.qplayer.feature.main"
}

dependencies {
    implementation(libs.androidx.material3)
}