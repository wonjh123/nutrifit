plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.nutrifit1"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.nutrifit1"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    buildFeatures {
        dataBinding = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation ("com.doinglab.foodlens:FoodLensSDK-ui:3.1.0")
    implementation("com.doinglab.foodlens:FoodLensSDK-core:3.0.9")
    implementation(libs.digital.ink.recognition)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    // ✅ OkHttp: HTTP 통신용
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // ✅ JSON 객체 생성용 (org.json)
    implementation("org.json:json:20231013")

    // ✅ AndroidX Core 라이브러리들
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // ✅ Logcat 출력 등 기본 기능
    implementation("androidx.core:core-ktx:1.12.0")
}