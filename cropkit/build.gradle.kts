plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("maven-publish")
}

android {
    namespace = "com.tanishranjan.cropkit"
    compileSdk = 35
    version = "1.1.0"

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
        compose = true
    }
}

dependencies {
    coreAndroid()
    composeBom()
    coroutines()
//    implementation(libs.androidx.core.ktx)
//    implementation(libs.androidx.activity.compose)
//    implementation(platform(libs.androidx.compose.bom))
//    implementation(libs.androidx.ui)
//    implementation(libs.androidx.ui.graphics)
//    implementation(libs.androidx.ui.tooling.preview)
//    implementation(libs.androidx.material3)
//    testImplementation(libs.junit)
//    androidTestImplementation(libs.androidx.junit)
//    androidTestImplementation(libs.androidx.espresso.core)
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
//
//tasks.register("copyAar") {
//    doLast {
//        copy {
//            from(file("build/outputs/aar/${project.name}-release.aar"))
//            into(projectDir)
//        }
//    }
//    outputs.file(projectDir.resolve("${project.name}-release.aar"))
//}
//
//tasks.assemble {
//    finalizedBy(":cropkit:copyAar")
//}
//
//afterEvaluate {
//    publishing {
//        publications {
//            register<MavenPublication>("release") {
//                from(components["release"])
//
//                groupId = "com.github.tanish-ranjan"
//                artifactId = "crop-kit"
//            }
//        }
//    }
//}