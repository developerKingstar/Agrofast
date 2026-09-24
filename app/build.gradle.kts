import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

// ---------- Read local.properties ----------
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.example.agrofastsolutions"

    buildFeatures {
        buildConfig = true
    }

    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.agrofastsolutions"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "NEWSDATA_API_KEY",
            "\"${localProperties["newsdata_api_key"] ?: ""}\""
        )
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.activity.ktx)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.lottie)
    implementation(libs.swiperefreshlayout)

    // Retrofit for API calls
    implementation(libs.retrofit)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.retrofit2.adapter.rxjava2)

    // OkHttp for logging
    implementation(libs.okhttp)
    implementation(libs.okhttp3.logging.interceptor)

    // Gson for JSON parsing
    implementation(libs.gson)

    // LiveData and ViewModel
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.livedata)

    // Glide for image loading
    implementation(libs.github.glide)
    annotationProcessor(libs.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ext.junit)
}