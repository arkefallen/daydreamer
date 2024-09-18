pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        maven {
            url = uri("https://maven.google.com")
        }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url =  uri("https://jitpack.io") }
    }
}

rootProject.name = "DaydreamerApp"
include(":app")
include(":core:ui")
include(":core:util")
include(":data:mongo")
include(":data:room")
include(":feature:auth")
include(":feature:home")
include(":domain")
include(":feature:write")
include(":data:model")
include(":data:glitch")
