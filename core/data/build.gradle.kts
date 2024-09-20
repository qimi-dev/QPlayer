plugins {
    alias(libs.plugins.qplayer.android.library)
    alias(libs.plugins.qplayer.hilt)
}

android {
    namespace = "com.qimi.app.qplayer.core.data"
}

dependencies {
    api(projects.core.network)
}