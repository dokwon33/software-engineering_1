pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://repository.map.naver.com/archive/maven")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        // jcenter는 더 이상 사용되지 않으므로 삭제하거나 필요시 명시적으로 추가
        maven("https://repository.map.naver.com/archive/maven")
    }
}
rootProject.name = "UOSense"
include(":app")

 