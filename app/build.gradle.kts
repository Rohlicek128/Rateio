import java.util.Properties
import java.io.FileInputStream

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    kotlin("plugin.serialization") version "2.0.0"
}

android {
    namespace = "com.rohlicek.rateio"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.rohlicek.rateio"
        minSdk = 35
        targetSdk = 36
        versionCode = 2
        versionName = "0.1.1"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file(localProperties.getProperty("RELEASE_KEYSTORE_PATH") ?: "")
            storePassword = localProperties.getProperty("RELEASE_KEYSTORE_PASSWORD")
            keyAlias = localProperties.getProperty("RELEASE_KEY_ALIAS")
            keyPassword = localProperties.getProperty("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            buildConfigField(
                "String",
                "TMDB_API_KEY",
                "\"${localProperties["TMDB_API_KEY"]}\""
            )
        }
        release {
            buildConfigField(
                "String",
                "TMDB_API_KEY",
                "\"${localProperties["TMDB_API_KEY"]}\""
            )
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

androidComponents {
    onVariants { variant ->
        variant.outputs.forEach { output ->
            val versionName = android.defaultConfig.versionName ?: "1.0.0"
            (output as? com.android.build.api.variant.impl.VariantOutputImpl)?.outputFileName?.set("Rateio-v${versionName}-alpha.apk")
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3Api")
        freeCompilerArgs.add("-opt-in=androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
    }
}

dependencies {
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui.geometry)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3.adaptive.navigation.suite)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.compose.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.kotlinx.serialization.json)

    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    implementation(libs.androidx.compose.material3)

    implementation(libs.palette)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.kotlin.csv.jvm)
}