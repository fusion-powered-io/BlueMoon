import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {

    compilerOptions {
        optIn.addAll(
            "org.koin.core.annotation.KoinExperimentalAPI",
            "kotlin.ExperimentalUnsignedTypes",
            "androidx.compose.foundation.ExperimentalFoundationApi",
            "androidx.compose.material3.ExperimentalMaterial3Api",
            "com.google.accompanist.permissions.ExperimentalPermissionsApi"
        )
        freeCompilerArgs.addAll(
            "-Xexpect-actual-classes",
            "-Xcontext-parameters",
            "-XXLanguage:+ExplicitBackingFields"
        )
    }

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.lifecycle.process)
            implementation(libs.koin.android)
            implementation(libs.accompanist.permissions)
        }
        commonMain.dependencies {
            implementation(libs.runtime)
            implementation(libs.foundation)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ui)
            implementation(libs.ui.tooling.preview)
            implementation(libs.components.resources)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.material3)
            implementation(libs.androidx.navigation3)
            implementation(libs.androidx.graphics.shapes)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.composeViewmodel)
            implementation(libs.koin.composeViewmodelNavigation)
            implementation(libs.koin.composeNavigation3)
            implementation(libs.composeIcons)
            api(libs.koin.annotations)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
        commonMain.configure {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
    add("kspAndroid", libs.koin.ksp.compiler)
    add("kspJvm", libs.koin.ksp.compiler)
    debugImplementation(compose.uiTooling)
}

ksp {
    arg("KOIN_USE_COMPOSE_VIEWMODEL", "true")
    arg("KOIN_CONFIG_CHECK", "true")
}

tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }.configureEach {
    dependsOn("kspCommonMainKotlinMetadata")
}

val appVersion = System.getenv("APP_VERSION") ?: "1.0.0"

android {
    namespace = "io.fusionpowered.bluemoon"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.fusionpowered.bluemoon"
        minSdk = 30
        targetSdk = 36
        versionName = appVersion
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

compose.desktop {
    application {
        mainClass = "io.fusionpowered.bluemoon.MainApplicationKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "io.fusionpowered.bluemoon"
            packageVersion = appVersion
        }
    }
}
