plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.julian.appturnos"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.julian.appturnos"
        minSdk = 30
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.legacy.support.v4)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation("com.google.code.gson:gson:2.10.1")
    // Retrofit para llamadas HTTP
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    // Converter Gson para Retrofit
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // OkHttp3 para cliente HTTP
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    // Picasso para carga de imágenes
    implementation("com.squareup.picasso:picasso:2.8")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

}