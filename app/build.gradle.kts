plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

val nativeNdkVersion = "28.2.13676358"
val nativeCmakeVersion = "3.22.1"

android {
    namespace = "com.notcvnt.rknhardering"
    ndkVersion = nativeNdkVersion
    compileSdk = 36

    defaultConfig {
        applicationId = "com.notcvnt.rknhardering"
        minSdk = 26
        targetSdk = 36
        versionCode = 20611
        versionName = "2.6.11"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        androidResources.localeFilters += listOf("en", "ru", "fa", "zh-rCN")

        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86_64")
        }

        externalNativeBuild {
            cmake {
                arguments += listOf("-DANDROID_STL=c++_static")
            }
        }
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        buildConfig = true
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = nativeCmakeVersion
        }
    }
    packaging {
        jniLibs {
            useLegacyPackaging = false
        }
    }
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
    val testHomeDir = layout.projectDirectory.dir(".test-home").asFile
    val tempDir = testHomeDir.resolve("tmp")

    doFirst {
        testHomeDir.mkdirs()
        tempDir.mkdirs()
    }

    systemProperty("user.home", testHomeDir.absolutePath)
    systemProperty("java.io.tmpdir", tempDir.absolutePath)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(project(":xray-protos"))
    implementation(libs.grpc.okhttp)
    implementation(libs.okhttp)
    implementation(libs.okhttp.dnsoverhttps)
    testImplementation(libs.junit)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
