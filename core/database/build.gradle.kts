plugins {
    alias(libs.plugins.qplayer.android.library)
    alias(libs.plugins.qplayer.android.room)
    alias(libs.plugins.qplayer.hilt)
}

android {
    namespace = "com.qimi.app.qplayer.core.database"
}

dependencies {
    api(projects.core.model)
}