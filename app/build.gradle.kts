plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.somil.dailywisdom"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.somil.dailywisdom"
        minSdk = 24
        targetSdk = 35
        versionCode = 4
        versionName = "1.0.3"

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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.google.android.gms:play-services-ads:23.0.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")

    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")


    // Firebase BOM manages versions
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation ("com.google.firebase:firebase-analytics")

    // Firestore (for storing quotes)
    implementation("com.google.firebase:firebase-firestore:24.10.0")
    implementation("com.google.firebase:firebase-auth:21.0.1")

    implementation("androidx.core:core-splashscreen:1.0.1")

    // ViewModel
    implementation ("androidx.lifecycle:lifecycle-viewmodel:2.9.2")

    // LiveData
    implementation ("androidx.lifecycle:lifecycle-livedata:2.9.2")

    // Annotation processor
    annotationProcessor ("androidx.lifecycle:lifecycle-compiler:2.9.2")

    // Firebase Authentication
    implementation("com.google.firebase:firebase-bom:32.7.4")
    implementation("com.google.firebase:firebase-auth")

    // Add Firebase Storage
    implementation ("com.google.firebase:firebase-storage")

    // Lottie Animation
    implementation("com.airbnb.android:lottie:6.0.0")

}