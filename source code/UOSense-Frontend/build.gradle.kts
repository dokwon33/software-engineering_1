// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("org.jetbrains.dokka") version "1.8.10" apply false
}
tasks {
    register("generateDocs", org.jetbrains.dokka.gradle.DokkaTask::class) {
        outputDirectory.set(buildDir.resolve("docs"))
        dokkaSourceSets {
            named("main") {
                includes.from("README.md") // 선택 사항
            }
        }
    }
}

buildscript {
    repositories {
        google() // Google 라이브러리 저장소 추가
        mavenCentral()
    }
    dependencies {
        classpath ("com.android.tools.build:gradle:8.1.1")  // 최신 버전 확인 필요
        classpath ("com.google.gms:google-services:4.3.15")  // Google 서비스 추가
    }
}
