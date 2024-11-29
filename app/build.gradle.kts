plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.exo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.exo"
        minSdk = 24
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")



    implementation("androidx.media:media:1.7.0")  // MediaBrowserServiceCompat
    implementation("com.google.android.exoplayer:exoplayer:2.19.1")  // ExoPlayer core
    implementation("com.google.android.exoplayer:extension-mediasession:2.19.1")  // ExoPlayer media session integration
    implementation("com.google.android.exoplayer:exoplayer-ui:2.19.1")  // ExoPlayer UI
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")  // Optional, for lifecycle-aware components

}