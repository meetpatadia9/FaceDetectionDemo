plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.codewithzebru.facedetectiondemo"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.codewithzebru.facedetectiondemo"
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    implementation("androidx.camera:camera-camera2:1.3.1")
    implementation("androidx.camera:camera-lifecycle:1.3.1")
    implementation("androidx.camera:camera-view:1.4.0-alpha04")

    implementation("com.google.mlkit:text-recognition:16.0.0")

    implementation("androidx.activity:activity-ktx:1.8.2")
    implementation("com.google.guava:guava:33.0.0-android")
    implementation("com.google.android.gms:play-services-mlkit-face-detection:17.1.0")
    implementation("com.google.mlkit:face-mesh-detection:16.0.0-beta1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    /*implementation("io.fotoapparat:facedetector:1.0.0")
    implementation("io.fotoapparat:fotoapparat:2.7.0")
    // If you are using Fotoapparat add this one as well
    implementation("io.fotoapparat.fotoapparat:library:1.2.0") // or later version*/

}