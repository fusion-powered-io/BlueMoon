import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-XXLanguage:+ExplicitBackingFields"
        )
    }

    jvm()

    sourceSets {
        jvmMain.dependencies {
            implementation(projects.appShared)
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.fusionpowered.bluemoon.MainApplicationKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "io.fusionpowered.bluemoon"
            packageVersion = System.getenv("APP_VERSION") ?: "1.0.0"
        }
    }
}
