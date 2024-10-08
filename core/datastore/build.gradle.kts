plugins {
    alias(libs.plugins.qplayer.android.library)
    alias(libs.plugins.qplayer.hilt)
}

android {
    namespace = "com.qimi.app.qplayer.core.datastore"
}

dependencies {
    api(libs.androidx.dataStore)
    api(projects.core.model)
}