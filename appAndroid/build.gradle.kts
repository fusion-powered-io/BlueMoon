plugins {
    alias(libs.plugins.androidApplication)
}

dependencies {
    implementation(projects.appShared)
}

kotlin {
    jvmToolchain(11)
}

android {
    namespace = "io.fusionpowered.bluemoon.android"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.fusionpowered.bluemoon"
        minSdk = 30
        targetSdk = 36
        versionName = System.getenv("APP_VERSION") ?: "1.0.0"
        versionCode = System.getenv("GITHUB_RUN_NUMBER")?.toInt() ?: 1
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}