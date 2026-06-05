pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "com.android.library" -> useVersion("8.7.3")
                "org.jetbrains.kotlin.android" -> useVersion("2.0.21")
                "com.google.devtools.ksp" -> useVersion("2.0.21-1.0.27")
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "testpulse-sdk"
