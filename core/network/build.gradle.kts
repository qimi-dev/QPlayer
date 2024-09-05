plugins {
    alias(libs.plugins.qplayer.android.library)
    alias(libs.plugins.qplayer.hilt)
}

android {
    namespace = "com.qimi.app.qplayer.core.network"
}

dependencies {
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
}