plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.ml.mobilefleet"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ml.mobilefleet"
        minSdk = 24
        targetSdk = 36
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Splash Screen API for Android 12+
    implementation("androidx.core:core-splashscreen:1.0.1")

    // Lottie for animations
    implementation("com.airbnb.android:lottie:6.1.0")
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    // Firebase dependencies
    implementation(libs.firebase.firestore.ktx)
    implementation(libs.firebase.auth.ktx)

    // ML Kit for QR code scanning
    implementation(libs.mlkit.barcode.scanning)

    // CameraX for camera functionality
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    // Guava for CameraX ListenableFuture
    implementation("com.google.guava:guava:31.1-android")

    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Navigation
    implementation(libs.navigation.compose)

    // Permissions
    implementation(libs.accompanist.permissions)

    // Cloudinary for image loading
    implementation("com.cloudinary:cloudinary-android:3.0.2")

    // Image loading with Coil
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Environment variables support
    implementation("io.github.cdimascio:dotenv-kotlin:6.4.1")

    // Existing dependencies
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Material Icons Extended for more icons
    implementation("androidx.compose.material:material-icons-extended:1.7.5")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}