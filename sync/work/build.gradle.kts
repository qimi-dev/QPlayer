plugins {
    alias(libs.plugins.qplayer.android.library)
    alias(libs.plugins.qplayer.hilt)
}

android {
    namespace = "com.qimi.app.qplayer.sync.work"
}

dependencies {
    ksp(libs.hilt.ext.compiler)
    api(libs.androidx.work)
    implementation(libs.hilt.ext.work)

    implementation(projects.core.data)
}