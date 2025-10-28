import java.util.Properties
import kotlin.toString

plugins {
    id("com.android.library")
    id("kotlin-kapt")
    id("kotlin-parcelize")
    id("org.jetbrains.kotlin.plugin.serialization")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.basesource.base"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
    }
    signingConfigs {
        create("myConfig") {
            keyAlias = localProperties["keyAlias"].toString()
            keyPassword = localProperties["storePass"].toString()
            storeFile = rootProject.file(localProperties["storeFile"] ?: "")
            storePassword = localProperties["storePass"].toString()
        }
    }
    buildTypes {
        release {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("myConfig")
        }

        debug {
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("myConfig")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.4"
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
        // Ensure native libraries are properly packaged
        jniLibs {
            pickFirsts += "**/libamg.so"
        }
    }
}

dependencies {
    coreAndroid()
    composeBom()
    coroutines()
    retrofit()
    koin()
    camera()
}

private fun DependencyHandlerScope.coreAndroid() {
    implementation(libs.colorpicker.compose)
    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.lifecycle.runtime.ktx.v270)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.ktx)
}

private fun DependencyHandlerScope.composeBom() {
    implementation(platform(libs.androidx.compose.bom.v20231001))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.androidx.runtime.livedata)
}

private fun DependencyHandlerScope.coroutines() {
    implementation(libs.kotlinx.coroutines.android)

}

private fun DependencyHandlerScope.retrofit() {
    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Retrofit
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
}

private fun DependencyHandlerScope.koin() {
    ksp(libs.koin.ksp.compiler)
    implementation(libs.koin.annotation)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.coil.compose)

}

private fun DependencyHandlerScope.camera() {
    val cameraX = "1.3.4"
    implementation("androidx.camera:camera-core:$cameraX")
    implementation("androidx.camera:camera-camera2:$cameraX")
    implementation("androidx.camera:camera-lifecycle:$cameraX")
    implementation("androidx.camera:camera-view:$cameraX")
}
