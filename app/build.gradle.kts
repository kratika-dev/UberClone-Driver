plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
    alias(libs.plugins.google.android.libraries.mapsplatform.secrets.gradle.plugin)
}

android {
    namespace = "com.example.uberclone"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.uberclone"
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
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.play.services.maps)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform("com.google.firebase:firebase-bom:34.0.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.firebaseui:firebase-ui-auth:9.0.0")
    // RxJava
    implementation("io.reactivex.rxjava3:rxjava:3.1.10")
    // RxAndroid
    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    // Location
    implementation("com.google.android.gms:play-services-location:21.3.0")
    // Dexter
    implementation("com.karumi:dexter:6.2.3")
    // GeoFire for Firebase Realtime Database
    implementation("com.firebase:geofire-android:3.2.0")
    // GeoFire utilities for Cloud Firestore
    implementation("com.firebase:geofire-android-common:3.2.0")
    //Circular Image View
    implementation("de.hdodenhof:circleimageview:3.1.0")
    //Picasso
    implementation("com.squareup.picasso:picasso:2.8")
    //EventBus
    implementation("org.greenrobot:eventbus:3.3.1")
    //Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")

    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    implementation("com.squareup.retrofit2:converter-scalars:2.11.0")

    implementation("com.github.akarnokd:rxjava3-retrofit-adapter:3.0.0")


    //Circular progress bar
    implementation("com.mikhaellopez:circularprogressbar:3.1.0")

    implementation("com.facebook.shimmer:shimmer:0.5.0")

}