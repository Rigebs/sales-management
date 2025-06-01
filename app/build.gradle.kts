import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    kotlin("kapt")
    kotlin("plugin.serialization") version "2.0.21"

    id ("kotlin-kapt")
    id ("dagger.hilt.android.plugin")
    id("androidx.navigation.safeargs")
}

android {

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    namespace = "com.rige"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.rige"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "SUPABASE_URL", "\"${localProperties["SUPABASE_URL"]}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${localProperties["SUPABASE_KEY"]}\"")

        buildConfigField("String", "CLOUDINARY_CLOUD_NAME", "\"${localProperties["CLOUDINARY_CLOUD_NAME"]}\"")
        buildConfigField("String", "CLOUDINARY_UPLOAD_PRESET", "\"${localProperties["CLOUDINARY_UPLOAD_PRESET"]}\"")
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
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(platform(libs.bom))
    implementation(libs.postgrest.kt)
    implementation(libs.ktor.client.android)

    implementation (libs.jakewharton.threetenabp)

    implementation (libs.glide)
    kapt (libs.compiler)

    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)

    implementation (libs.hilt.android)
    kapt (libs.hilt.compiler)

    implementation(libs.barcode.scanning)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    implementation (libs.androidx.navigation.fragment.ktx)
    implementation (libs.androidx.navigation.ui.ktx)

    implementation (libs.cloudinary.android)

    implementation(libs.okhttp)
    implementation(libs.json)
    implementation(libs.guava)

    implementation (libs.flexbox)
}

kapt {
    correctErrorTypes = true
}
