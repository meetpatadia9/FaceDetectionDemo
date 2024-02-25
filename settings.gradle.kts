pluginManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("http://dl.bintray.com/fotoapparat/fotoapparat")
            isAllowInsecureProtocol = true
            /*
            * For insecure HTTP connections in Gradle 7+ versions,
            * we need to specify a boolean allowInsecureProtocol
            * as true to MavenArtifactRepository closure.
            * */
        }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven { url = uri("https://jitpack.io") }
        maven {
            url = uri("http://dl.bintray.com/fotoapparat/fotoapparat")
            isAllowInsecureProtocol = true
            /*
            * For insecure HTTP connections in Gradle 7+ versions,
            * we need to specify a boolean allowInsecureProtocol
            * as true to MavenArtifactRepository closure.
            * */
        }
        mavenCentral()
    }
}

rootProject.name = "FaceDetectionDemo"
include(":app")
 