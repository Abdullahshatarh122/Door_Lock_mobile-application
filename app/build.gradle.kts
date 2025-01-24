import org.gradle.kotlin.dsl.dependencies


plugins {
    alias(libs.plugins.android.application)
//    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.door_lock_v1"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.door_lock_v2"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    add("implementation", "androidx.appcompat:appcompat:1.6.1")
    add("implementation", "com.google.android.material:material:1.2.1")
    add("implementation", "androidx.constraintlayout:constraintlayout:2.0.4")
    add("implementation", "androidx.lifecycle:lifecycle-extensions:2.2.0")
    add("implementation", "androidx.cardview:cardview:latest")
    add("implementation", "com.google.android.material:material:latest")


    add("implementation", "com.google.firebase:firebase-auth:latest")
    add("implementation", "com.google.android.gms:play-services-auth:latest")

    add("implementation", "com.github.bumptech.glide:glide:4.16.0")

    add("implementation", "com.google.firebase:firebase-messaging:latest")


    implementation("com.google.firebase:firebase-analytics")
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))


}