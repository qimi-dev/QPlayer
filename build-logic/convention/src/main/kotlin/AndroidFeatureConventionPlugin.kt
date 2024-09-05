import com.android.build.gradle.LibraryExtension
import com.qimi.app.qplayer.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("qplayer.android.library")
                apply("qplayer.hilt")
            }
        }
    }

}