import java.util.Properties
import kotlin.toString

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.amb.photo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.amb.emoij"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
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
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("myConfig")
        }

        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("myConfig")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    coreAndroid()
    composeBom()
    coroutines()
    retrofit()
    koin()
    implementation(libs.coil.gif)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.annotation)
    implementation(libs.lottie.compose)


}

private fun DependencyHandlerScope.coreAndroid() {
    implementation(project(":base"))
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
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
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