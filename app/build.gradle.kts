plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.example.android_project_msd"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.android_project_msd"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
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

    buildFeatures { compose = true }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    // Basic Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.foundation)

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.2")

    // Compose BOM + base libs
    implementation(platform("androidx.compose:compose-bom:2024.09.01"))
    implementation(libs.firebase.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.runtime)
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.09.01"))

    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Lifecycle + ViewModel
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    // Firebase BOM
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Firebase modules
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")   // *** REQUIRED FOR PROFILE PICS ***

    // Retrofit dependencies (for HttpException)
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
// Retrofit Core
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
// Gson converter for Retrofit
    // Retrofit converter for Moshi
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
// OkHttp Logging Interceptor
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")
    // Optional Firebase modules you already used
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging)

    // Coil (image loading for profile pictures)
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Tests
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // Moshi for JSON parsing
    implementation("com.squareup.moshi:moshi:1.13.0")
    implementation("com.squareup.moshi:moshi-kotlin:1.13.0")
// For Kotlin support
}