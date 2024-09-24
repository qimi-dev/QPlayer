plugins {
    alias(libs.plugins.qplayer.android.library)
    alias(libs.plugins.qplayer.hilt)
}

android {
    namespace = "com.qimi.app.qplayer.core.network"
}

dependencies {
    api(projects.core.model)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
}